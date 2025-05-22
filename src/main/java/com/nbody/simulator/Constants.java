package com.nbody.simulator;

/**
 * Holds common constants for the N-body simulator.
 */
public final class Constants {
    // --- SCALED VALUES FOR SOLAR SYSTEM SIMULATION ---

    // Gravitational constant (adjust for your chosen units!)
    // If 1 distance unit = 1 AU, 1 mass unit = 1 Solar Mass, 1 time unit = 1 Earth Year
    // Then G can be set to approximately 4 * PI^2
    // For visual simulation, you might still need to tweak this
    // A common value used for scaled G, where AU/year/solar_mass are units:
    public static final double GRAVITATIONAL_CONSTANT = 39.478; // Approx 4 * PI^2 (AU^3 / (SolarMass * Year^2))
    // Or you can find values used in other N-body solar system simulations online.

    // Minimum distance squared between bodies to avoid division by zero
    // For a solar system, this should be small relative to orbital distances,
    // but large enough to prevent extreme forces at very close approaches.
    // If 1 AU is 1 unit, then 0.001 AU is a reasonable "too close" distance.
    public static final double MIN_DISTANCE_SQ = 0.001 * 0.001; // ~ (150,000 km)^2 in AU units

    // Collision elasticity (keep for now, but will be modified for destruction)
    public static final double COLLISION_ELASTICITY = 0.5; // Still allows some bounce for other collisions

    // Time step: How much simulation time passes per update.
    // If 1 time unit = 1 Earth Year, then 0.001 would be about 0.365 days per step.
    public static final double TIME_STEP = 0.001; // In Earth Years per simulation step

    // UI/Rendering related constants (can be adjusted for visual clarity)
    public static final double SUN_DISPLAY_RADIUS_MULTIPLIER = 50.0; // To make the Sun visible, as its real size is tiny on an AU scale
    public static final double PLANET_DISPLAY_RADIUS_MULTIPLIER = 1000.0; // To make planets visible
    public static final int MAX_TRAIL_LENGTH = 500; // Longer trails for clearer orbits

    // Prevent instantiation
    private Constants() {}
}