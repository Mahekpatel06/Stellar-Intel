# Satellite Telemetry Ingestion and State-of-Health (SOH) Monitoring Principles

## Topic
Multi-Subsystem Correlation, Telemetry Frame Structures, and Nominal Envelope Verification

## Educational Context Notice
This technical reference document is curated for the StellarIntel Academic Satellite Telemetry Analysis AI Platform. The operational thresholds and subsystem descriptions represent educational simulation models and must not be interpreted as official operational flight rules for active NASA, ESA, or commercial spacecraft.

## Technical Explanation
Satellite operations rely entirely on downlinked house-keeping telemetry frames to reconstruct vehicle state-of-health (SOH) in near real-time. Onboard data acquisition systems convert analog voltages, thermistor resistances, shunt currents, digital encoder outputs, and software state flags into packetized binary telemetry streams (such as CCSDS Space Packet protocols) downlinked to ground station networks.

Deterministic ground software ingests these raw parameters, applies polynomial calibration curves to derive engineering units (e.g., °C, Volts, degrees, $\mu\text{Sv/h}$), and compares each parameter against predefined numerical operational safety boundaries (yellow warning limits and red critical limits).

## Relevant Subsystem Concepts
- **Deterministic Anomaly Detection:** Rigorous rule-based evaluation where observed telemetry is compared against immutable numerical limits. Deterministic analysis serves as the single source of truth for parameter compliance and health status.
- **Subsystem Interdependence:** Spacecraft subsystems do not operate in isolation. An anomaly in one subsystem frequently causes cascading secondary symptoms in interconnected subsystems:
  - Thermal Control (TCS) and Electrical Power (EPS) interact constantly through battery charging dissipation.
  - Attitude Control (ADCS) directly dictates solar array sun-pointing efficiency and radiator deep-space view factors.
  - Radiation flux affects Command & Data Handling (CDH) memory integrity and star tracker camera noise.
- **State-of-Health (SOH) Classification:**
  - **NOMINAL:** All monitored telemetry parameters remain strictly within standard simulated operational boundaries.
  - **WARNING:** Minor threshold excursions, deadband recovery, or elevated thermal/electrical values that warrant operator attention but pose no immediate vehicle loss-of-mission threat.
  - **CRITICAL:** High-severity boundary violations (extreme temperatures, major pointing divergence, hazardous radiation flux) requiring immediate ground intervention and procedural mitigation.

## General Interpretation Guidance
- **Nominal Operations:** When deterministic checks confirm zero anomalies, the GenAI diagnostic layer must verify that all subsystems operate within nominal envelopes and **must not fabricate or invent phantom problems**.
- **Multi-Anomaly Correlation:** When multiple concurrent anomalies occur (e.g., elevated radiation combined with attitude pointing error), ground operators must distinguish between independent physical root causes and coupled failure modes.
- **Cautious Investigative Language:** AI-assisted interpretations should provide plausible hypotheses grounded in technical context, clearly distinguishing known facts from speculative possibilities.

## Limitations & Uncertainty
Downlinked telemetry represents discrete time-sampled frames. Single-frame analysis cannot observe high-frequency transient oscillations occurring between telemetry sampling epochs without onboard circular event buffer dumps.
