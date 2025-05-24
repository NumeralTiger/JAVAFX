package com.nbody.simulator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PhysicsEngine {

    public List<Body> calculateGravitationalForcesAndHandleCollisions(List<Body> bodies, List<Body> bodiesToRemove) {
        for (Body body : bodies) {
            body.resetAcceleration();
        }
        double G = Constants.GRAVITATIONAL_CONSTANT;
        for (int i = 0; i < bodies.size(); i++) {
            Body body1 = bodies.get(i);
            if (bodiesToRemove.contains(body1)) continue;
            for (int j = i + 1; j < bodies.size(); j++) {
                Body body2 = bodies.get(j);
                if (bodiesToRemove.contains(body2)) continue;
                Vector2D r = body2.getPosition().subtract(body1.getPosition());
                double distanceSq = r.magnitudeSq();
                if (distanceSq < Constants.MIN_DISTANCE_SQ) {
                    distanceSq = Constants.MIN_DISTANCE_SQ;
                }
                double distance = Math.sqrt(distanceSq);
                if (distance < (body1.getRadius() + body2.getRadius())) {
                    handleMergeCollision(body1, body2, bodiesToRemove);
                    if (bodiesToRemove.contains(body1) || bodiesToRemove.contains(body2)) {
                        continue; 
                    }
                }
                double forceMagnitude = G * body1.getMass() * body2.getMass() / distanceSq;
                Vector2D force = r.normalize().scale(forceMagnitude);
                body1.setAcceleration(body1.getAcceleration().add(force.scale(1.0 / body1.getMass())));
                body2.setAcceleration(body2.getAcceleration().add(force.scale(-1.0 / body2.getMass())));
            }
        }
        return bodiesToRemove;
    }
    
    private void handleMergeCollision(Body body1, Body body2, List<Body> bodiesToRemove) {
        Body survivor, absorbed;
        if (body1.getMass() >= body2.getMass()) {
            survivor = body1;
            absorbed = body2;
        } else {
            survivor = body2;
            absorbed = body1;
        }
        if (bodiesToRemove.contains(survivor) || bodiesToRemove.contains(absorbed)) {
            return;
        }
        System.out.println("Collision! Merging " + absorbed.getId() + " into " + survivor.getId());
        Vector2D finalVelocity = survivor.getVelocity().scale(survivor.getMass())
                                 .add(absorbed.getVelocity().scale(absorbed.getMass()))
                                 .scale(1.0 / (survivor.getMass() + absorbed.getMass()));
        survivor.setMass(survivor.getMass() + absorbed.getMass());
        survivor.setVelocity(finalVelocity);
        if (!bodiesToRemove.contains(absorbed)) {
            bodiesToRemove.add(absorbed);
        }
    }
}