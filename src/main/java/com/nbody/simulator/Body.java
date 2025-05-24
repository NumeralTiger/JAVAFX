package com.nbody.simulator;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Body {
    private String id;
    private double mass;
    private double radius; 
    private Color color; 
    private Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration;
    private final List<Vector2D> trail;

    public Body(String id, double mass, double radius, Color color, Vector2D position, Vector2D velocity) {
        this.id = id;
        this.mass = mass;
        this.radius = radius; 
        this.color = color;
        this.position = position;
        this.velocity = velocity;
        this.acceleration = new Vector2D(0, 0);
        this.trail = new ArrayList<>();
        addTrailPoint(position);
    }

    public String getId() { return id; }
    public double getMass() { return mass; }
    public double getRadius() { return radius; }
    public Color getColor() { return color; }
    public Vector2D getPosition() { return position; }
    public Vector2D getVelocity() { return velocity; }
    public Vector2D getAcceleration() { return acceleration; }
    public List<Vector2D> getTrail() { return Collections.unmodifiableList(trail); }

    public void setMass(double mass) { this.mass = mass; }
    public void setPosition(Vector2D position) { this.position = position; } 
    public void setVelocity(Vector2D velocity) { this.velocity = velocity; }
    public void setAcceleration(Vector2D acceleration) { this.acceleration = acceleration; }

    private void addTrailPoint(Vector2D point) {
        trail.add(new Vector2D(point.x, point.y)); 
        if (trail.size() > Constants.MAX_TRAIL_LENGTH) {
            trail.remove(0);
        }
    }

    public void update(double deltaTime) {
        this.velocity = velocity.add(acceleration.scale(deltaTime));
        this.position = position.add(this.velocity.scale(deltaTime));
        addTrailPoint(this.position);
    }

    public void resetAcceleration() {
        this.acceleration = new Vector2D(0, 0);
    }

    @Override
    public String toString() {
        return String.format("Body[%s, m=%.2e, p=(%.2f,%.2f), v=(%.2f,%.2f)]", id, mass, position.x, position.y, velocity.x, velocity.y);
    }
}