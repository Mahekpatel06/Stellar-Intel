/**
 * StellarIntel - Mission Control Telemetry Dashboard JavaScript
 * Phase 3: Spring AI GenAI Diagnostic Layer & Mission Control Integration
 *
 * ARCHITECTURAL RULE:
 * Deterministic Java backend rules remain the authoritative ground truth for
 * numerical threshold evaluation, anomaly detection, and overall health status.
 * Spring AI GenAI reasoning layer provides technical interpretation, contributing factors,
 * affected subsystems, and recommended investigation procedures.
 */

// Simulated Telemetry Profiles (Phase 1, Phase 2, & Phase 3 Synthetic Data)
const SIMULATED_PROFILES = {
  "nominal-orbit": {
    "satelliteId": "STELLAR-SAT-01",
    "timestamp": "2026-09-25T12:00:00Z",
    "batteryTemperatureCelsius": 18.5,
    "solarPanelVoltage": 30.2,
    "attitudeControlErrorX": 0.012,
    "attitudeControlErrorY": -0.008,
    "radiationExposureLevel": 0.42
  },
  "solar-flare": {
    "satelliteId": "STELLAR-SAT-01",
    "timestamp": "2026-09-25T12:05:00Z",
    "batteryTemperatureCelsius": 38.2,
    "solarPanelVoltage": 34.8,
    "attitudeControlErrorX": 0.035,
    "attitudeControlErrorY": 0.041,
    "radiationExposureLevel": 8.75
  },
  "reaction-wheel-anomaly": {
    "satelliteId": "STELLAR-SAT-01",
    "timestamp": "2026-09-25T12:10:00Z",
    "batteryTemperatureCelsius": 22.1,
    "solarPanelVoltage": 28.7,
    "attitudeControlErrorX": 1.845,
    "attitudeControlErrorY": -2.312,
    "radiationExposureLevel": 0.55
  },
  "thermal-anomaly": {
    "satelliteId": "STELLAR-SAT-01",
    "timestamp": "2026-09-25T12:15:00Z",
    "batteryTemperatureCelsius": 52.4,
    "solarPanelVoltage": 31.0,
    "attitudeControlErrorX": 0.015,
    "attitudeControlErrorY": 0.012,
    "radiationExposureLevel": 0.60
  },
  "multiple-anomalies": {
    "satelliteId": "STELLAR-SAT-01",
    "timestamp": "2026-09-25T12:20:00Z",
    "batteryTemperatureCelsius": 51.8,
    "solarPanelVoltage": 38.5,
    "attitudeControlErrorX": 1.450,
    "attitudeControlErrorY": -1.820,
    "radiationExposureLevel": 9.20
  }
};

// UI Element References: Ingestion & Controls
const profileSelect = document.getElementById("profileSelect");
const rawJsonInput = document.getElementById("rawJsonInput");
const loadProfileBtn = document.getElementById("loadProfileBtn");
const parseTelemetryBtn = document.getElementById("parseTelemetryBtn");
const analyzeTelemetryBtn = document.getElementById("analyzeTelemetryBtn");
const headerRunAiBtn = document.getElementById("headerRunAiBtn");
const resetTelemetryBtn = document.getElementById("resetTelemetryBtn");
const alertBanner = document.getElementById("alertBanner");
const alertText = document.getElementById("alertText");

// UI Element References: Live Telemetry Instruments
const valSatelliteId = document.getElementById("valSatelliteId");
const valTimestamp = document.getElementById("valTimestamp");
const valBatteryTemp = document.getElementById("valBatteryTemp");
const valSolarVoltage = document.getElementById("valSolarVoltage");
const valAttitudeX = document.getElementById("valAttitudeX");
const valAttitudeY = document.getElementById("valAttitudeY");
const valRadiation = document.getElementById("valRadiation");

// UI Element References: Telemetry Gauges / Bars
const barBattery = document.getElementById("barBattery");
const barSolar = document.getElementById("barSolar");
const barAttitudeX = document.getElementById("barAttitudeX");
const barAttitudeY = document.getElementById("barAttitudeY");
const barRadiation = document.getElementById("barRadiation");

// UI Element References: Phase 2 Deterministic Dashboard
const healthBadge = document.getElementById("healthBadge");
const healthStatusText = document.getElementById("healthStatusText");
const kpiStatus = document.getElementById("kpiStatus");
const kpiAnomalyDetected = document.getElementById("kpiAnomalyDetected");
const kpiAnomalyCount = document.getElementById("kpiAnomalyCount");
const analysisSummaryText = document.getElementById("analysisSummaryText");
const statusExplanationText = document.getElementById("statusExplanationText");
const anomaliesContainer = document.getElementById("anomaliesContainer");

// UI Element References: Phase 3 GenAI Diagnostic Command Center
const triggerAiBtn = document.getElementById("triggerAiBtn");
const aiStatusBadge = document.getElementById("aiStatusBadge");
const aiStatusBadgeText = document.getElementById("aiStatusBadgeText");
const aiStatusPulse = document.getElementById("aiStatusPulse");
const aiEngineLabel = document.getElementById("aiEngineLabel");

const aiStandbyCard = document.getElementById("aiStandbyCard");
const aiLoadingCard = document.getElementById("aiLoadingCard");
const aiResultsContainer = document.getElementById("aiResultsContainer");

const aiModelBadge = document.getElementById("aiModelBadge");
const aiTimestampBadge = document.getElementById("aiTimestampBadge");
const aiStatusPill = document.getElementById("aiStatusPill");
const aiDisclaimerText = document.getElementById("aiDisclaimerText");

const aiSummaryContent = document.getElementById("aiSummaryContent");
const aiObservedConditionsList = document.getElementById("aiObservedConditionsList");
const aiContributingFactorsList = document.getElementById("aiContributingFactorsList");
const aiAffectedSubsystemsContainer = document.getElementById("aiAffectedSubsystemsContainer");
const aiRecommendedInvestigationList = document.getElementById("aiRecommendedInvestigationList");
const aiLimitationsList = document.getElementById("aiLimitationsList");

// Phase 6: Automated Mission Diagnostic Report Elements
const generateReportBtn = document.getElementById("generateReportBtn");
const generateReportBtnText = document.getElementById("generateReportBtnText");
const generateReportBannerBtn = document.getElementById("generateReportBannerBtn");
const generateReportBannerBtnText = document.getElementById("generateReportBannerBtnText");

let lastDiagnosticReport = null;

function setReportButtonsEnabled(enabled) {
  if (generateReportBtn) {
    generateReportBtn.disabled = !enabled;
    if (enabled && generateReportBtnText) {
      generateReportBtnText.textContent = "GENERATE MISSION REPORT";
    }
  }
  if (generateReportBannerBtn) {
    generateReportBannerBtn.disabled = !enabled;
  }
}

/**
 * Display an alert message in the feedback banner
 */
function showAlert(message, type = "info") {
  if (!alertBanner || !alertText) return;
  alertBanner.className = `alert-feedback ${type}`;
  alertText.textContent = message;
  alertBanner.classList.remove("hidden");
}

/**
 * Hide the feedback banner
 */
function hideAlert() {
  if (!alertBanner || !alertText) return;
  alertBanner.className = "alert-feedback hidden";
  alertText.textContent = "";
}

/**
 * Load selected profile JSON into the editor
 */
function loadSelectedProfile() {
  const selectedKey = profileSelect.value;
  if (!selectedKey || !SIMULATED_PROFILES[selectedKey]) {
    showAlert("Please select a simulated profile from the dropdown.", "error");
    return;
  }

  const profileData = SIMULATED_PROFILES[selectedKey];
  rawJsonInput.value = JSON.stringify(profileData, null, 2);
  showAlert(`Loaded simulated profile: "${profileSelect.options[profileSelect.selectedIndex].text}". Ready for parsing, deterministic analysis, and AI diagnosis.`, "info");
}

/**
 * Update the Mission Control live instrumentation readouts
 */
function updateDashboard(telemetry) {
  if (!telemetry) return;

  valSatelliteId.textContent = telemetry.satelliteId || "N/A";
  valTimestamp.textContent = telemetry.timestamp || "N/A";

  // Battery Temperature (-20°C to 60°C range mapping for bar)
  const temp = Number(telemetry.batteryTemperatureCelsius);
  valBatteryTemp.textContent = !isNaN(temp) ? temp.toFixed(1) : "--";
  if (!isNaN(temp)) {
    const tempPercent = Math.min(Math.max(((temp + 20) / 80) * 100, 5), 100);
    barBattery.style.width = `${tempPercent}%`;
    if (temp > 45.0) {
      valBatteryTemp.className = "telemetry-num metric-danger";
    } else if (temp > 35.0) {
      valBatteryTemp.className = "telemetry-num metric-warning";
    } else {
      valBatteryTemp.className = "telemetry-num metric-normal";
    }
  }

  // Solar Panel Voltage (0V to 40V range mapping)
  const voltage = Number(telemetry.solarPanelVoltage);
  valSolarVoltage.textContent = !isNaN(voltage) ? voltage.toFixed(1) : "--";
  if (!isNaN(voltage)) {
    const voltPercent = Math.min(Math.max((voltage / 40) * 100, 5), 100);
    barSolar.style.width = `${voltPercent}%`;
    if (voltage < 26.0 || voltage > 35.0) {
      valSolarVoltage.className = "telemetry-num metric-warning";
    } else {
      valSolarVoltage.className = "telemetry-num metric-normal";
    }
  }

  // Attitude Error X (-3° to +3° range mapping)
  const errX = Number(telemetry.attitudeControlErrorX);
  valAttitudeX.textContent = !isNaN(errX) ? (errX >= 0 ? `+${errX.toFixed(3)}` : errX.toFixed(3)) : "--";
  if (!isNaN(errX)) {
    const errXPercent = Math.min(Math.max(((Math.abs(errX)) / 3) * 100, 5), 100);
    barAttitudeX.style.width = `${errXPercent}%`;
    if (Math.abs(errX) > 0.05) {
      valAttitudeX.className = "telemetry-num metric-danger";
    } else {
      valAttitudeX.className = "telemetry-num metric-normal";
    }
  }

  // Attitude Error Y (-3° to +3° range mapping)
  const errY = Number(telemetry.attitudeControlErrorY);
  valAttitudeY.textContent = !isNaN(errY) ? (errY >= 0 ? `+${errY.toFixed(3)}` : errY.toFixed(3)) : "--";
  if (!isNaN(errY)) {
    const errYPercent = Math.min(Math.max(((Math.abs(errY)) / 3) * 100, 5), 100);
    barAttitudeY.style.width = `${errYPercent}%`;
    if (Math.abs(errY) > 0.05) {
      valAttitudeY.className = "telemetry-num metric-danger";
    } else {
      valAttitudeY.className = "telemetry-num metric-normal";
    }
  }

  // Radiation Exposure (0 to 10 µSv/h range mapping)
  const rad = Number(telemetry.radiationExposureLevel);
  valRadiation.textContent = !isNaN(rad) ? rad.toFixed(2) : "--";
  if (!isNaN(rad)) {
    const radPercent = Math.min(Math.max((rad / 10) * 100, 5), 100);
    barRadiation.style.width = `${radPercent}%`;
    if (rad >= 8.0) {
      valRadiation.className = "telemetry-num metric-danger";
    } else if (rad > 5.0) {
      valRadiation.className = "telemetry-num metric-warning";
    } else {
      valRadiation.className = "telemetry-num metric-normal";
    }
  }
}

/**
 * Parses and verifies raw telemetry JSON
 */
async function parseTelemetry() {
  const rawText = rawJsonInput.value.trim();
  if (!rawText) {
    showAlert("Please enter or load telemetry JSON before parsing.", "error");
    return null;
  }

  let telemetry;
  try {
    telemetry = JSON.parse(rawText);
  } catch (err) {
    showAlert(`JSON Parse Error: ${err.message}. Please check syntax.`, "error");
    return null;
  }

  if (!telemetry.satelliteId || typeof telemetry.satelliteId !== "string" || !telemetry.satelliteId.trim()) {
    showAlert("Validation Error: Missing or blank 'satelliteId' field.", "error");
    return null;
  }

  if (!telemetry.timestamp || typeof telemetry.timestamp !== "string" || !telemetry.timestamp.trim()) {
    showAlert("Validation Error: Missing or blank 'timestamp' field.", "error");
    return null;
  }

  try {
    const response = await fetch("/api/telemetry/validate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(telemetry)
    });

    const result = await response.json();

    if (response.ok && result.success) {
      updateDashboard(result.telemetry || telemetry);
      showAlert(`Telemetry verified for [${telemetry.satelliteId}]. Click 'Analyze Telemetry' or 'Run AI Diagnostic'.`, "success");
      return telemetry;
    } else {
      showAlert(`Validation Failed: ${result.message || 'Unknown error'}`, "error");
      return null;
    }
  } catch (netErr) {
    updateDashboard(telemetry);
    showAlert(`Telemetry parsed locally (Offline Mode): ${netErr.message}`, "info");
    return telemetry;
  }
}

/**
 * Phase 2: Executes Deterministic Telemetry Analysis
 * Calls POST /api/telemetry/analyze and updates the Mission Control Dashboard
 */
async function analyzeTelemetry() {
  const rawText = rawJsonInput.value.trim();
  if (!rawText) {
    showAlert("Please enter or load telemetry JSON before analyzing.", "error");
    return;
  }

  let telemetry;
  try {
    telemetry = JSON.parse(rawText);
  } catch (err) {
    showAlert(`JSON Parse Error: ${err.message}. Malformed telemetry payload.`, "error");
    return;
  }

  try {
    showAlert(`Executing deterministic Java analysis for [${telemetry.satelliteId || 'Spacecraft'}]...`, "info");

    const response = await fetch("/api/telemetry/analyze", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(telemetry)
    });

    const result = await response.json();

    if (response.ok) {
      // 1. Sync live instrument dials
      updateDashboard(telemetry);

      // 2. Render deterministic analysis result
      renderAnalysisResult(result);

      // 3. Update AI Command Center status to READY
      if (aiStatusBadge && aiStatusBadgeText) {
        aiStatusBadge.className = "ai-status-badge ai-ready";
        aiStatusBadgeText.textContent = "AI ANALYSIS: READY";
      }

      showAlert(`Deterministic analysis complete. Derived Health Status: ${result.overallStatus}. Found ${result.anomalyCount} anomalies. Ready for AI Diagnosis.`, "success");
    } else {
      showAlert(`Analysis Error (${response.status}): ${result.message || 'Backend evaluation failed'}`, "error");
    }
  } catch (netErr) {
    showAlert(`Network communication error: ${netErr.message}`, "error");
  }
}

/**
 * Updates the visual 4-stage knowledge-assisted analysis pipeline stepper
 * 1: ANALYZING TELEMETRY...
 * 2: DETECTING ANOMALIES...
 * 3: RETRIEVING TECHNICAL KNOWLEDGE...
 * 4: GENERATING DIAGNOSTIC...
 */
function updatePipelineStep(stepNumber) {
  for (let i = 1; i <= 4; i++) {
    const row = document.getElementById(`pipeStep${i}`);
    if (!row) continue;
    const stateText = row.querySelector(".step-state-text");
    if (i < stepNumber) {
      row.className = "pipeline-step-row step-complete";
      if (stateText) stateText.textContent = "DONE";
    } else if (i === stepNumber) {
      row.className = "pipeline-step-row step-active";
      if (stateText) stateText.textContent = "RUNNING";
    } else {
      row.className = "pipeline-step-row step-pending";
      if (stateText) stateText.textContent = "QUEUED";
    }
  }
}

/**
 * Phase 4: Executes Spring AI GenAI Diagnostic Pipeline with RAG Context
 * Calls POST /api/telemetry/diagnose
 * Displays loading state with 4-stage stepper
 * Renders deterministic ground truth, retrieved knowledge sources, and GenAI sections
 */
async function runAiDiagnostic() {
  const rawText = rawJsonInput.value.trim();
  if (!rawText) {
    showAlert("Please enter or load telemetry JSON before running AI diagnostic.", "error");
    return;
  }

  let telemetry;
  try {
    telemetry = JSON.parse(rawText);
  } catch (err) {
    showAlert(`JSON Parse Error: ${err.message}. Malformed telemetry payload.`, "error");
    return;
  }

  // 1. Set Loading State and start Stage 1
  setAiLoadingState(true);
  updatePipelineStep(1);
  showAlert(`Analyzing telemetry and synthesizing diagnostic for [${telemetry.satelliteId || 'Spacecraft'}]...`, "info");

  // Step 2 & 3 dynamic progress timers while request is in flight
  const timerStep2 = setTimeout(() => updatePipelineStep(2), 250);
  const timerStep3 = setTimeout(() => updatePipelineStep(3), 600);
  const timerStep4 = setTimeout(() => updatePipelineStep(4), 1100);

  try {
    const response = await fetch("/api/telemetry/diagnose", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(telemetry)
    });

    clearTimeout(timerStep2);
    clearTimeout(timerStep3);
    clearTimeout(timerStep4);
    updatePipelineStep(5); // marks all 4 as complete

    const report = await response.json();

    if (response.ok && report.analysis && report.diagnostic) {
      // Update Live Instruments
      updateDashboard(telemetry);

      // Render Deterministic Ground Truth
      renderAnalysisResult(report.analysis);

      // Render AI Diagnostic & Referenced Documentation
      renderAiDiagnostic(report.diagnostic);

      // Cache complete diagnostic report for Phase 6 automated PDF report generation
      lastDiagnosticReport = {
        telemetry: telemetry,
        analysis: report.analysis,
        diagnostic: report.diagnostic,
        timestamp: report.timestamp || new Date().toISOString()
      };
      setReportButtonsEnabled(true);

      // Update AI Status Badge based on execution mode
      if (aiStatusBadge && aiStatusBadgeText) {
        const diagStatus = report.diagnostic.status || "SUCCESS";
        if (diagStatus === "SUCCESS") {
          aiStatusBadge.className = "ai-status-badge ai-completed";
          aiStatusBadgeText.textContent = "AI ANALYSIS: COMPLETE";
        } else if (diagStatus === "OFFLINE_SYNTHESIS") {
          aiStatusBadge.className = "ai-status-badge ai-completed";
          aiStatusBadgeText.textContent = "AI ANALYSIS: COMPLETE";
        } else {
          aiStatusBadge.className = "ai-status-badge";
          aiStatusBadgeText.textContent = "AI ANALYSIS: FALLBACK ACTIVATED";
        }
      }

      const sourcesCount = report.diagnostic.knowledgeSources ? report.diagnostic.knowledgeSources.length : 0;
      showAlert(`Diagnostic report generated successfully with ${sourcesCount} referenced technical document(s). Health Status: ${report.analysis.overallStatus}.`, "success");

      // Smooth scroll to AI diagnostic section
      const genAiSection = document.getElementById("genAiSection");
      if (genAiSection) {
        genAiSection.scrollIntoView({ behavior: "smooth", block: "nearest" });
      }

    } else if (report.analysis) {
      // LLM failed but deterministic analysis is preserved
      updateDashboard(telemetry);
      renderAnalysisResult(report.analysis);

      if (report.diagnostic) {
        renderAiDiagnostic(report.diagnostic);
      }

      // Cache partial report even if LLM failed
      lastDiagnosticReport = {
        telemetry: telemetry,
        analysis: report.analysis,
        diagnostic: report.diagnostic,
        timestamp: report.timestamp || new Date().toISOString()
      };
      setReportButtonsEnabled(true);

      showAlert(`Deterministic analysis succeeded, but AI diagnostic returned: ${report.diagnostic ? report.diagnostic.diagnosticSummary : 'Unavailable'}`, "error");
    } else {
      showAlert(`Diagnostic API Error (${response.status}): ${report.message || 'Diagnostic failed'}`, "error");
      setAiLoadingState(false);
    }

  } catch (netErr) {
    clearTimeout(timerStep2);
    clearTimeout(timerStep3);
    clearTimeout(timerStep4);
    showAlert(`Network communication error during AI diagnosis: ${netErr.message}`, "error");
    setAiLoadingState(false);
  } finally {
    setAiLoadingState(false, true);
  }
}

/**
 * Manages loading spinners and button states during AI analysis
 */
function setAiLoadingState(isLoading, keepResultsIfRendered = false) {
  if (triggerAiBtn) triggerAiBtn.disabled = isLoading;
  if (headerRunAiBtn) headerRunAiBtn.disabled = isLoading;
  if (analyzeTelemetryBtn) analyzeTelemetryBtn.disabled = isLoading;

  if (isLoading) {
    if (aiStatusBadge && aiStatusBadgeText) {
      aiStatusBadge.className = "ai-status-badge ai-in-progress";
      aiStatusBadgeText.textContent = "AI ANALYSIS: IN PROGRESS";
    }
    if (aiStandbyCard) aiStandbyCard.classList.add("hidden");
    if (aiResultsContainer) aiResultsContainer.classList.add("hidden");
    if (aiLoadingCard) aiLoadingCard.classList.remove("hidden");
    updatePipelineStep(1);
  } else {
    if (aiLoadingCard) aiLoadingCard.classList.add("hidden");
    if (!keepResultsIfRendered) {
      if (aiStandbyCard) aiStandbyCard.classList.remove("hidden");
    }
  }
}

/**
 * Renders the structured GenAI diagnostic sections into the Command Center
 */
function renderAiDiagnostic(diagnostic) {
  if (!diagnostic) return;

  // Show Results Container, Hide Standby & Loading Cards
  if (aiStandbyCard) aiStandbyCard.classList.add("hidden");
  if (aiLoadingCard) aiLoadingCard.classList.add("hidden");
  if (aiResultsContainer) aiResultsContainer.classList.remove("hidden");

  // Metadata Banner
  if (aiStatusPill) {
    aiStatusPill.className = "meta-tag tag-success";
    aiStatusPill.textContent = "COMPLETE";
  }

  if (aiModelBadge) {
    aiModelBadge.textContent = "Autonomous Diagnostic Engine";
  }

  if (aiTimestampBadge) {
    aiTimestampBadge.textContent = diagnostic.generatedAt ? diagnostic.generatedAt.substring(0, 19).replace("T", " ") + " UTC" : new Date().toISOString().substring(0, 19).replace("T", " ") + " UTC";
  }

  // Section 0: RETRIEVED MISSION KNOWLEDGE (RAG CONTEXT)
  const aiKnowledgeSourcesList = document.getElementById("aiKnowledgeSourcesList");
  if (aiKnowledgeSourcesList) {
    aiKnowledgeSourcesList.innerHTML = "";
    const sources = diagnostic.knowledgeSources || [];
    if (sources.length === 0) {
      aiKnowledgeSourcesList.innerHTML = `
        <div class="knowledge-empty-state">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#94A3B8" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          <span>Relevant technical knowledge was not confidently retrieved. Diagnostic proceeding using deterministic telemetry findings only.</span>
        </div>
      `;
    } else {
      sources.forEach(src => {
        const card = document.createElement("div");
        card.className = "knowledge-source-card";
        const scorePct = src.relevanceScore != null ? (src.relevanceScore * 100).toFixed(0) + "% match" : "Retrieved";
        card.innerHTML = `
          <div class="knowledge-source-header">
            <div class="source-title-group">
              <span class="source-doc-badge">${escapeHtml(src.documentName || 'knowledge-doc.md')}</span>
              <span class="source-topic-tag">${escapeHtml(src.relevantTopic || 'Aerospace Telemetry')}</span>
            </div>
            <span class="source-score-pill">${escapeHtml(scorePct)}</span>
          </div>
          <div class="source-preview-text">
            &ldquo;${escapeHtml(src.retrievedContentPreview || 'Technical context excerpt unavailable.')}&rdquo;
          </div>
        `;
        aiKnowledgeSourcesList.appendChild(card);
      });
    }
  }

  // Section 1: AI DIAGNOSTIC SUMMARY
  if (aiSummaryContent) {
    aiSummaryContent.textContent = diagnostic.diagnosticSummary || "Diagnostic summary unavailable.";
  }

  // Section 2: OBSERVED CONDITIONS
  if (aiObservedConditionsList) {
    aiObservedConditionsList.innerHTML = "";
    const conditions = diagnostic.observedConditions || [];
    if (conditions.length === 0) {
      aiObservedConditionsList.innerHTML = `<li>Telemetry parameters align within configured simulated baseline envelopes.</li>`;
    } else {
      conditions.forEach(item => {
        const li = document.createElement("li");
        li.textContent = item;
        aiObservedConditionsList.appendChild(li);
      });
    }
  }

  // Section 3: POSSIBLE CONTRIBUTING FACTORS
  if (aiContributingFactorsList) {
    aiContributingFactorsList.innerHTML = "";
    const factors = diagnostic.possibleContributingFactors || [];
    if (factors.length === 0) {
      aiContributingFactorsList.innerHTML = `<li>No anomalous environmental or hardware perturbations identified in the current telemetry window.</li>`;
    } else {
      factors.forEach(item => {
        const li = document.createElement("li");
        li.textContent = item;
        aiContributingFactorsList.appendChild(li);
      });
    }
  }

  // Section 4: AFFECTED SUBSYSTEMS
  if (aiAffectedSubsystemsContainer) {
    aiAffectedSubsystemsContainer.innerHTML = "";
    const subsystems = diagnostic.affectedSubsystems || [];
    if (subsystems.length === 0) {
      aiAffectedSubsystemsContainer.innerHTML = `
        <span class="subsystem-chip" style="border-color: rgba(52, 211, 153, 0.35); color: #34D399; background: rgba(52, 211, 153, 0.08);">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#34D399" stroke-width="2">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
          All Subsystems Nominal
        </span>
      `;
    } else {
      subsystems.forEach(subsystem => {
        const chip = document.createElement("span");
        chip.className = "subsystem-chip";
        chip.innerHTML = `
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
          </svg>
          ${escapeHtml(subsystem)}
        `;
        aiAffectedSubsystemsContainer.appendChild(chip);
      });
    }
  }

  // Section 5: RECOMMENDED INVESTIGATION
  if (aiRecommendedInvestigationList) {
    aiRecommendedInvestigationList.innerHTML = "";
    const recs = diagnostic.recommendedInvestigation || [];
    if (recs.length === 0) {
      aiRecommendedInvestigationList.innerHTML = `<li>Continue routine automated orbital state-of-health monitoring.</li>`;
    } else {
      recs.forEach(rec => {
        const li = document.createElement("li");
        li.textContent = rec;
        aiRecommendedInvestigationList.appendChild(li);
      });
    }
  }

  // Section 6: LIMITATIONS
  if (aiLimitationsList) {
    aiLimitationsList.innerHTML = "";
    const limits = diagnostic.limitations || [];
    if (limits.length === 0) {
      aiLimitationsList.innerHTML = `<li>Assessment represents a single synthetic telemetry frame snapshot without historical multi-orbit trending.</li>`;
    } else {
      limits.forEach(lim => {
        const li = document.createElement("li");
        li.textContent = lim;
        aiLimitationsList.appendChild(li);
      });
    }
  }
}

/**
 * Updates all Phase 2 analysis dashboard widgets with the deterministic result
 */
function renderAnalysisResult(result) {
  const status = result.overallStatus || "NOMINAL";

  // Update Header Health Badge
  healthStatusText.textContent = status;
  healthBadge.className = `health-status-badge badge-${status.toLowerCase()}`;

  // Update KPI Metrics
  kpiStatus.textContent = status;
  kpiStatus.className = `kpi-val status-${status.toLowerCase()}`;

  kpiAnomalyDetected.textContent = result.anomalyDetected ? "YES" : "NO";
  kpiAnomalyDetected.className = result.anomalyDetected ? "kpi-val status-critical" : "kpi-val status-nominal";

  kpiAnomalyCount.textContent = result.anomalyCount;
  kpiAnomalyCount.className = result.anomalyCount > 0 ? "kpi-val status-warning" : "kpi-val status-nominal";

  // Update Synthesis & Justification
  analysisSummaryText.textContent = result.summary || "No summary available.";
  statusExplanationText.textContent = `Status Justification: ${result.statusExplanation || 'Standard simulated limits.'}`;

  // Render Subsystem Anomalies Matrix
  anomaliesContainer.innerHTML = "";

  if (!result.detectedAnomalies || result.detectedAnomalies.length === 0) {
    // Render Nominal Card
    const nominalDiv = document.createElement("div");
    nominalDiv.className = "nominal-state-card";
    nominalDiv.innerHTML = `
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#34D399" stroke-width="2">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
          <polyline points="22 4 12 14.01 9 11.01"></polyline>
      </svg>
      <div>
          <div style="font-weight: 700; color: #34D399;">All Monitored Subsystems Operating Nominally</div>
          <div style="font-size: 0.82rem; color: var(--text-secondary); margin-top: 0.2rem;">
              Thermal (&le; 45°C), Array Voltage (26V&ndash;35V), ADCS Pointing (&le; 0.05°), and Dosimetry (&le; 5 µSv/h) are within standard simulated thresholds.
          </div>
      </div>
    `;
    anomaliesContainer.appendChild(nominalDiv);
  } else {
    // Render individual anomaly comparison cards
    result.detectedAnomalies.forEach((anomaly) => {
      const card = document.createElement("div");
      const isCritical = anomaly.severity === "CRITICAL";
      card.className = `anomaly-card ${isCritical ? 'anomaly-critical' : 'anomaly-warning'}`;

      card.innerHTML = `
        <div class="anomaly-card-header">
            <div class="anomaly-param-title">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="${isCritical ? '#F87171' : '#FBBF24'}" stroke-width="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <span>${escapeHtml(anomaly.parameter)}</span>
            </div>
            <div style="display: flex; gap: 0.5rem; align-items: center;">
                <span class="card-badge">${escapeHtml(anomaly.anomalyType)}</span>
                <span class="${isCritical ? 'severity-badge-critical' : 'severity-badge-warning'}">${escapeHtml(anomaly.severity)}</span>
            </div>
        </div>

        <div class="anomaly-comparison-grid">
            <div class="comparison-field">
                <span class="comparison-label">Observed Telemetry</span>
                <span class="comparison-val breached">${anomaly.observedValue}</span>
            </div>
            <div class="comparison-field">
                <span class="comparison-label">Configured Threshold</span>
                <span class="comparison-val">${escapeHtml(anomaly.threshold)}</span>
            </div>
            <div class="comparison-field">
                <span class="comparison-label">Subsystem Impact</span>
                <span class="comparison-val" style="color: ${isCritical ? '#F87171' : '#FBBF24'};">${isCritical ? 'High Risk' : 'Caution'}</span>
            </div>
        </div>

        <div class="anomaly-explanation-box">
            <strong>Deterministic Reason:</strong> ${escapeHtml(anomaly.explanation)}
        </div>
      `;
      anomaliesContainer.appendChild(card);
    });
  }
}

/**
 * Escapes HTML characters for secure rendering
 */
function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

/**
 * Resets telemetry inputs, instruments, deterministic analysis, and AI diagnostic to standby
 */
function resetTelemetry() {
  rawJsonInput.value = "";
  profileSelect.selectedIndex = 0;
  hideAlert();

  // Reset live instruments
  valSatelliteId.textContent = "--";
  valTimestamp.textContent = "--";

  valBatteryTemp.textContent = "--";
  valBatteryTemp.className = "telemetry-num";
  barBattery.style.width = "0%";

  valSolarVoltage.textContent = "--";
  valSolarVoltage.className = "telemetry-num";
  barSolar.style.width = "0%";

  valAttitudeX.textContent = "--";
  valAttitudeX.className = "telemetry-num";
  barAttitudeX.style.width = "0%";

  valAttitudeY.textContent = "--";
  valAttitudeY.className = "telemetry-num";
  barAttitudeY.style.width = "0%";

  valRadiation.textContent = "--";
  valRadiation.className = "telemetry-num";
  barRadiation.style.width = "0%";

  // Reset Phase 2 Analysis Dashboard
  healthStatusText.textContent = "NOMINAL";
  healthBadge.className = "health-status-badge badge-nominal";

  kpiStatus.textContent = "NOMINAL";
  kpiStatus.className = "kpi-val status-nominal";
  kpiAnomalyDetected.textContent = "NO";
  kpiAnomalyDetected.className = "kpi-val";
  kpiAnomalyCount.textContent = "0";
  kpiAnomalyCount.className = "kpi-val";

  analysisSummaryText.textContent = "Select a simulated profile and click \"Analyze Telemetry\" to evaluate numerical rules against telemetry thresholds.";
  statusExplanationText.textContent = "Status Rule: Evaluates pure mathematical limits across Thermal, Dosimetry, ADCS, and Power subsystems.";

  anomaliesContainer.innerHTML = `
    <div class="nominal-state-card">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#34D399" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
        </svg>
        <div>
            <div style="font-weight: 700; color: #34D399;">All Subsystems Nominal</div>
            <div style="font-size: 0.82rem; color: var(--text-secondary); margin-top: 0.2rem;">
                Battery Temperature (&le; 45°C), Voltage (26V&ndash;35V), ADCS Error Magnitude (&le; 0.05°), and Radiation Flux (&le; 5 µSv/h) are within standard operating boundaries.
            </div>
        </div>
    </div>
  `;

  // Reset Phase 3 GenAI Command Center
  if (aiStatusBadge && aiStatusBadgeText) {
    aiStatusBadge.className = "ai-status-badge";
    aiStatusBadgeText.textContent = "AI ANALYSIS: STANDBY";
  }

  if (aiResultsContainer) aiResultsContainer.classList.add("hidden");
  if (aiLoadingCard) aiLoadingCard.classList.add("hidden");
  if (aiStandbyCard) aiStandbyCard.classList.remove("hidden");

  const aiKnowledgeSourcesList = document.getElementById("aiKnowledgeSourcesList");
  if (aiKnowledgeSourcesList) aiKnowledgeSourcesList.innerHTML = "";
  updatePipelineStep(1);

  // Reset Phase 6 Mission Report State
  lastDiagnosticReport = null;
  setReportButtonsEnabled(false);

  showAlert("Telemetry display, deterministic engine, and AI diagnostic reset to standby.", "info");
}

/**
 * Phase 6: Automated Mission Diagnostic Report Generation Workflow.
 * Sends the current in-memory diagnostic report to POST /api/reports/diagnostic
 * and triggers a direct browser download of the generated PDF.
 */
async function generateMissionReport() {
  if (!lastDiagnosticReport) {
    showAlert("Please run an AI Diagnostic first before generating the mission report.", "warning");
    return;
  }

  const btn = document.getElementById("generateReportBtn");
  const btnText = document.getElementById("generateReportBtnText");
  const bannerBtn = document.getElementById("generateReportBannerBtn");
  const bannerBtnText = document.getElementById("generateReportBannerBtnText");

  // Show "GENERATING MISSION REPORT..." UI feedback
  if (btn) btn.disabled = true;
  if (btnText) btnText.textContent = "GENERATING MISSION REPORT...";
  if (bannerBtn) bannerBtn.disabled = true;
  if (bannerBtnText) bannerBtnText.textContent = "GENERATING PDF REPORT...";

  showAlert("Generating official satellite mission diagnostic PDF report...", "info");

  try {
    const response = await fetch("/api/reports/diagnostic", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(lastDiagnosticReport)
    });

    if (!response.ok) {
      throw new Error(`Server returned HTTP ${response.status}`);
    }

    // Extract filename from Content-Disposition header if available
    const satId = lastDiagnosticReport.analysis?.satelliteId || lastDiagnosticReport.telemetry?.satelliteId || "vehicle";
    let filename = `stellarintel-diagnostic-report-${satId.replace(/[^a-zA-Z0-9_-]/g, '_')}.pdf`;
    const disposition = response.headers.get("Content-Disposition");
    if (disposition && disposition.indexOf("filename=") !== -1) {
      const matches = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/.exec(disposition);
      if (matches != null && matches[1]) {
        filename = matches[1].replace(/['"]/g, '');
      }
    }

    const blob = await response.blob();
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = downloadUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);

    // Show "REPORT GENERATED" UI feedback
    if (btnText) btnText.textContent = "REPORT GENERATED";
    if (bannerBtnText) bannerBtnText.textContent = "REPORT DOWNLOADED";
    showAlert(`Mission Report [${filename}] generated and downloaded successfully.`, "success");

    setTimeout(() => {
      if (btn) btn.disabled = false;
      if (btnText) btnText.textContent = "GENERATE MISSION REPORT";
      if (bannerBtn) bannerBtn.disabled = false;
      if (bannerBtnText) bannerBtnText.textContent = "EXPORT REPORT (PDF)";
    }, 3000);

  } catch (err) {
    console.error("Mission report generation failed:", err);
    // Show "REPORT GENERATION FAILED" UI feedback
    if (btnText) btnText.textContent = "REPORT GENERATION FAILED";
    if (bannerBtnText) bannerBtnText.textContent = "GENERATION FAILED";
    showAlert("Failed to generate mission report PDF. Please check connection and try again.", "error");

    setTimeout(() => {
      if (btn) btn.disabled = false;
      if (btnText) btnText.textContent = "GENERATE MISSION REPORT";
      if (bannerBtn) bannerBtn.disabled = false;
      if (bannerBtnText) bannerBtnText.textContent = "EXPORT REPORT (PDF)";
    }, 3000);
  }
}

// Event Listeners
loadProfileBtn.addEventListener("click", loadSelectedProfile);
profileSelect.addEventListener("change", loadSelectedProfile);
parseTelemetryBtn.addEventListener("click", parseTelemetry);
analyzeTelemetryBtn.addEventListener("click", analyzeTelemetry);
resetTelemetryBtn.addEventListener("click", resetTelemetry);

// Phase 3 GenAI Diagnostic Triggers
if (triggerAiBtn) {
  triggerAiBtn.addEventListener("click", runAiDiagnostic);
}
if (headerRunAiBtn) {
  headerRunAiBtn.addEventListener("click", runAiDiagnostic);
}

// Phase 6 Mission Diagnostic Report Triggers
if (generateReportBtn) {
  generateReportBtn.addEventListener("click", generateMissionReport);
}
if (generateReportBannerBtn) {
  generateReportBannerBtn.addEventListener("click", generateMissionReport);
}

// On page load, auto-load and analyze the first nominal profile for instant user experience
document.addEventListener("DOMContentLoaded", () => {
  if (profileSelect && profileSelect.options.length > 0) {
    profileSelect.value = "nominal-orbit";
    loadSelectedProfile();
    analyzeTelemetry();
  }
});
