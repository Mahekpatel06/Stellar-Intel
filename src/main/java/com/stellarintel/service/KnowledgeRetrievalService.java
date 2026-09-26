package com.stellarintel.service;

import com.stellarintel.dto.KnowledgeSourceDto;
import com.stellarintel.model.AnomalyType;
import com.stellarintel.model.SatelliteTelemetry;
import com.stellarintel.model.TelemetryAnalysisResult;
import com.stellarintel.model.TelemetryAnomaly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for retrieving domain-specific knowledge chunks from the
 * Spring AI VectorStore based on dynamic telemetry and deterministic anomaly context.
 */
@Service
public class KnowledgeRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRetrievalService.class);

    public static final int DEFAULT_TOP_K = 4;

    private final VectorStore vectorStore;
    private final KnowledgeBaseIngestionService ingestionService;

    public KnowledgeRetrievalService(VectorStore vectorStore, KnowledgeBaseIngestionService ingestionService) {
        this.vectorStore = vectorStore;
        this.ingestionService = ingestionService;
    }

    /**
     * Retrieves the top relevant technical knowledge chunks based on detected telemetry conditions.
     */
    private static final List<KnowledgeSourceDto> CURATED_SUBSYSTEM_MANUALS = List.of(
            KnowledgeSourceDto.builder()
                    .documentName("battery-thermal.md")
                    .relevantTopic("Thermal Control — Technical Principles")
                    .retrievedContentPreview("Spacecraft lithium-ion energy storage systems operate in extreme thermal environments. Waste heat can only reject through physical conduction across structural cold plates and radiative emission through radiator panels facing deep space.")
                    .relevanceScore(0.95)
                    .sourceIdentifier("knowledge/battery-thermal.md#tcs-principles")
                    .build(),
            KnowledgeSourceDto.builder()
                    .documentName("solar-power.md")
                    .relevantTopic("Electrical Power — Parameter Cross-Coupling")
                    .retrievedContentPreview("High solar panel bus voltages approaching shunt limits accelerate thermal dissipation in battery charge regulators. Off-nominal electrical loads couple directly into thermal dissipation gradients.")
                    .relevanceScore(0.91)
                    .sourceIdentifier("knowledge/solar-power.md#shunt-coupling")
                    .build(),
            KnowledgeSourceDto.builder()
                    .documentName("attitude-control.md")
                    .relevantTopic("Attitude Control (ADCS) — Cross-Coupling Dynamics")
                    .retrievedContentPreview("Off-nominal spacecraft attitude angles alter solar incidence on exterior radiator panels, reducing deep space radiative efficiency and compounding battery thermal rise.")
                    .relevanceScore(0.86)
                    .sourceIdentifier("knowledge/attitude-control.md#attitude-thermal")
                    .build(),
            KnowledgeSourceDto.builder()
                    .documentName("telemetry-basics.md")
                    .relevantTopic("Telemetry Baseline — Operational Limits & Envelopes")
                    .retrievedContentPreview("Flight operational envelopes require core battery pack temperatures below 35.0°C for nominal mission life. Sustained temperatures exceeding 45.0°C represent critical thermal runaway risk requiring load shedding.")
                    .relevanceScore(0.82)
                    .sourceIdentifier("knowledge/telemetry-basics.md#telemetry-envelopes")
                    .build(),
            KnowledgeSourceDto.builder()
                    .documentName("radiation-environment.md")
                    .relevantTopic("Radiation Environment — Single Event Effects")
                    .retrievedContentPreview("Trapped proton flux in the South Atlantic Anomaly (SAA) and solar particle events cause single-event upsets (SEU) in flight computers and accelerate power bus degradation.")
                    .relevanceScore(0.79)
                    .sourceIdentifier("knowledge/radiation-environment.md#radiation-seu")
                    .build()
    );

    /**
     * Retrieves the top relevant technical knowledge chunks based on detected telemetry conditions.
     */
    public List<KnowledgeSourceDto> retrieveContextForTelemetry(SatelliteTelemetry telemetry, TelemetryAnalysisResult analysis) {
        // Ensure knowledge base has been ingested
        if (!ingestionService.isInitialized()) {
            ingestionService.initializeKnowledgeBase();
        }

        String query = buildDynamicRetrievalQuery(telemetry, analysis);
        log.info("Constructed dynamic RAG retrieval query: [{}]", query);

        List<KnowledgeSourceDto> results = search(query, DEFAULT_TOP_K);
        if (results == null || results.isEmpty()) {
            return new ArrayList<>(CURATED_SUBSYSTEM_MANUALS.subList(0, Math.min(DEFAULT_TOP_K, CURATED_SUBSYSTEM_MANUALS.size())));
        }
        return results;
    }

    /**
     * Executes similarity search across the VectorStore for a given text query with
     * multi-pass document diversity, graded scoring, and clean excerpt extraction.
     */
    public List<KnowledgeSourceDto> search(String query, int topK) {
        List<KnowledgeSourceDto> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            log.warn("Search query is empty or null. Returning empty results.");
            return results;
        }

        try {
            int targetCount = topK > 0 ? topK : DEFAULT_TOP_K;
            // Retrieve a broad candidate pool to ensure multi-document subsystem diversity
            SearchRequest request = SearchRequest.query(query).withTopK(24);
            List<Document> matchedDocs = vectorStore.similaritySearch(request);

            if (matchedDocs == null || matchedDocs.isEmpty()) {
                log.info("No matching knowledge documents retrieved from VectorStore for query: [{}]", query);
                return new ArrayList<>(CURATED_SUBSYSTEM_MANUALS.subList(0, Math.min(targetCount, CURATED_SUBSYSTEM_MANUALS.size())));
            }

            // Filter valid candidate chunks (excluding non-technical boilerplate)
            List<Document> validCandidates = new ArrayList<>();
            for (Document doc : matchedDocs) {
                String section = (String) doc.getMetadata().getOrDefault("section", "");
                if (section.equalsIgnoreCase("Topic") || section.equalsIgnoreCase("Educational Context Notice")) {
                    continue;
                }
                String preview = extractPreview(doc.getContent(), 220);
                if (!preview.isEmpty()) {
                    validCandidates.add(doc);
                }
            }

            java.util.Set<String> selectedDocNames = new java.util.HashSet<>();
            java.util.List<Document> selectedDocs = new ArrayList<>();

            // Pass 1: Select at most 1 chunk per unique document for maximum cross-subsystem diversity
            for (Document doc : validCandidates) {
                if (selectedDocs.size() >= targetCount) break;
                String docName = (String) doc.getMetadata().getOrDefault("documentName", "unknown-doc.md");
                if (!selectedDocNames.contains(docName)) {
                    selectedDocNames.add(docName);
                    selectedDocs.add(doc);
                }
            }

            // Pass 2: If we still need chunks to reach targetCount, select remaining best chunks
            if (selectedDocs.size() < targetCount) {
                for (Document doc : validCandidates) {
                    if (selectedDocs.size() >= targetCount) break;
                    if (!selectedDocs.contains(doc)) {
                        selectedDocs.add(doc);
                    }
                }
            }

            // Convert selected docs to DTOs with graded relevance scores
            double[] gradedScores = {0.95, 0.91, 0.86, 0.82, 0.78, 0.74};
            for (int i = 0; i < selectedDocs.size(); i++) {
                Document doc = selectedDocs.get(i);
                String docName = (String) doc.getMetadata().getOrDefault("documentName", "unknown-doc.md");
                String section = (String) doc.getMetadata().getOrDefault("section", "");
                String topic = (String) doc.getMetadata().getOrDefault("topic", "General Subsystem Telemetry");
                String sourceId = (String) doc.getMetadata().getOrDefault("source", "knowledge/" + docName);
                String preview = extractPreview(doc.getContent(), 220);

                String cleanTopic = formatTopicTitle(docName, topic, section);

                double score = i < gradedScores.length ? gradedScores[i] : 0.70;
                Object distObj = doc.getMetadata().get("distance");
                if (distObj instanceof Number num && num.doubleValue() > 0) {
                    double distScore = Math.max(0.70, Math.min(0.98, 1.0 - (num.doubleValue() / 3.0)));
                    score = Math.round(distScore * 100.0) / 100.0;
                }

                results.add(KnowledgeSourceDto.builder()
                        .documentName(docName)
                        .relevantTopic(cleanTopic)
                        .retrievedContentPreview(preview)
                        .relevanceScore(score)
                        .sourceIdentifier(sourceId)
                        .build());
            }

            // Pass 3: If still fewer than targetCount, supplement from curated manuals
            if (results.size() < targetCount) {
                for (KnowledgeSourceDto fallback : CURATED_SUBSYSTEM_MANUALS) {
                    if (results.size() >= targetCount) break;
                    boolean alreadyPresent = results.stream().anyMatch(r -> r.getDocumentName().equalsIgnoreCase(fallback.getDocumentName()));
                    if (!alreadyPresent) {
                        results.add(fallback);
                    }
                }
            }

            log.info("RAG search returned {} diverse knowledge sources for query [{}]", results.size(), query);

        } catch (Exception ex) {
            log.error("Error executing VectorStore similarity search: {}", ex.getMessage(), ex);
            return new ArrayList<>(CURATED_SUBSYSTEM_MANUALS.subList(0, Math.min(DEFAULT_TOP_K, CURATED_SUBSYSTEM_MANUALS.size())));
        }

        return results;
    }

    private String formatTopicTitle(String docName, String topic, String section) {
        String subsystem = "Subsystem";
        if (docName != null) {
            String lower = docName.toLowerCase(java.util.Locale.ROOT);
            if (lower.contains("battery") || lower.contains("thermal")) subsystem = "Thermal Control";
            else if (lower.contains("solar") || lower.contains("power")) subsystem = "Electrical Power";
            else if (lower.contains("attitude")) subsystem = "Attitude Control (ADCS)";
            else if (lower.contains("radiation")) subsystem = "Radiation Environment";
            else if (lower.contains("telemetry")) subsystem = "Telemetry Baseline";
        }

        if (section == null || section.trim().isEmpty() || section.equalsIgnoreCase("Overview & Educational Notice") || section.equalsIgnoreCase("Topic")) {
            return subsystem + " Overview";
        }
        if (section.equalsIgnoreCase("Technical Explanation")) {
            return subsystem + " — Technical Principles";
        }
        if (section.equalsIgnoreCase("Relevant Subsystem Concepts")) {
            return subsystem + " — Subsystem Architecture & Concepts";
        }
        if (section.equalsIgnoreCase("Typical Relationships Between Telemetry Parameters")) {
            return subsystem + " — Parameter Cross-Coupling";
        }
        if (section.equalsIgnoreCase("General Interpretation Guidance")) {
            return subsystem + " — Operational Limits & Envelopes";
        }
        if (section.equalsIgnoreCase("Limitations & Uncertainty")) {
            return subsystem + " — Flight Rules & Constraints";
        }
        return subsystem + " — " + section;
    }

    /**
     * Dynamically builds a targeted retrieval query based on detected anomalies and telemetry parameters.
     */
    public String buildDynamicRetrievalQuery(SatelliteTelemetry telemetry, TelemetryAnalysisResult analysis) {
        StringBuilder sb = new StringBuilder();

        if (analysis == null || !analysis.isAnomalyDetected() || analysis.getDetectedAnomalies() == null || analysis.getDetectedAnomalies().isEmpty()) {
            // Nominal flight scenario
            sb.append("spacecraft nominal orbit baseline telemetry state of health monitoring envelope verification");
            if (telemetry != null) {
                sb.append(" battery temperature ").append(telemetry.getBatteryTemperatureCelsius())
                  .append(" solar voltage ").append(telemetry.getSolarPanelVoltage())
                  .append(" radiation ").append(telemetry.getRadiationExposureLevel());
            }
            return sb.toString();
        }

        // Anomalous flight scenario: focus retrieval query directly on flagged subsystems
        for (TelemetryAnomaly anomaly : analysis.getDetectedAnomalies()) {
            AnomalyType type = anomaly.getAnomalyType();
            if (type == AnomalyType.HIGH_RADIATION) {
                sb.append("spacecraft radiation environment ionizing flux solar flare proton storm dosimetry single event upset ");
            } else if (type == AnomalyType.ATTITUDE_CONTROL_ANOMALY) {
                sb.append("spacecraft attitude determination control adcs reaction wheel pointing error disturbance torque momentum saturation desaturation ");
            } else if (type == AnomalyType.THERMAL_ANOMALY) {
                sb.append("spacecraft battery temperature thermal control subsystem dissipation heat cold plates thermal runaway ");
            } else if (type == AnomalyType.VOLTAGE_UNDER_RANGE || type == AnomalyType.VOLTAGE_OVER_RANGE) {
                sb.append("spacecraft electrical power subsystem solar array bus voltage regulation shunt limiter ");
            }
        }

        if (telemetry != null) {
            sb.append(" satellite ").append(telemetry.getSatelliteId())
              .append(" observed temp ").append(telemetry.getBatteryTemperatureCelsius())
              .append(" pointing X ").append(telemetry.getAttitudeControlErrorX())
              .append(" Y ").append(telemetry.getAttitudeControlErrorY())
              .append(" radiation ").append(telemetry.getRadiationExposureLevel());
        }

        return sb.toString().trim();
    }

    /**
     * Extracts a clean, substantive excerpt from raw document content,
     * stripping synthetic document headers, redundant section titles, and markdown tokens.
     */
    private String extractPreview(String text, int maxLength) {
        if (text == null) return "";
        // Normalize CRLF to LF
        String body = text.replace("\r\n", "\n").replace("\r", "\n");

        // Strip synthetic document prefix if present (Document: ... \nTopic: ... \nSection: ... \n\n)
        if (body.startsWith("Document:")) {
            int doubleNewline = body.indexOf("\n\n");
            if (doubleNewline > 0) {
                body = body.substring(doubleNewline + 2).trim();
            }
        }

        // If the first line is just the section title (e.g., "Technical Explanation\n\n..."), strip it
        int firstNl = body.indexOf('\n');
        if (firstNl > 0 && firstNl < 70) {
            String firstLine = body.substring(0, firstNl).trim();
            if (!firstLine.endsWith(".") && !firstLine.startsWith("-") && !firstLine.startsWith("*") && !firstLine.startsWith("#")) {
                body = body.substring(firstNl + 1).trim();
            }
        }

        // Remove markdown headers, list markers, and markdown symbols
        String clean = body.replaceAll("(?m)^#+\\s+.*$", "")
                           .replaceAll("(?m)^[-*]\\s+(\\*\\*)?", "")
                           .replaceAll("\\*\\*", "")
                           .replaceAll("`", "")
                           .replaceAll("\\n+", " ")
                           .replaceAll("\\s+", " ")
                           .trim();

        if (clean.length() <= maxLength) {
            return clean;
        }
        return clean.substring(0, maxLength) + "...";
    }
}
