package com.nbody.simulator;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class Main extends Application {

    private Simulator simulator;
    private SimulationPanel simulationPanel;
    private AnimationTimer gameLoop;

    private boolean isPaused = false;
    private double simulationSpeed = 1.0; // Multiplier for Constants.TIME_STEP
    private double offsetY;
    private double offsetX;
    private double zoom;

    @Override
    public void start(Stage primaryStage) {
        simulator = new Simulator();
        simulationPanel = new SimulationPanel(simulator);

        // --- Setup Initial Bodies ---
        setupSolarSystemBodies();

        // --- Game Loop (AnimationTimer) ---
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                // Calculate time elapsed since last frame in seconds
                double elapsedTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                if (!isPaused) {
                    // Update simulation based on elapsed time and simulation speed
                    simulator.update(Constants.TIME_STEP * simulationSpeed);
                }

                simulationPanel.draw(); // Redraw the simulation
            }
        };
        gameLoop.start(); // Start the simulation loop

        // --- GUI Controls ---
        VBox controlPanel = createControlPanel();

        // --- Main Layout ---
        BorderPane root = new BorderPane();
        root.setCenter(simulationPanel);
        root.setRight(controlPanel);

        // --- Event Handling for Pan/Zoom ---
        addMouseHandlers();

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("N-Body Gravity Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Ensure canvas is drawn initially and on resize
        simulationPanel.widthProperty().addListener((obs, oldVal, newVal) -> simulationPanel.draw());
        simulationPanel.heightProperty().addListener((obs, oldVal, newVal) -> simulationPanel.draw());
        simulationPanel.draw(); // Initial draw
    }

    private void setupSolarSystemBodies() {
        // --- Solar System Bodies (Scaled) ---
        // Masses are relative to Sun's mass (Sun = 1.0 Solar Mass)
        // Distances are in Astronomical Units (AU)
        // Velocities are in AU per Earth Year

        // Define multipliers for size and x-coordinate adjustments
        final double SIZE_MULTIPLIER = 100.0; // Make bodies 10 times larger
        final double X_OFFSET_MULTIPLIER = 20.0; // Double the x-coordinate spacing
        final double SIZE_OFFSET = 0.5 * SIZE_MULTIPLIER; // Additional offset based on size to prevent overlap

        // Sun (Mass: 1 Solar Mass, position at origin, no initial velocity)
        simulator.addBody(
                new Body("Sun", 250000, 0.00465 * Constants.SUN_DISPLAY_RADIUS_MULTIPLIER * SIZE_MULTIPLIER, Color.YELLOW,
                        new Vector2D(0, 0), new Vector2D(0, 0))); // Radius: ~0.00465 AU

        // // Mercury
        // simulator.addBody(new Body("Mercury", 1.660E-7,
        //         0.000016 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER * SIZE_MULTIPLIER, Color.GRAY,
        //         new Vector2D(0.387 * X_OFFSET_MULTIPLIER + SIZE_OFFSET, 0), new Vector2D(0, 10.45)));

        // // Venus
        // simulator.addBody(new Body("Venus", 2.447E-6,
        //         0.000040 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER * SIZE_MULTIPLIER, Color.ORANGE,
        //         new Vector2D(0.723 * X_OFFSET_MULTIPLIER + SIZE_OFFSET, 0), new Vector2D(0, 7.65)));

        // // Earth
        // simulator.addBody(new Body("Earth", 3.003E-6,
        //         0.000043 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER * SIZE_MULTIPLIER, Color.BLUE,
        //         new Vector2D(1.0 * X_OFFSET_MULTIPLIER + SIZE_OFFSET, 0), new Vector2D(0, 6.28)));

        // // Mars
        // simulator.addBody(new Body("Mars", 3.227E-7,
        //         0.000023 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER * SIZE_MULTIPLIER, Color.RED,
        //         new Vector2D(1.524 * X_OFFSET_MULTIPLIER + SIZE_OFFSET, 0), new Vector2D(0, 5.08)));

        // // Jupiter
        // simulator.addBody(new Body("Jupiter", 9.548E-4,
        //         0.00047 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER * SIZE_MULTIPLIER, Color.BROWN,
        //         new Vector2D(5.203 * X_OFFSET_MULTIPLIER + SIZE_OFFSET, 0), new Vector2D(0, 2.76)));

        // // Saturn
        // simulator.addBody(new Body("Saturn", 2.859E-4,
        //         0.00039 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER * SIZE_MULTIPLIER, Color.GOLD,
        //         new Vector2D(9.537 * X_OFFSET_MULTIPLIER + SIZE_OFFSET, 0), new Vector2D(0, 2.04)));

        // // Uranus
        // simulator.addBody(new Body("Uranus", 4.366E-5,
        //         0.00017 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER * SIZE_MULTIPLIER, Color.LIGHTBLUE,
        //         new Vector2D(19.191 * X_OFFSET_MULTIPLIER + SIZE_OFFSET, 0), new Vector2D(0, 1.43)));

        // // Neptune
        // simulator.addBody(new Body("Neptune", 5.150E-5,
        //         0.00016 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER * SIZE_MULTIPLIER, Color.DARKBLUE,
        //         new Vector2D(30.069 * X_OFFSET_MULTIPLIER + SIZE_OFFSET, 0), new Vector2D(0, 1.14)));

        // Initial camera settings to see the whole system
        simulationPanel.setZoom(6); // Adjusted zoom for larger bodies and increased spacing
        simulationPanel.setOffsetX(0);
        simulationPanel.setOffsetY(0);
    }

    private VBox createControlPanel() {
        VBox controls = new VBox(10); // 10px spacing
        controls.setPadding(new Insets(15));
        controls.setStyle("-fx-background-color: #333; -fx-text-fill: white;");

        // --- Simulation Controls ---
        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> {
            isPaused = !isPaused;
            pauseButton.setText(isPaused ? "Resume" : "Pause");
        });

        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            simulator.reset();
            setupSolarSystemBodies();// Re-add initial bodies
            simulationPanel.resetCamera(); // Reset camera position
            simulationPanel.draw();
        });

        HBox simControls = new HBox(10, pauseButton, resetButton);

        // --- Speed Slider ---
        Label speedLabel = new Label("Sim Speed:");
        speedLabel.setTextFill(Color.WHITE);
        Slider speedSlider = new Slider(0.1, 5.0, simulationSpeed);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setBlockIncrement(0.1);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            simulationSpeed = newVal.doubleValue();
        });

        // --- Add Body Button (Simplified for now) ---
        Button addBodyButton = new Button("Add Random Body");
        addBodyButton.setOnAction(e -> addRandomBody());

        // --- Layout Controls ---
        controls.getChildren().addAll(
                new Label("Simulation Controls") {
                    {
                        setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: white;");
                    }
                },
                simControls,
                speedLabel,
                speedSlider,
                addBodyButton);

        return controls;
    }

    private void addRandomBody() {
        Random rand = new Random();
        double mass = 50 + rand.nextDouble() * 200;
        double radius = 3 + rand.nextDouble() * 5;
        Color color = Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));

        // Position it closer to the center / star
        Vector2D position = new Vector2D(rand.nextDouble() * 400 - 200, rand.nextDouble() * 400 - 200); // Smaller range

        // Adjust velocity range for more likely orbits.
        // A good starting point for orbit is perpendicular to position vector
        // and magnitude related to sqrt(G * M / r)
        double velMagnitude = rand.nextDouble() * 40 - 20; // Adjust this range
        Vector2D velocity = new Vector2D(0, 0.276);
        // You might want to adjust velocity to be more tangential
        // e.g., if position is (x,y), velocity might be (-y, x) scaled.
        // Example:
        // Vector2D posUnit = position.normalize();
        // Vector2D tangentVel = new Vector2D(-posUnit.y,
        // posUnit.x).scale(someOrbitalSpeed);
        // Vector2D velocity = tangentVel.add(new Vector2D(rand.nextDouble()*5-2.5,
        // rand.nextDouble()*5-2.5)); // Add some randomness

        simulator.addBody(
                new Body("New Body " + (simulator.getBodies().size() + 1), mass, radius, color, position, velocity));
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    private void addMouseHandlers() {
        // Pan: Mouse Drag
        final double[] lastX = { 0 };
        final double[] lastY = { 0 };

        simulationPanel.setOnMousePressed(event -> {
            lastX[0] = event.getX();
            lastY[0] = event.getY();
        });

        simulationPanel.setOnMouseDragged(event -> {
            double dx = event.getX() - lastX[0];
            double dy = event.getY() - lastY[0];
            simulationPanel.pan(dx, dy);
            lastX[0] = event.getX();
            lastY[0] = event.getY();
        });

        // Zoom: Scroll Wheel
        simulationPanel.setOnScroll(event -> {
            double zoomFactor = 1.0;
            if (event.getDeltaY() < 0) { // Scroll down (zoom out)
                zoomFactor = 1 / 1.1; // Zoom out by 10%
            } else { // Scroll up (zoom in)
                zoomFactor = 1.1; // Zoom in by 10%
            }
            simulationPanel.zoom(zoomFactor, event.getX(), event.getY());
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}