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

        // Sun (Mass: 1 Solar Mass, position at origin, no initial velocity)
        simulator.addBody(new Body("Sun", 1.0, 0.00465 * Constants.SUN_DISPLAY_RADIUS_MULTIPLIER, Color.YELLOW,
                new Vector2D(0, 0), new Vector2D(0, 0))); // Radius: ~0.00465 AU

        // Mercury
        // Mass: 1.660E-7 Solar Mass
        // Avg. Distance: 0.387 AU
        // Orbital Velocity: ~10.45 AU/year (tangential for circular orbit)
        simulator.addBody(new Body("Mercury", 1.660E-7, 0.000016 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER, Color.GRAY,
                new Vector2D(0.387, 0), new Vector2D(0, 10.45)));

        // Venus
        // Mass: 2.447E-6 Solar Mass
        // Avg. Distance: 0.723 AU
        // Orbital Velocity: ~7.65 AU/year
        simulator.addBody(new Body("Venus", 2.447E-6, 0.000040 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER, Color.ORANGE,
                new Vector2D(0.723, 0), new Vector2D(0, 7.65)));

        // Earth
        // Mass: 3.003E-6 Solar Mass (often set to 1 for convenience in some systems, but use real ratio here)
        // Avg. Distance: 1.0 AU
        // Orbital Velocity: ~6.28 AU/year
        simulator.addBody(new Body("Earth", 3.003E-6, 0.000043 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER, Color.BLUE,
                new Vector2D(1.0, 0), new Vector2D(0, 6.28)));

        // Mars
        // Mass: 3.227E-7 Solar Mass
        // Avg. Distance: 1.524 AU
        // Orbital Velocity: ~5.08 AU/year
        simulator.addBody(new Body("Mars", 3.227E-7, 0.000023 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER, Color.RED,
                new Vector2D(1.524, 0), new Vector2D(0, 5.08)));

        // Jupiter
        // Mass: 9.548E-4 Solar Mass
        // Avg. Distance: 5.203 AU
        // Orbital Velocity: ~2.76 AU/year
        simulator.addBody(new Body("Jupiter", 9.548E-4, 0.00047 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER, Color.BROWN,
                new Vector2D(5.203, 0), new Vector2D(0, 2.76)));

        // Saturn
        // Mass: 2.859E-4 Solar Mass
        // Avg. Distance: 9.537 AU
        // Orbital Velocity: ~2.04 AU/year
        simulator.addBody(new Body("Saturn", 2.859E-4, 0.00039 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER, Color.GOLD,
                new Vector2D(9.537, 0), new Vector2D(0, 2.04)));

        // Uranus
        // Mass: 4.366E-5 Solar Mass
        // Avg. Distance: 19.191 AU
        // Orbital Velocity: ~1.43 AU/year
        simulator.addBody(new Body("Uranus", 4.366E-5, 0.00017 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER, Color.LIGHTBLUE,
                new Vector2D(19.191, 0), new Vector2D(0, 1.43)));

        // Neptune
        // Mass: 5.150E-5 Solar Mass
        // Avg. Distance: 30.069 AU
        // Orbital Velocity: ~1.14 AU/year
        simulator.addBody(new Body("Neptune", 5.150E-5, 0.00016 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER, Color.DARKBLUE,
                new Vector2D(30.069, 0), new Vector2D(0, 1.14)));

        // Initial camera settings to see the whole system
        simulationPanel.setZoom(12); // Adjust based on max AU
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
                new Label("Simulation Controls") {{ setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: white;"); }},
                simControls,
                speedLabel,
                speedSlider,
                addBodyButton
        );

        return controls;
    }

    private void addRandomBody() {
        Random rand = new Random();
        double mass = (1E-8) + rand.nextDouble() * (1E-6); // Small mass for random bodies
        double radius = 0.00001 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER + rand.nextDouble() * 0.00002 * Constants.PLANET_DISPLAY_RADIUS_MULTIPLIER;
        Color color = Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));

        // Position within a reasonable orbital distance (e.g., 2 to 10 AU)
        double distance = 2.0 + rand.nextDouble() * 8.0; // Random distance between 2 and 10 AU
        double angle = rand.nextDouble() * 2 * Math.PI; // Random angle
        Vector2D position = new Vector2D(distance * Math.cos(angle), distance * Math.sin(angle));

        // Approximate orbital speed for a circular orbit at that distance (sqrt(G*M_sun/r))
        // This is a rough estimate to get it orbiting.
        double expectedOrbitalSpeed = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT * 1.0 / distance); // Mass of Sun is 1.0

        // Give it a velocity mostly tangential to its position, with some randomness
        Vector2D tangentialVelocity = new Vector2D(-position.y, position.x).normalize().scale(expectedOrbitalSpeed * (0.9 + rand.nextDouble() * 0.2)); // 0.9 to 1.1 of expected speed
        Vector2D radialVelocity = position.normalize().scale(rand.nextDouble() * 0.1); // Small radial component for eccentricity
        Vector2D velocity = tangentialVelocity.add(radialVelocity);

        simulator.addBody(new Body("Asteroid " + (simulator.getBodies().size() + 1), mass, radius, color, position, velocity));
        System.out.println("Added random body: " + simulator.getBodies().get(simulator.getBodies().size()-1));
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
        final double[] lastX = {0};
        final double[] lastY = {0};

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