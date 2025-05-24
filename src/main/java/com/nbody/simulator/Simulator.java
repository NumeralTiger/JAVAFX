package com.nbody.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Simulator {
    private List<Body> bodies;
    private PhysicsEngine physicsEngine;
    private double simulationTime;
    private List<Body> bodiesToRemoveNextStep;

    public Simulator() {
        this.bodies = new ArrayList<>();
        this.physicsEngine = new PhysicsEngine();
        this.simulationTime = 0.0;
        this.bodiesToRemoveNextStep = new ArrayList<>();
    }

    public void addBody(Body body) {
        if (bodies.stream().noneMatch(b -> b.getId().equals(body.getId()))) {
            this.bodies.add(body);
        } else {
            System.err.println("Warning: Body with ID " + body.getId() + " already exists. Not adding.");
        }
    }
    
    public Optional<Body> getBodyById(String id) {
        return bodies.stream().filter(b -> b.getId().equals(id)).findFirst();
    }

    public List<Body> getBodies() {
        return Collections.unmodifiableList(bodies);
    }

    public void update(double deltaTime) {
        if (bodies.isEmpty()) return;
        physicsEngine.calculateGravitationalForcesAndHandleCollisions(bodies, bodiesToRemoveNextStep);
        for (Body body : bodies) {
            if (!bodiesToRemoveNextStep.contains(body)) {
                body.update(deltaTime);
            }
        }
        simulationTime += deltaTime;
    }
    
    public List<String> processRemovals() {
        List<String> removedIds = new ArrayList<>();
        if (!bodiesToRemoveNextStep.isEmpty()) {
            for (Body bodyToRemove : bodiesToRemoveNextStep) {
                bodies.remove(bodyToRemove);
                removedIds.add(bodyToRemove.getId());
            }
            bodiesToRemoveNextStep.clear();
        }
        return removedIds;
    }

    public void reset() {
        bodies.clear();
        bodiesToRemoveNextStep.clear();
        simulationTime = 0.0;
    }

    public double getSimulationTime() {
        return simulationTime;
    }
}
