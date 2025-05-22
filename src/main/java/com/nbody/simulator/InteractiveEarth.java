package com.nbody.simulator;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

public class InteractiveEarth {
    private final Sphere sphere;
    private AnimationTimer rotationTimer;
    private double rotationSpeed = 0.2; // degrees per frame

    public InteractiveEarth(double radius) {
        this.sphere = new Sphere(radius);
        initializeEarth();
    }

    private void initializeEarth() {
        sphere.setRotationAxis(Rotate.Y_AXIS);
        sphere.setMaterial(createEarthMaterial());
        startRotation();
    }

    private PhongMaterial createEarthMaterial() {
        PhongMaterial earthMaterial = new PhongMaterial();
        earthMaterial.setDiffuseMap(new Image(getClass().getResourceAsStream("/earth/earth-d.jpg")));
        earthMaterial.setSelfIlluminationMap(new Image(getClass().getResourceAsStream("/earth/earth-l.jpg")));
        earthMaterial.setSpecularMap(new Image(getClass().getResourceAsStream("/earth/earth-s.jpg")));
        earthMaterial.setBumpMap(new Image(getClass().getResourceAsStream("/earth/earth-n.jpg")));
        return earthMaterial;
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
}
