package com.nbody.simulator;

import java.util.List;
import java.util.Iterator;

/**
 * Calculates gravitational forces and handles basic physics interactions.
 */
public class PhysicsEngine {

    /**
     * Calculates the gravitational forces between all bodies and updates their accelerations.
     * Uses Newton's Law of Universal Gravitation.
     * @param bodies The list of bodies in the simulation.
     */
    public void calculateGravitationalForces(List<Body> bodies) {
        // Reset accelerations for all bodies
        for (Body body : bodies) {
            body.resetAcceleration();
        }

        // Calculate forces between all unique pairs of bodies
        for (int i = 0; i < bodies.size(); i++) {
            Body body1 = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                Body body2 = bodies.get(j);

                // Calculate distance vector between bodies
                Vector2D r = body2.getPosition().subtract(body1.getPosition());
                double distanceSq = r.magnitudeSq();

                // Prevent division by zero if bodies are exactly at the same position
                if (distanceSq < Constants.MIN_DISTANCE_SQ) {
                    distanceSq = Constants.MIN_DISTANCE_SQ;
                }

                double distance = Math.sqrt(distanceSq);

                // Calculate gravitational force magnitude
                // F = G * (m1 * m2) / r^2
                double forceMagnitude = Constants.GRAVITATIONAL_CONSTANT * body1.getMass() * body2.getMass() / distanceSq;

                // Calculate force vector (direction and magnitude)
                Vector2D force = r.normalize().scale(forceMagnitude);

                // Apply force to accelerations (F = ma => a = F/m)
                // Body1 experiences force towards Body2
                body1.setAcceleration(body1.getAcceleration().add(force.scale(1.0 / body1.getMass())));
                // Body2 experiences force towards Body1 (equal and opposite)
                body2.setAcceleration(body2.getAcceleration().add(force.scale(-1.0 / body2.getMass())));
            }
        }
    }

    /**
     * Handles elastic collisions between bodies.
     * This is a simplified 2D elastic collision model.
     * @param body1 The first body.
     * @param body2 The second body.
     */
    public void handleElasticCollision(Body body1, Body body2) {
        // Distance between centers
        Vector2D normal = body2.getPosition().subtract(body1.getPosition());
        double dist = normal.magnitude();

        // Check for overlap
        if (dist < (body1.getRadius() + body2.getRadius())) {
            // Collision detected!

            // 1. Move bodies apart to prevent sticking (simple separation along normal)
            double overlap = (body1.getRadius() + body2.getRadius()) - dist;
            Vector2D separationVector = normal.normalize().scale(overlap * 0.5); // Move each body half the overlap
            body1.setPosition(body1.getPosition().subtract(separationVector));
            body2.setPosition(body2.getPosition().add(separationVector));


            // 2. Relative velocity
            Vector2D relativeVelocity = body1.getVelocity().subtract(body2.getVelocity());

            // 3. Normal vector (direction of impact)
            Vector2D collisionNormal = normal.normalize();

            // 4. Relative velocity along the normal
            double velocityAlongNormal = relativeVelocity.dotProduct(collisionNormal);

            // Do not resolve if velocities are separating
            if (velocityAlongNormal > 0) {
                return; // Bodies are already moving apart
            }

            // 5. Impulse scalar
            // Formula for 2D elastic collision impulse
            double impulseScalar = -(1 + Constants.COLLISION_ELASTICITY) * velocityAlongNormal /
                                   (1 / body1.getMass() + 1 / body2.getMass());

            // 6. Impulse vector
            Vector2D impulse = collisionNormal.scale(impulseScalar);

            // 7. Apply impulse to velocities
            body1.setVelocity(body1.getVelocity().add(impulse.scale(1 / body1.getMass())));
            body2.setVelocity(body2.getVelocity().subtract(impulse.scale(1 / body2.getMass())));

            // Optional: for small overlaps or floating point issues, you might need to slightly
            // adjust positions again or add a small repulsive force.
        }
    }

    /**
     * Updates the position and velocity of all bodies for a given time step.
     * @param bodies The list of bodies.
     * @param deltaTime The time step.
     */
    public void updateBodyPositionsAndVelocities(List<Body> bodies, double deltaTime) {
        for (Body body : bodies) {
            body.update(deltaTime);
        }
    }

    /**
     * Detects and resolves collisions between all bodies.
     * @param bodies The list of bodies.
     */
    public void detectAndResolveCollisions(List<Body> bodies) {
        // Use an iterator for safe removal of bodies during iteration
        for (int i = 0; i < bodies.size(); i++) {
            Body body1 = bodies.get(i);
            if (body1 == null) continue; // Skip if body was removed

            for (int j = i + 1; j < bodies.size(); j++) {
                Body body2 = bodies.get(j);
                if (body2 == null) continue; // Skip if body was removed

                // Check distance for collision
                double distanceSq = body1.getPosition().distanceSq(body2.getPosition());
                double combinedRadii = body1.getRadius() + body2.getRadius();

                if (distanceSq < combinedRadii * combinedRadii) {
                    // Collision detected!
                    // Determine which body is larger (usually the "star")
                    Body largerBody = (body1.getMass() >= body2.getMass()) ? body1 : body2;
                    Body smallerBody = (body1.getMass() < body2.getMass()) ? body1 : body2;

                    // Option 1: Simulate subsumption (smaller body absorbed by larger)
                    // This is more realistic for a small asteroid hitting the sun.
                    // The larger body's mass and momentum would increase.
                    // The smaller body is "destroyed" (removed from simulation).

                    // Update larger body's properties (conservation of momentum)
                    Vector2D newVelocity = (largerBody.getVelocity().scale(largerBody.getMass())
                                         .add(smallerBody.getVelocity().scale(smallerBody.getMass())))
                                         .scale(1.0 / (largerBody.getMass() + smallerBody.getMass()));
                    largerBody.setVelocity(newVelocity);
                    largerBody.setMass(largerBody.getMass() + smallerBody.getMass());
                    // Optionally: increase radius of larger body based on new mass (e.g., radius ~ mass^(1/3))
                    // largerBody.setRadius(Math.pow(largerBody.getMass(), 1.0/3.0) * some_scale_factor);

                    // Mark smaller body for removal. Important: cannot remove directly from `bodies` list
                    // while iterating with index-based for loops. Set to null and clean up later.
                    if (body1 == smallerBody) {
                        bodies.set(i, null); // Set to null instead of removing to avoid index shifting
                    } else {
                        bodies.set(j, null);
                    }
                    System.out.println(smallerBody.getId() + " was subsumed by " + largerBody.getId());
                }
            }
            bodies.removeIf(b -> b == null);
        }
    }
}