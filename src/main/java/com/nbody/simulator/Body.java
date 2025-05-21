package com.nbody.simulator;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a celestial body in the N-body simulation.
 */
public class Body {
    private String id; // Unique identifier for the body
    private double mass; // Mass of the body
    private double radius; // Radius for collision detection and rendering
    private Color color; // Color for rendering
    private Vector2D position; // Current position
    private Vector2D velocity; // Current velocity
    private Vector2D acceleration; // Current acceleration (calculated by physics engine)


    // Trail data
    private final List<Vector2D> trail;
    private static final int MAX_TRAIL_LENGTH = 200; // Max points in the trail
    private Vector2D oldPosition; // Needed for Leapfrog integration

    public Body(String id, double mass, double radius, Color color, Vector2D position, Vector2D velocity) {
        this.id = id;
        this.mass = mass;
        this.radius = radius;
        this.color = color;
        this.position = position;
        this.velocity = velocity;
        this.acceleration = new Vector2D(0, 0); // Initialize acceleration to zero
        this.oldPosition = position.subtract(velocity.scale(Constants.TIME_STEP)); // Initial guess for oldPosition
        this.trail = new ArrayList<>();
        this.trail.add(position);
    }
    public void setMass(double mass) {
        this.mass = mass;
    }

    //region Getters and Setters

    public String getId() {
        return id;
    }

    public double getMass() {
        return mass;
    }

    public double getRadius() {
        return radius;
    }

    public Color getColor() {
        return color;
    }

    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public Vector2D getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector2D acceleration) {
        this.acceleration = acceleration;
    }

    public List<Vector2D> getTrail() {
        return trail;
    }

    //endregion

    /**
     * Updates the body's position and velocity based on its current acceleration
     * and a given time step.
     * Uses Euler integration for simplicity. You will improve this later.
     * @param deltaTime The time step for the simulation.
     */
    public void update(double deltaTime) {
        // Leapfrog / Velocity Verlet Integration Steps:
        // 1. Calculate new velocity (v_new = v_old + a * dt)
        Vector2D newVelocity = velocity.add(acceleration.scale(deltaTime));

        // 2. Calculate new position (p_new = p_old + v_new * dt) - using new velocity
        Vector2D newPosition = position.add(newVelocity.scale(deltaTime));

        // Update properties
        this.oldPosition = this.position; // Store current position as old for next step
        this.position = newPosition;
        this.velocity = newVelocity;

        // Add current position to trail
        trail.add(position);
        if (trail.size() > Constants.MAX_TRAIL_LENGTH) { // Use constant here
            trail.remove(0); // Remove oldest point if trail is too long
        }
    }

    /**
     * Resets the body's acceleration to zero before new forces are calculated.
     */
    public void resetAcceleration() {
        this.acceleration = new Vector2D(0, 0);
    }

    @Override
    public String toString() {
        return "Body{" +
               "id='" + id + '\'' +
               ", mass=" + mass +
               ", radius=" + radius +
               ", pos=" + position +
               ", vel=" + velocity +
               '}';
    }
}