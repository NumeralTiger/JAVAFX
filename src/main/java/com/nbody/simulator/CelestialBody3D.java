package com.nbody.simulator;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

public class CelestialBody3D {
    private final Sphere sphere;
    private AnimationTimer rotationTimer;
    private double rotationSpeed = 0.2; // degrees per frame
    private final String name;

    public CelestialBody3D(String name, double radius, Color color) {
        this.name = name;
        this.sphere = new Sphere(radius);
        initializeBody(color);
    }

    public CelestialBody3D(String name, double radius, String texturePath) {
        this.name = name;
        this.sphere = new Sphere(radius);
        initializeBodyWithTexture(texturePath);
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
        material.setDiffuseMap(new Image(getClass().getResourceAsStream(texturePath)));
        sphere.setMaterial(material);
        sphere.setRotationAxis(Rotate.Y_AXIS);
        startRotation();
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
        if (rotationTimer != null) {
            rotationTimer.stop();
        }
    }

    public Node getNode() {
        return sphere;
    }

    public void setPosition(double x, double y, double z) {
        sphere.setTranslateX(x);
        sphere.setTranslateY(y);
        sphere.setTranslateZ(z);
    }

    public void setRotationSpeed(double speed) {
        this.rotationSpeed = speed;
    }

    public String getName() {
        return name;
    }
}
