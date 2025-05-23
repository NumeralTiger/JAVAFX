package com.nbody.simulator;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.*;
import javafx.scene.*;
import java.util.*;

public class Main extends Application {
    // Simulation components
    private Simulator simulator;
    private AnimationTimer gameLoop;
    private boolean isPaused = false;
    private double simulationSpeed = 1.0; // Default simulation speed
    private Map<String, CelestialBody3D> celestialBodies = new HashMap<>();

    // 3D scene components
    private Group solarSystem; // Group for celestial bodies that will be rotated/scaled
    private final Group world = new Group(); // Root for 3D content in SubScene
    private Scene scene;
    private BorderPane mainLayout; // Main layout for the scene, will hold SubScene and controls
    
    // 3D control variables
    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    // Control panel components
    private VBox controlPanel;
    private Slider speedSlider; // Slider to control simulationSpeed
    // Removed unused ComboBox, Label, and Slider declarations for body properties for this fix


    @Override
    public void start(Stage primaryStage) {
        mainLayout = new BorderPane(); // This will be the root for the scene graph

        // Create 3D scene components
        solarSystem = new Group();
        world.getChildren().add(solarSystem); // solarSystem group will contain all celestial body 3D nodes

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        // Adjusted camera Z position for earthX = 1.0 AU
        camera.setTranslateZ(-15); // Closer to see objects at a distance of ~1 unit

        // Create SubScene for 3D content
        // The SubScene allows separating 3D rendering from the main UI layout
        SubScene subScene3D = new SubScene(world, 1000, 780, true, SceneAntialiasing.BALANCED);
        subScene3D.setFill(Color.BLACK);
        subScene3D.setCamera(camera);

        mainLayout.setCenter(subScene3D); // Place the 3D SubScene in the center of the BorderPane

        // Initialize simulator
        simulator = new Simulator();

        // Setup Initial Bodies
        setupSolarSystemBodies(); // This adds bodies to the simulator and 3D spheres to solarSystem group

        // Setup Control Panel for simulation speed
        controlPanel = new VBox(10); // Spacing of 10 between elements
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setPrefWidth(200); // Give the control panel a preferred width

        Label speedLabel = new Label("Simulation Speed:");
        // Slider: min speed, max speed, initial speed
        speedSlider = new Slider(0.1, 100, 1.0); // Speed from 0.1x to 100x
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(25);
        speedSlider.setMinorTickCount(5);
        speedSlider.setBlockIncrement(10); // Amount to adjust with arrow keys

        // Label to display the current speed value, bound to the slider's value
        Label currentSpeedLabel = new Label();
        currentSpeedLabel.textProperty().bind(
            Bindings.createStringBinding(
                () -> String.format("Speed: %.1fx", speedSlider.getValue()),
                speedSlider.valueProperty()
            )
        );

        // Update simulationSpeed when slider changes
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            simulationSpeed = newVal.doubleValue();
        });

        controlPanel.getChildren().addAll(speedLabel, speedSlider, currentSpeedLabel);
        mainLayout.setRight(controlPanel); // Add control panel to the right side of the BorderPane

        // Create the main scene with mainLayout as its root
        scene = new Scene(mainLayout, 1200, 800, true); // Overall window size

        // Initialize mouse controls for the 3D SubScene (rotation and zoom)
        initMouseControl(subScene3D, solarSystem);

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
                    // Update physics simulation multiple times based on simulation speed
                    int steps = (int) Math.ceil(simulationSpeed); // Number of simulation steps per frame
                    // Effective time step for each physics update
                    double timeStepPerPhysicsUpdate = Constants.TIME_STEP * (simulationSpeed / steps); 
                    
                    for (int i = 0; i < steps; i++) {
                        simulator.update(timeStepPerPhysicsUpdate); // Pass the adjusted time step
                    }
                    
                    // Update 3D body positions
                    for (Body body : simulator.getBodies()) { //
                        CelestialBody3D body3D = celestialBodies.get(body.getId()); //
                        if (body3D != null) {
                            body3D.setPosition(
                                body.getPosition().x, //
                                body.getPosition().y, //
                                0  // Keep z=0 for 2D simulation in 3D space
                            );
                        }
                    }
                }
                // Note: lastUpdate = now; was missing from the original game loop, which could affect calculations
                // if time delta per frame was intended to be used. However, current logic uses fixed steps.
            }
        };
        gameLoop.start();      

        primaryStage.setScene(scene);
        primaryStage.setTitle("N-body Gravity Simulator (Enhanced)");
        primaryStage.show();
    }

    private void setupSolarSystemBodies() {
        // Define actual astronomical unit radii for scaling (approximate)
        // double sunActualRadiusAU = 0.00465; // Sun's radius in AU
        // double earthActualRadiusAU = 0.000043; // Earth's radius in AU

        // Define desired display radii for a visually appealing simulation where Earth orbits at 1 AU
        // These are arbitrary values for visualization, not direct physical scaling for this example.
        double sunDisplayRadius = 0.2;  // Make Sun larger on screen
        double earthDisplayRadius = 0.05; // Make Earth smaller, but visible

        // Create and add the Sun
        CelestialBody3D sun3D = new CelestialBody3D("Sun", sunDisplayRadius, "/2k_sun.jpg");
        sun3D.setPosition(0, 0, 0);
        sun3D.setRotationSpeed(0.01); // Visual rotation of the sphere
        celestialBodies.put("Sun", sun3D);
        solarSystem.getChildren().add(sun3D.getNode());
        // The Body's radius parameter is used for collision detection in PhysicsEngine
        // and 2D drawing in SimulationPanel (if used).
        // We use the same displayRadius here for consistency.
        simulator.addBody(new Body("Sun", 1.0, sunDisplayRadius, Color.YELLOW, // Sun's mass is 1.0
                new Vector2D(0, 0), new Vector2D(0, 0))); //

        // Create Earth
        CelestialBody3D earth3D = new CelestialBody3D("Earth", earthDisplayRadius, "/earth/earth-d.jpg"); //
        double earthX = 1.0;  // Set Earth's distance from Sun to 1.0 AU
        earth3D.setPosition(earthX, 0, 0); //
        earth3D.setRotationSpeed(0.24); // Visual rotation of the sphere
        celestialBodies.put("Earth", earth3D);
        solarSystem.getChildren().add(earth3D.getNode()); //

        // Calculate the required orbital velocity for Earth
        // v = sqrt(G * M_sun / r)
        // M_sun is 1.0 (as per Sun's body definition)
        // G is Constants.GRAVITATIONAL_CONSTANT
        // r is earthX
        double earthOrbitalVelocity = Math.sqrt((Constants.GRAVITATIONAL_CONSTANT * 1.0) / earthX);

        simulator.addBody(new Body("Earth", 3.003E-6, earthDisplayRadius, Color.BLUE, // Earth's mass
                new Vector2D(earthX, 0), new Vector2D(0, earthOrbitalVelocity))); // Initial position and corrected velocity
        
        // Removed redundant call to initMouseControl() here
    }    
    
    // Modified initMouseControl to accept the node for attaching events (SubScene)
    // and the group to apply transformations (solarSystem)
    private void initMouseControl(Node eventNode, Group groupToTransform) {
        Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
        groupToTransform.getTransforms().addAll(xRotate, yRotate); // Apply rotations to the solarSystem group
        xRotate.angleProperty().bind(angleX); //
        yRotate.angleProperty().bind(angleY); //

        eventNode.setOnMousePressed(event -> { // Attach events to the SubScene
            anchorX = event.getSceneX(); //
            anchorY = event.getSceneY(); //
            anchorAngleX = angleX.get(); //
            anchorAngleY = angleY.get(); //
        });

        eventNode.setOnMouseDragged(event -> { // Attach events to the SubScene
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()) * 0.5); //
            angleY.set(anchorAngleY + (anchorX - event.getSceneX()) * 0.5); //
        });

        eventNode.setOnScroll(event -> { // Attach events to the SubScene
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 1/1.1; //
            groupToTransform.setScaleX(groupToTransform.getScaleX() * zoomFactor); //
            groupToTransform.setScaleY(groupToTransform.getScaleY() * zoomFactor); //
            groupToTransform.setScaleZ(groupToTransform.getScaleZ() * zoomFactor); //
        });
    }

    // Zoom and offset methods are not used by the 3D view directly,
    // they were part of SimulationPanel logic or older concepts.
    // public void setZoom(double zoom) { this.zoom = zoom; }
    // public void setOffsetX(double offsetX) { this.offsetX = offsetX; }
    // public void setOffsetY(double offsetY) { this.offsetY = offsetY; }

    public static void main(String[] args) {
        launch(args);
    }
}