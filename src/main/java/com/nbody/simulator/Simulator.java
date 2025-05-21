package com.nbody.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orchestrates the N-body simulation steps.
 */
public class Simulator {
    private List<Body> bodies;
    private PhysicsEngine physicsEngine;
    private double simulationTime;

    public Simulator() {
        this.bodies = new ArrayList<>();
        this.physicsEngine = new PhysicsEngine();
        this.simulationTime = 0.0;
    }

    /**
     * Adds a body to the simulation.
     * @param body The body to add.
     */
    public void addBody(Body body) {
        this.bodies.add(body);
    }

    /**
     * Removes a body from the simulation.
     * @param body The body to remove.
     */
    public void removeBody(Body body) {
        this.bodies.remove(body);
    }

    /**
     * Returns an unmodifiable list of bodies in the simulation.
     * @return A list of bodies.
     */
    public List<Body> getBodies() {
        return Collections.unmodifiableList(bodies);
    }

    /**
     * Performs one step of the simulation.
     * @param deltaTime The time elapsed for this step.
     */
    public void update(double deltaTime) {
        // 1. Calculate gravitational forces and update accelerations
        // This is done BEFORE updating positions/velocities using the current positions
        physicsEngine.calculateGravitationalForces(bodies);

        // 2. Update positions and velocities for ALL bodies.
        // This implicitly uses the new accelerations calculated in the previous step.
        // NOTE: Body.update() now includes the position and velocity integration.
        for (Body body : bodies) {
            body.update(deltaTime);
        }

        // 3. Detect and resolve collisions (which might remove bodies)
        physicsEngine.detectAndResolveCollisions(bodies); // This might modify the 'bodies' list

        simulationTime += deltaTime;
    }


    /**
     * Resets the simulation, clearing all bodies.
     */
    public void reset() {
        bodies.clear();
        simulationTime = 0.0;
    }

    public double getSimulationTime() {
        return simulationTime;
    }
}