package com.stellarintel.service;

import com.stellarintel.dto.GenAiDiagnosticResponse;
import com.stellarintel.dto.KnowledgeSourceDto;
import com.stellarintel.model.SatelliteTelemetry;
import com.stellarintel.model.TelemetryAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4 Comprehensive RAG Unit Tests.
 *
 * Validates document loading, semantic chunking, VectorStore indexing,
 * dynamic anomaly query formulation, and retrieval relevance across satellite subsystems.
 */
class KnowledgeBaseRAGTest {

    private StellarIntelEmbeddingModel embeddingModel;
    private VectorStore vectorStore;
    private KnowledgeBaseIngestionService ingestionService;
    private KnowledgeRetrievalService retrievalService;
    private TelemetryService telemetryService;
    private TelemetryAnalysisService analysisService;
    private GenAiDiagnosticService genAiService;

    @BeforeEach
    void setUp() {
        embeddingModel = new StellarIntelEmbeddingModel("mock-stellarintel-key", "https://api.openai.com");
        vectorStore = new SimpleVectorStore(embeddingModel);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        ingestionService = new KnowledgeBaseIngestionService(vectorStore, resolver);
        ingestionService.initializeKnowledgeBase();

        retrievalService = new KnowledgeRetrievalService(vectorStore, ingestionService);

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        telemetryService = new TelemetryService(mapper);
        analysisService = new TelemetryAnalysisService(telemetryService);
        genAiService = new GenAiDiagnosticService(null, mapper, retrievalService);
    }

    @Test
    @DisplayName("Knowledge Base Ingestion: 5 markdown documents are loaded and chunked")
    void testKnowledgeBaseIngestion() {
        assertTrue(ingestionService.isInitialized(), "Knowledge base should be marked initialized");
        assertEquals(5, ingestionService.getIndexedDocumentCount(), "Should index exactly 5 technical documents");
        assertTrue(ingestionService.getIndexedChunkCount() >= 20, "Should generate multiple semantic chunks across documents");
    }

    @Test
    @DisplayName("Document Chunking preserves title, topic, and level-2 markdown sections")
    void testChunkMarkdownDocument() {
        String sampleMd = """
                # Thermal Management Subsystem
                ## Topic
                Battery Heat Dissipation
                ## Technical Explanation
                Lithium-ion cells radiate heat through cold plates.
                ## Recommendations
                Monitor battery temperature.
                """;
        List<Document> chunks = ingestionService.chunkMarkdownDocument(sampleMd, "test-doc.md", "Thermal Subsystem", "Battery Heat");
        assertNotNull(chunks);
        assertTrue(chunks.size() >= 3);
        assertEquals("test-doc.md", chunks.get(0).getMetadata().get("documentName"));
    }

    @Test
    @DisplayName("RAG Retrieval: Thermal Query retrieves battery-thermal.md")
    void testRetrieval_ThermalQuery() {
        List<KnowledgeSourceDto> results = retrievalService.search("spacecraft battery temperature thermal control subsystem dissipation heat", 3);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        boolean hasThermalDoc = results.stream().anyMatch(d -> d.getDocumentName().contains("battery-thermal.md"));
        assertTrue(hasThermalDoc, "Top results should include battery-thermal.md");
    }

    @Test
    @DisplayName("RAG Retrieval: Attitude Query retrieves attitude-control.md")
    void testRetrieval_AttitudeQuery() {
        List<KnowledgeSourceDto> results = retrievalService.search("spacecraft attitude determination control adcs reaction wheel pointing error", 3);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        boolean hasAttitudeDoc = results.stream().anyMatch(d -> d.getDocumentName().contains("attitude-control.md"));
        assertTrue(hasAttitudeDoc, "Top results should include attitude-control.md");
    }

    @Test
    @DisplayName("RAG Retrieval: Radiation Query retrieves radiation-environment.md")
    void testRetrieval_RadiationQuery() {
        List<KnowledgeSourceDto> results = retrievalService.search("spacecraft ionizing radiation environment solar flare proton storm dosimetry", 3);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        boolean hasRadiationDoc = results.stream().anyMatch(d -> d.getDocumentName().contains("radiation-environment.md"));
        assertTrue(hasRadiationDoc, "Top results should include radiation-environment.md");
    }

    @Test
    @DisplayName("RAG Retrieval: Empty or blank query is handled gracefully without errors")
    void testRetrieval_EmptyQueryGraceful() {
        List<KnowledgeSourceDto> emptyResults = retrievalService.search("", 3);
        assertNotNull(emptyResults);
        assertTrue(emptyResults.isEmpty());

        List<KnowledgeSourceDto> nullResults = retrievalService.search(null, 3);
        assertNotNull(nullResults);
        assertTrue(nullResults.isEmpty());
    }

    @Test
    @DisplayName("Dynamic Retrieval for Telemetry: Solar Flare telemetry retrieves radiation knowledge")
    void testDynamicRetrieval_SolarFlareProfile() {
        SatelliteTelemetry solarFlare = telemetryService.getProfileByKey("solar-flare");
        TelemetryAnalysisResult analysis = analysisService.analyzeTelemetry(solarFlare);

        List<KnowledgeSourceDto> sources = retrievalService.retrieveContextForTelemetry(solarFlare, analysis);
        assertNotNull(sources);
        assertFalse(sources.isEmpty());
        assertTrue(sources.stream().anyMatch(s -> s.getDocumentName().contains("radiation-environment.md")));
    }

    @Test
    @DisplayName("Dynamic Retrieval for Telemetry: Reaction Wheel Anomaly retrieves attitude-control knowledge")
    void testDynamicRetrieval_ReactionWheelProfile() {
        SatelliteTelemetry reactionWheel = telemetryService.getProfileByKey("reaction-wheel-anomaly");
        TelemetryAnalysisResult analysis = analysisService.analyzeTelemetry(reactionWheel);

        List<KnowledgeSourceDto> sources = retrievalService.retrieveContextForTelemetry(reactionWheel, analysis);
        assertNotNull(sources);
        assertFalse(sources.isEmpty());
        assertTrue(sources.stream().anyMatch(s -> s.getDocumentName().contains("attitude-control.md")));
    }

    @Test
    @DisplayName("Prompt Augmentation: User prompt embeds telemetry, deterministic analysis, and retrieved knowledge")
    void testUserPromptWithRAGContext() {
        SatelliteTelemetry solarFlare = telemetryService.getProfileByKey("solar-flare");
        TelemetryAnalysisResult analysis = analysisService.analyzeTelemetry(solarFlare);
        List<KnowledgeSourceDto> sources = retrievalService.retrieveContextForTelemetry(solarFlare, analysis);

        String userPrompt = genAiService.buildUserPrompt(solarFlare, analysis, sources, "JSON_FORMAT");

        assertTrue(userPrompt.contains("TELEMETRY CONTEXT:"));
        assertTrue(userPrompt.contains("DETERMINISTIC ANALYSIS (Ground Truth):"));
        assertTrue(userPrompt.contains("HIGH_RADIATION"));
        assertTrue(userPrompt.contains("RETRIEVED TECHNICAL KNOWLEDGE (Domain Context from Knowledge Base):"));
        assertTrue(userPrompt.contains("radiation-environment.md"));
        assertTrue(userPrompt.contains("JSON_FORMAT"));
    }

    @Test
    @DisplayName("End-to-End Diagnostic with RAG: Offline diagnostic synthesizes grounded analysis with knowledge sources")
    void testDiagnoseTelemetry_OfflineSynthesisWithRAGSources() {
        SatelliteTelemetry solarFlare = telemetryService.getProfileByKey("solar-flare");
        TelemetryAnalysisResult analysis = analysisService.analyzeTelemetry(solarFlare);

        GenAiDiagnosticResponse response = genAiService.diagnoseTelemetry(solarFlare, analysis);

        assertNotNull(response);
        assertEquals("OFFLINE_SYNTHESIS", response.getStatus());
        assertNotNull(response.getKnowledgeSources());
        assertFalse(response.getKnowledgeSources().isEmpty(), "Knowledge sources should be attached to diagnostic response");
        assertTrue(response.getKnowledgeSources().stream().anyMatch(k -> k.getDocumentName().equals("radiation-environment.md")));
        assertTrue(response.getDiagnosticSummary().contains("radiation-environment.md"));
    }
}
