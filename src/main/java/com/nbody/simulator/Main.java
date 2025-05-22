package com.nbody.simulator;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main extends Application {
    // Simulation components
    private Simulator simulator;
    private AnimationTimer gameLoop;
    private boolean isPaused = false;
    private double simulationSpeed = 1.0;
    private Map<String, CelestialBody3D> celestialBodies = new HashMap<>();

    // 3D scene components
    private Group solarSystem;
    private final Group world = new Group();
    private Scene scene;
    private BorderPane mainLayout;
    
    // 3D control variables
    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    // Zoom and offset fields
    private double zoom = 1.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    @Override
    public void start(Stage primaryStage) {
        // Create 3D scene
        mainLayout = new BorderPane();
        scene = new Scene(world, 1200, 800, true);
        scene.setFill(Color.BLACK);

        // Set up camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-2000);
        scene.setCamera(camera);

        // Create solar system group
        solarSystem = new Group();
        world.getChildren().add(solarSystem);
        world.getChildren().add(mainLayout);

        // Initialize simulator
        simulator = new Simulator();

        // Setup Initial Bodies
        setupSolarSystemBodies();

        // Game Loop
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                if (!isPaused) {
                    // Update physics simulation
                    simulator.update(Constants.TIME_STEP * simulationSpeed);
                    
                    // Update 3D body positions
                    for (Body body : simulator.getBodies()) {
                        CelestialBody3D body3D = celestialBodies.get(body.getId());
                        if (body3D != null) {
                            body3D.setPosition(
                                body.getPosition().x,
                                body.getPosition().y,
                                0  // Keep z=0 for now, all bodies in same plane
                            );
                        }
                    }
                }
            }
        };
        gameLoop.start();

        // GUI Controls
        VBox controlPanel = createControlPanel();
        mainLayout.setRight(controlPanel);

        // Event Handling
        initMouseControl();

        primaryStage.setTitle("3D N-Body Gravity Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupSolarSystemBodies() {
        // Create and add the Sun (stationary at center)
        double sunRadius = 50;  // Visible size for display
        CelestialBody3D sun = new CelestialBody3D("Sun", sunRadius, Color.YELLOW);
        sun.setPosition(0, 0, 0);
        sun.setRotationSpeed(0.1);
        celestialBodies.put("Sun", sun);
        solarSystem.getChildren().add(sun.getNode());
        simulator.addBody(new Body("Sun", 250000, sunRadius, Color.YELLOW,
                new Vector2D(0, 0), new Vector2D(0, 0)));

        // Create Earth
        double earthRadius = 20;
        CelestialBody3D earth = new CelestialBody3D("Earth", earthRadius, "/earth/earth-d.jpg");
        double earthX = 400;  // Distance from Sun
        earth.setPosition(earthX, 0, 0);
        earth.setRotationSpeed(0.5);
        celestialBodies.put("Earth", earth);
        solarSystem.getChildren().add(earth.getNode());
        simulator.addBody(new Body("Earth", 3.003E-6, earthRadius, Color.BLUE,
                new Vector2D(earthX, 0), new Vector2D(0, 6.28)));

        // Create Mars
        double marsRadius = 15;
        CelestialBody3D mars = new CelestialBody3D("Mars", marsRadius, Color.RED);
        double marsX = 600;  // Distance from Sun
        mars.setPosition(marsX, 0, 0);
        mars.setRotationSpeed(0.4);
        celestialBodies.put("Mars", mars);
        solarSystem.getChildren().add(mars.getNode());
        simulator.addBody(new Body("Mars", 3.227E-7, marsRadius, Color.RED,
                new Vector2D(marsX, 0), new Vector2D(0, 5.08)));

        // Add mouse control for 3D rotation
        initMouseControl();
    }    private void initMouseControl() {
        Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
        solarSystem.getTransforms().addAll(xRotate, yRotate);
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        scene.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()) * 0.5);
            angleY.set(anchorAngleY + (anchorX - event.getSceneX()) * 0.5);
        });

        scene.setOnScroll(event -> {
            // Zoom the solar system
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 1/1.1;
            solarSystem.setScaleX(solarSystem.getScaleX() * zoomFactor);
            solarSystem.setScaleY(solarSystem.getScaleY() * zoomFactor);
            solarSystem.setScaleZ(solarSystem.getScaleZ() * zoomFactor);
        });
    }

    private void show3DEarth() {
        Stage earthStage = new Stage();
        InteractiveEarth earth = new InteractiveEarth(150);
        
        Group earthRoot = new Group(earth.getNode());
        Scene earthScene = new Scene(earthRoot, 800, 600, true);
        
        // Set up 3D camera for the Earth view
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-500);
        earthScene.setCamera(camera);
        
        earthStage.setTitle("3D Earth View");
        earthStage.setScene(earthScene);
        earthStage.show();
    }

    private VBox createControlPanel() {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(15));
        controls.setStyle("-fx-background-color: #333; -fx-text-fill: white;");

        // Simulation Controls
        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> {
            isPaused = !isPaused;
            pauseButton.setText(isPaused ? "Resume" : "Pause");
        });        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            simulator.reset();
            solarSystem.getChildren().clear();
            setupSolarSystemBodies();
        });

        HBox simControls = new HBox(10, pauseButton, resetButton);

        // Speed Slider
        Label speedLabel = new Label("Sim Speed:");
        speedLabel.setTextFill(Color.WHITE);
        Slider speedSlider = new Slider(0.1, 5.0, simulationSpeed);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setBlockIncrement(0.1);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            simulationSpeed = newVal.doubleValue();
        });

        // Buttons
        Button addBodyButton = new Button("Add Random Body");
        addBodyButton.setOnAction(e -> addRandomBody());

        Button earth3DButton = new Button("Open 3D Earth");
        earth3DButton.setOnAction(e -> show3DEarth());

        // Layout
        controls.getChildren().addAll(
            new Label("Simulation Controls") {{
                setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: white;");
            }},
            simControls,
            speedLabel,
            speedSlider,
            addBodyButton,
            earth3DButton
        );

        return controls;
    }

    private void addRandomBody() {
        Random rand = new Random();
        double mass = 50 + rand.nextDouble() * 200;
        double radius = 3 + rand.nextDouble() * 5;
        Color color = Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));        // Position it closer to the center / star
        Vector2D position = new Vector2D(rand.nextDouble() * 400 - 200, rand.nextDouble() * 400 - 200); // Smaller range

        // Adjust velocity range for more likely orbits.
        // A good starting point for orbit is perpendicular to position vector
        // and magnitude related to sqrt(G * M / r)
        // Create a velocity vector perpendicular to the position vector for better orbits
        double velMagnitude = rand.nextDouble() * 40 - 20; // Adjust this range
        Vector2D velocity = new Vector2D(-position.y, position.x).normalize().scale(Math.abs(velMagnitude));
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

    public static void main(String[] args) {
        launch(args);
    }
}