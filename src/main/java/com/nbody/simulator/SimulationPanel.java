package com.nbody.simulator;

import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * A custom JavaFX Pane that contains a Canvas for drawing the N-body simulation.
 */
public class SimulationPanel extends Pane {

    private Canvas canvas;
    private Simulator simulator;
    private VBox controlPanel;
    private Slider zoomSlider;

    // Camera/Viewport properties
    private double offsetX = 0;
    private double offsetY = 0;
    private double zoom = 0.01;

    public SimulationPanel(Simulator simulator) {
        this.simulator = simulator;
        this.canvas = new Canvas(); // Canvas will resize with pane
        
        // Create control panel
        // setupControlPanel();
        
        // Add canvas and control panel to the pane
        this.getChildren().addAll(canvas, controlPanel);

        // Bind canvas size to pane size, but leave space for control panel
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty().subtract(controlPanel.heightProperty()));

        // Initial drawing
        draw();
    }

    /**
     * Draws the current state of the simulation onto the canvas.
     */
    public void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();

        // Clear the canvas
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Apply camera transform (pan and zoom)
        gc.translate(canvas.getWidth() / 2 + offsetX, canvas.getHeight() / 2 + offsetY);
        gc.scale(zoom, zoom);

        for (Body body : simulator.getBodies()) {
            // Skip drawing if body was removed due to collision (can be null temporarily)
            if (body == null) continue;

            // --- Draw Trail ---
            gc.setStroke(body.getColor().deriveColor(0, 1, 1, 0.3)); // Slightly transparent trail
            gc.setLineWidth(0.5); // Thin trail line
            List<Vector2D> trail = body.getTrail();
            if (trail.size() > 1) { // Need at least 2 points to draw a line segment
                for (int i = 0; i < trail.size() - 1; i++) {
                    Vector2D p1 = trail.get(i);
                    Vector2D p2 = trail.get(i + 1);
                    // Draw line segments for the trail
                    gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
                }
            }

            // --- Draw Body (Circle) ---
            gc.setFill(body.getColor());
            double displayRadius = body.getRadius(); // This radius comes from Body.java
                                                     // and is already scaled by SUN/PLANET_DISPLAY_RADIUS_MULTIPLIER

            // x, y, width, height for fillOval:
            // x = center.x - radius
            // y = center.y - radius
            // width = 2 * radius
            // height = 2 * radius
            gc.fillOval(body.getPosition().x - displayRadius,
                        body.getPosition().y - displayRadius,
                        displayRadius * 2,
                        displayRadius * 2); // Ensure width and height are 2 * radius for a perfect circle

            // Optional: Draw body ID/Mass
            // gc.setFill(Color.WHITE);
            // gc.fillText(body.getId() + " (" + (int)body.getMass() + "kg)", body.getPosition().x + displayRadius + 2, body.getPosition().y);
        }

        // gc.restore(); // Restore to previous state (undo transformations)

        // // Draw debug info/overlays (not affected by camera)
        // gc.setFill(Color.WHITE);
        // gc.fillText(String.format("Time: %.2f s", simulator.getSimulationTime()), 10, 20);
        // gc.fillText(String.format("Zoom: %.2f", zoom), 10, 35);
        // gc.fillText(String.format("Bodies: %d", simulator.getBodies().size()), 10, 50);
    }

    //region Camera Control Methods

    public void pan(double dx, double dy) {
        offsetX += dx;
        offsetY += dy;
        draw();
    }

    public void zoom(double factor, double pivotX, double pivotY) {
        // Adjust pivot point based on current zoom and pan
        double worldX = (pivotX - (canvas.getWidth() / 2 + offsetX)) / zoom;
        double worldY = (pivotY - (canvas.getHeight() / 2 + offsetY)) / zoom;

        zoom *= factor;

        // Recalculate offset to keep pivot point fixed
        offsetX = pivotX - worldX * zoom - canvas.getWidth() / 2;
        offsetY = pivotY - worldY * zoom - canvas.getHeight() / 2;

        draw();
    }

    public void resetCamera() {
        offsetX = 0;
        offsetY = 0;
        zoom = 1.0;
        draw();
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
        draw();
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
        draw();
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
        draw();
    }
}
