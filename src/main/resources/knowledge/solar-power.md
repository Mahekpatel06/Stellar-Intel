# Photovoltaic Array & Power Distribution Subsystem (EPS): Voltage Regulation and Bus Health

## Topic
Solar Array Bus Voltage, Shunt Limiters, and Eclipse Transits

## Educational Context Notice
This technical reference document is curated for the StellarIntel Academic Satellite Telemetry Analysis AI Platform. The operational thresholds and subsystem descriptions represent educational simulation models and must not be interpreted as official operational flight rules for active NASA, ESA, or commercial spacecraft.

## Technical Explanation
The Electrical Power Subsystem (EPS) generates, conditions, stores, and distributes electrical power to all satellite avionics and mission payloads. Photovoltaic solar array wings composed of high-efficiency multi-junction gallium-arsenide (GaAs) solar cells convert solar photon irradiance into direct electrical current.

In a regulated power bus architecture, the solar array output voltage is conditioned by a Sequential Shunt Limiter (SSL) or Maximum Power Point Tracking (MPPT) electronic power conditioner. The EPS provides power along a regulated main voltage bus (simulated nominal band: 26.0 V to 35.0 V DC) that concurrently supplies vehicle avionics and powers the Battery Charge and Discharge Regulators (BCDR).

## Relevant Subsystem Concepts
- **Direct Energy Transfer (DET):** Power architecture where solar arrays connect directly to the main bus via shunt regulators that dissipate excess power as heat when batteries are fully charged.
- **Maximum Power Point Tracking (MPPT):** DC-DC power conversion that dynamically adjusts input impedance to extract maximum electrical power from the solar arrays under varying temperatures and illumination angles.
- **Bus Regulation Limits:** Upper voltage cutoff prevents overvoltage stress on sensitive downstream DC-DC converters; lower voltage limit prevents battery over-discharge during high vehicle load or extended eclipses.
- **Solar Aspect Angle (SAA):** The angle between the solar array normal vector and the sun vector. Power generated varies with $\cos(\text{SAA})$.

## Typical Relationships Between Telemetry Parameters
- **Solar Bus Voltage and Orbital Eclipse State:** In sunlit flight phases, bus voltage remains at the array regulation plateau (typically 28.0 V to 34.0 V). Upon orbital sunset (umbra entry), array power ceases immediately, and bus voltage transitions to the battery discharge plateau (typically 24.0 V to 28.0 V).
- **Solar Bus Voltage and Battery Temperature:** High bus voltages (> 34.0 V) during end-of-charge states force shunt regulators to dump excess current, elevating radiator temperature and radiant heating around the battery bay.

## General Interpretation Guidance
- **Nominal Operating Band (e.g., 26.0 V to 35.0 V):** Indicates healthy power balance between solar generation, battery buffer storage, and vehicle load consumption.
- **Undervoltage Condition (< 26.0 V):** Suggests severe solar array shadowing, failure of an array string, damaged deployment hinge mechanism, or an excessive electrical bus short/overload.
- **Overvoltage Condition (> 35.0 V):** Indicates failure of shunt limiters or MPPT regulation circuitry to clamp open-circuit array voltages, placing avionics downstream at risk of electrical overstress.

## Limitations & Uncertainty
Single-frame telemetry shows snapshot bus voltage but lacks solar array string current telemetry, individual shunt circuit switch states, battery charge regulator currents, and precise orbital eclipse entry/exit timers.
