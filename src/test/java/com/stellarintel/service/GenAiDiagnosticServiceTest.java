package com.stellarintel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stellarintel.dto.GenAiDiagnosticResponse;
import com.stellarintel.model.HealthStatus;
import com.stellarintel.model.SatelliteTelemetry;
import com.stellarintel.model.TelemetryAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GenAiDiagnosticServiceTest {

    private TelemetryService telemetryService;
    private TelemetryAnalysisService analysisService;
    private GenAiDiagnosticService genAiService;
    private ChatModel mockChatModel;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        telemetryService = new TelemetryService(objectMapper);
        analysisService = new TelemetryAnalysisService(telemetryService);
        mockChatModel = Mockito.mock(ChatModel.class);
        genAiService = new GenAiDiagnosticService(mockChatModel, objectMapper);
    }

    @Test
    @DisplayName("Service Instantiation & System Prompt Directives Verification")
    void testServiceInstantiationAndSystemPrompt() {
        assertNotNull(genAiService);
        String systemPrompt = genAiService.buildSystemPrompt();

        assertTrue(systemPrompt.contains("StellarIntel Satellite Telemetry Diagnostic Assistant"));
        assertTrue(systemPrompt.contains("AUTHORITATIVE"));
        assertTrue(systemPrompt.contains("must NOT recalculate, override, or invent telemetry values"));
        assertTrue(systemPrompt.contains("DO NOT issue direct commands to a spacecraft"));
    }

    @Test
    @DisplayName("User Prompt Construction embeds full telemetry and deterministic ground truth")
    void testUserPromptConstruction() {
        SatelliteTelemetry solarFlare = telemetryService.getProfileByKey("solar-flare");
        TelemetryAnalysisResult analysis = analysisService.analyzeTelemetry(solarFlare);

        String userPrompt = genAiService.buildUserPrompt(solarFlare, analysis, "FORMAT_INSTRUCTIONS");

        assertTrue(userPrompt.contains("STELLAR-SAT-01"));
        assertTrue(userPrompt.contains("38.2"));
        assertTrue(userPrompt.contains("8.75"));
        assertTrue(userPrompt.contains("Overall Health Status: CRITICAL"));
        assertTrue(userPrompt.contains("HIGH_RADIATION"));
        assertTrue(userPrompt.contains("FORMAT_INSTRUCTIONS"));
    }

    @Test
    @DisplayName("Offline Mode: When API key is placeholder, returns structured fallback diagnostic without crashing")
    void testDiagnoseTelemetry_OfflineModeFallback() {
        ReflectionTestUtils.setField(genAiService, "openAiApiKey", "mock-stellarintel-key");

        SatelliteTelemetry nominal = telemetryService.getProfileByKey("nominal-orbit");
        TelemetryAnalysisResult analysis = analysisService.analyzeTelemetry(nominal);

        GenAiDiagnosticResponse response = genAiService.diagnoseTelemetry(nominal, analysis);

        assertNotNull(response);
        assertEquals("OFFLINE_SYNTHESIS", response.getStatus());
        assertTrue(response.getDiagnosticSummary().contains("stable nominal state"));
        assertFalse(response.getObservedConditions().isEmpty());
        assertFalse(response.getAffectedSubsystems().isEmpty());
        assertFalse(response.getRecommendedInvestigation().isEmpty());
    }

    @Test
    @DisplayName("Live LLM Mock: Spring AI ChatModel returns structured response successfully parsed")
    void testDiagnoseTelemetry_LiveChatModelSuccess() {
        // Enable live mode by setting a simulated key
        ReflectionTestUtils.setField(genAiService, "openAiApiKey", "sk-proj-valid-test-key");

        String mockAiJson = """
                {
                    "diagnosticSummary": "Test Executive Summary: Solar flare event detected.",
                    "observedConditions": ["Radiation flux measured at 8.75 uSv/h."],
                    "possibleContributingFactors": ["Coronal mass ejection transit."],
                    "affectedSubsystems": ["Dosimetry", "Power Bus"],
                    "recommendedInvestigation": ["Check SEU error counts."],
                    "limitations": ["Single point telemetry snapshot."],
                    "status": "SUCCESS",
                    "disclaimer": "Simulated AI output.",
                    "generatedAt": "2026-09-25T12:00:00Z"
                }
                """;

        AssistantMessage assistantMessage = new AssistantMessage(mockAiJson);
        Generation generation = new Generation(assistantMessage);
        ChatResponse mockChatResponse = new ChatResponse(List.of(generation));

        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

        SatelliteTelemetry solarFlare = telemetryService.getProfileByKey("solar-flare");
        TelemetryAnalysisResult analysis = analysisService.analyzeTelemetry(solarFlare);

        GenAiDiagnosticResponse response = genAiService.diagnoseTelemetry(solarFlare, analysis);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertTrue(response.getDiagnosticSummary().contains("Solar flare event detected"));
        assertEquals(1, response.getObservedConditions().size());
        assertEquals(2, response.getAffectedSubsystems().size());
    }

    @Test
    @DisplayName("Exception Resilience: When Spring AI call throws an exception, returns controlled fallback response")
    void testDiagnoseTelemetry_ChatModelExceptionFallback() {
        ReflectionTestUtils.setField(genAiService, "openAiApiKey", "sk-proj-valid-test-key");

        when(mockChatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("OpenAI API rate limit exceeded"));

        SatelliteTelemetry solarFlare = telemetryService.getProfileByKey("solar-flare");
        TelemetryAnalysisResult analysis = analysisService.analyzeTelemetry(solarFlare);

        // Must NOT throw exception; must return controlled fallback
        GenAiDiagnosticResponse response = assertDoesNotThrow(() -> genAiService.diagnoseTelemetry(solarFlare, analysis));

        assertNotNull(response);
        assertEquals("OFFLINE_FALLBACK", response.getStatus());
        assertTrue(response.getDiagnosticSummary().contains("rate limit exceeded"));
    }
}
