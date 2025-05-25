package com.nbody.simulator;

/**
 * Holds common constants for the N-body simulator.
 */
public final class Constants {
    // --- SCALED VALUES FOR SOLAR SYSTEM SIMULATION ---

    // Gravitational constant (adjust for your chosen units!)
    // If 1 distance unit = 1 AU, 1 mass unit = 1 Solar Mass, 1 time unit = 1 Earth Year
    // Then G can be set to approximately 4 * PI^2
    // A common value used for scaled G, where AU/year/solar_mass are units:

    public static double GRAVITATIONAL_CONSTANT = 39.478; // Approx 4 * PI^2 (AU^3 / (SolarMass * Year^2))

    public static double BASE_GRAVITATIONAL_CONSTANT = 39.478; //original value

    // Minimum distance squared between bodies to avoid division by zero
    // For a solar system, this should be small relative to orbital distances,
    // but large enough to prevent extreme forces at very close approaches.
    // If 1 AU is 1 unit, then 0.0000001 AU is a reasonable "too close" distance.
    public static final double MIN_DISTANCE_SQ = 0.0001 * 0.0001; // AU units

    // Collision elasticity
    public static final double COLLISION_ELASTICITY = 0.5; // Still allows some bounce

    // Time calc: How much simulation time passes per update
    // If 1 time unit = 1 Earth Year, then 0.001 would be about 0.365 days per step.
    public static final double TIME_STEP = 0.0005; // In Earth Years per simulation step

    // UI related constants
    public static final double SUN_DISPLAY_RADIUS_MULTIPLIER = 50000.0; // Sun
    public static final double PLANET_DISPLAY_RADIUS_MULTIPLIER = 1000000.0; // planets
        // Visual & Trail Constants
    public static final int MAX_TRAIL_LENGTH = 1000; 
    public static final double TRAIL_STROKE_WIDTH = 0;
    public static final double RING_THICKNESS = 0.015; // Thickness for Saturn's ring

    // Camera Constants
    public static final double CAMERA_INITIAL_Z = -40; // camera initially
    public static final double CAMERA_FAR_CLIP = 50000.0; // clip distance
    
    // Prevent instantiation
    private Constants() {}
}