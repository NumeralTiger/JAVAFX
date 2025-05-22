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
            // Normalize the collision normal
            Vector2D collisionNormal = normal.scale(1.0 / dist);

            // Calculate relative velocity
            Vector2D relativeVelocity = body2.getVelocity().subtract(body1.getVelocity());

            // Calculate relative velocity along normal
            double normalVelocity = relativeVelocity.dotProduct(collisionNormal);

            // Only proceed if objects are moving toward each other
            if (normalVelocity < 0) {
                // Calculate restitution coefficient based on masses
                // Larger mass differences result in more energy loss
                double massRatio = Math.min(body1.getMass(), body2.getMass()) / 
                                 Math.max(body1.getMass(), body2.getMass());
                double restitution = Constants.COLLISION_ELASTICITY * massRatio;

                // Calculate impulse scalar
                double j = -(1 + restitution) * normalVelocity;
                j /= (1 / body1.getMass() + 1 / body2.getMass());

                // Apply impulse
                Vector2D impulse = collisionNormal.scale(j);
                
                // Update velocities based on conservation of momentum
                body1.setVelocity(body1.getVelocity().subtract(impulse.scale(1 / body1.getMass())));
                body2.setVelocity(body2.getVelocity().add(impulse.scale(1 / body2.getMass())));

                // Separate the bodies to prevent sticking
                double overlap = (body1.getRadius() + body2.getRadius()) - dist;
                Vector2D separation = collisionNormal.scale(overlap * 0.5);
                body1.setPosition(body1.getPosition().subtract(separation));
                body2.setPosition(body2.getPosition().add(separation));
            }
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
        for (int i = 0; i < bodies.size(); i++) {
            Body body1 = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                Body body2 = bodies.get(j);
                handleElasticCollision(body1, body2);
            }
        }
    }
    
}