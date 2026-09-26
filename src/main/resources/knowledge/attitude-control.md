# Attitude Determination and Control Systems (ADCS): Momentum Management & Pointing Stability

## Topic
Reaction Wheel Assemblies (RWA), Angular Pointing Error Vectors, and Disturbance Torques

## Educational Context Notice
This technical reference document is curated for the StellarIntel Academic Satellite Telemetry Analysis AI Platform. The operational thresholds and subsystem descriptions represent educational simulation models and must not be interpreted as official operational flight rules for active NASA, ESA, or commercial spacecraft.

## Technical Explanation
The Attitude Determination and Control Subsystem (ADCS) maintains 3-axis orbital orientation (pitch, roll, yaw) for spacecraft communications, scientific payloads, and solar array sun-pointing. The system operates via a continuous closed-loop architecture:
1. **Sensors:** Star trackers, digital sun sensors, Earth limb sensors, and inertial measurement units (IMUs / gyroscopes) measure the spacecraft's inertial attitude and angular rates.
2. **Estimation:** An onboard extended Kalman filter (EKF) fuses sensor readings to compute the current attitude quaternion relative to the desired target pointing frame.
3. **Actuation:** Reaction Wheel Assemblies (RWAs) accelerate or decelerate internal flywheel rotors to exchange angular momentum with the spacecraft bus via Newton's third law.

When external disturbance torques (such as solar radiation pressure, residual atmospheric drag in LEO, gravity-gradient torque, or magnetic dipole interactions) act on the satellite, the reaction wheels continuously accelerate to counteract the perturbation, accumulating stored angular momentum.

## Relevant Subsystem Concepts
- **Pointing Error Magnitude:** The composite angular deviation between the spacecraft payload bore-sight (or communication antenna) and the target vector: $\theta_{err} = \sqrt{\theta_X^2 + \theta_Y^2}$.
- **Reaction Wheel Assembly (RWA):** Precision electromechanical flywheels operating at speeds typically between 500 RPM and 6,000 RPM.
- **Wheel Momentum Saturation:** When a flywheel reaches its maximum design rotational velocity, it can no longer produce control torque in that rotational axis.
- **Momentum Desaturation (Momentum Dump):** The process of firing attitude control thrusters or energizing magnetic torque rods (MTRs) against Earth's geomagnetic field to dump accumulated wheel angular momentum without disturbing vehicle pointing.

## Typical Relationships Between Telemetry Parameters
- **ADCS Pointing Errors (X and Y Axes):** In fine-pointing mode, angular errors along X (pitch) and Y (yaw) should remain tightly bounded (e.g., $|\theta_X|, |\theta_Y| \le 0.050^\circ$).
- **Pointing Deviation and Communication Link:** Angular pointing errors exceeding 1.0° typically result in severe degradation of directional high-gain antenna (X-band/Ka-band) carrier lock and signal-to-noise ratio (SNR).
- **Pointing Error and Power Generation:** Extreme attitude misalignment alters solar array cosine incidence, resulting in diminished solar bus voltage and reduced battery charging.

## General Interpretation Guidance
- **Fine Pointing Envelope (Nominal):** Vector error $\le 0.050^\circ$. Indicates optimal closed-loop control and nominal flywheel speed margins.
- **Warning Deviation (e.g., > 0.050° to 0.500°):** Indicates attitude transient recovery, recent slew completion, unmodeled external disturbance, or star tracker optical glare/occlusion.
- **Critical Anomaly (e.g., > 0.500° to multi-degree errors):** Represents a significant attitude control disruption. Potential causes include:
  1. Reaction wheel momentum saturation (wheel running at maximum tachometer RPM).
  2. Mechanical bearing drag or electromechanical driver motor anomaly.
  3. Star tracker loss-of-lock or Earth horizon sensor blinding.
  4. Large external torque impulse (e.g., micrometeoroid impact or propellant leak).

## Critical Diagnostic Rule & Language Cautions
Do **NOT** definitively declare that a reaction wheel has physically failed based solely on pointing error telemetry. Pointing divergence establishes the presence of an attitude-control anomaly. Determining whether the underlying cause is wheel bearing friction, momentum saturation, star tracker glare, or magnetic torquer timing requires tachometer speed data, motor current telemetry, and sensor validity flags.

## Limitations & Uncertainty
Single-frame telemetry provides pointing error components ($X, Y$) but lacks individual reaction wheel tachometer speeds, motor current draws, star tracker quaternion residuals, and magnetic torque rod activation states.
