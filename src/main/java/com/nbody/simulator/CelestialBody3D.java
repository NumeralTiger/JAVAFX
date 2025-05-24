package com.nbody.simulator;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import java.util.List;
import java.util.stream.Collectors;

public class CelestialBody3D {
    private final Sphere sphere;
    private AnimationTimer rotationTimer;
    private double rotationSpeed = 0.2;
    private final String name;
    private final Polyline trailLine;
    private final Group bodyGroup;
    private final Group parentGroup;

    // Constructor for color-defined bodies
    public CelestialBody3D(String name, double radius, Color bodyColor, Color trailColor, Group parentGroup) {
        this.name = name;
        this.sphere = new Sphere(radius);
        this.parentGroup = parentGroup;
        this.bodyGroup = new Group(sphere);
        initializeBody(bodyColor);
        this.trailLine = createTrailLine(trailColor); // Use explicit trail color
        this.bodyGroup.getChildren().add(trailLine);
        this.parentGroup.getChildren().add(bodyGroup);
    }

    // Constructor for texture-defined bodies
    public CelestialBody3D(String name, double radius, String texturePath, Color trailColor, Group parentGroup) {
        this.name = name;
        this.sphere = new Sphere(radius);
        this.parentGroup = parentGroup;
        this.bodyGroup = new Group(sphere);
        initializeBodyWithTexture(texturePath);
        this.trailLine = createTrailLine(trailColor); // Use explicit trail color
        this.bodyGroup.getChildren().add(trailLine);
        this.parentGroup.getChildren().add(bodyGroup);
    }

    private void initializeBody(Color color) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        sphere.setMaterial(material);
        sphere.setRotationAxis(Rotate.Y_AXIS);
        startRotation();
    }

    private void initializeBodyWithTexture(String texturePath) {
        PhongMaterial material = new PhongMaterial();
        try {
            Image textureImage = new Image(getClass().getResourceAsStream(texturePath));
            material.setDiffuseMap(textureImage);
        } catch (Exception e) {
            System.err.println("Failed to load texture: " + texturePath + " for " + name + ". Using fallback color.");
            material.setDiffuseColor(Color.DARKGRAY);
        }
        sphere.setMaterial(material);
        sphere.setRotationAxis(Rotate.Y_AXIS);
        startRotation();
    }
    
    public void addRing(double innerRadius, double outerRadius, Color ringColor) {
        Cylinder ring = new Cylinder(outerRadius, Constants.RING_THICKNESS); 
        PhongMaterial ringMaterial = new PhongMaterial(ringColor);
        ring.setMaterial(ringMaterial);
        ring.setRotationAxis(Rotate.X_AXIS); 
        ring.setRotate(90); 
        this.bodyGroup.getChildren().add(ring);
    }

    private Polyline createTrailLine(Color trailColor) {
        Polyline polyline = new Polyline();
        polyline.setStroke(trailColor);
        polyline.setStrokeWidth(Constants.TRAIL_STROKE_WIDTH); // Use constant for thickness
        return polyline;
    }
    
    public void clearTrail() {
        trailLine.getPoints().clear();
    }

    public void createOrbitalPath(List<Vector2D> orbitPoints, double zOffset) {
        // Clear existing points
        trailLine.getPoints().clear();
        
        // Convert orbit points to 3D coordinates
        List<Double> polylinePoints = orbitPoints.stream()
            .flatMap(p -> List.of(p.x, p.y, zOffset).stream())
            .collect(Collectors.toList());
        
        // Add the first point again to close the orbit
        if (!orbitPoints.isEmpty()) {
            Vector2D firstPoint = orbitPoints.get(0);
            polylinePoints.addAll(List.of(firstPoint.x, firstPoint.y, zOffset));
        }
        
        trailLine.getPoints().setAll(polylinePoints);
        trailLine.setStrokeWidth(1.5); // Make lines thinner for better visualization
    }

    // Modified updateTrail method to support static orbital paths
    public void updateTrail(List<Vector2D> trailPoints, boolean isStaticOrbit) {
        if (trailPoints.size() < 2) {
            trailLine.getPoints().clear();
            return;
        }
        
        if (isStaticOrbit) {
            createOrbitalPath(trailPoints, 0.0);
        } else {
            List<Double> polylinePoints = trailPoints.stream()
                    .flatMap(p -> List.of(p.x, p.y, 0.0).stream())
                    .collect(Collectors.toList());
            trailLine.getPoints().setAll(polylinePoints);
        }
    }

    private void startRotation() {
        rotationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                sphere.setRotate(sphere.getRotate() + rotationSpeed);
            }
        };
        rotationTimer.start();
    }

    public void stopRotation() {
        if (rotationTimer != null) rotationTimer.stop();
    }

    public Node getNode() { return sphere; }
    public Group getBodyGroupNode() { return bodyGroup; }

    public void setPosition(double x, double y, double z) {
        bodyGroup.setTranslateX(x);
        bodyGroup.setTranslateY(y);
        bodyGroup.setTranslateZ(z);
    }

    public void setRotationSpeed(double speed) { this.rotationSpeed = speed; }
    public String getName() { return name; }

    public void removeFromScene() {
        stopRotation();
        parentGroup.getChildren().remove(bodyGroup);
    }
}