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
        speedSlider = new Slider(0.1, 5, 1.0); // Speed from 0.1x to 100x
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
        primaryStage.setTitle("Gravity Simulator: Interactive");
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
        double mercuryDisplayRadius = 0.02; // Mercury's display radius
        double venusDisplayRadius = 0.03; // Venus's display radius 
        double marsDisplayRadius = 0.025; // Mars's display radius
        double jupiterDisplayRadius = 0.1; // Jupiter's display radius
        double saturnDisplayRadius = 0.08; // Saturn's display radius
        double uranusDisplayRadius = 0.06; // Uranus's display radius
        double neptuneDisplayRadius = 0.05; // Neptune's display radius


        double mercuryX = 0.39; // Mercury's distance from Sun in AU
        double venusX = 0.72; // Venus's distance from Sun in AU
        double marsX = 1.52; // Mars's distance from Sun in AU
        double jupiterX = 5.2; // Jupiter's distance from Sun in AU
        double saturnX = 9.58; // Saturn's distance from Sun in AU
        double uranusX = 19.22; // Uranus's distance from Sun in AU
        double neptuneX = 30.05; // Neptune's distance from Sun in AU
        
        double mercuryOrbitalVelocity = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT / mercuryX);
        double venusOrbitalVelocity = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT / venusX);
        double marsOrbitalVelocity = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT / marsX);
        double jupiterOrbitalVelocity = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT / jupiterX);
        double saturnOrbitalVelocity = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT / saturnX);
        double uranusOrbitalVelocity = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT / uranusX);
        double neptuneOrbitalVelocity = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT / neptuneX);

        // Create and add the other planets
        CelestialBody3D mercury3D = new CelestialBody3D("Mercury", mercuryDisplayRadius, Color.GRAY);
        mercury3D.setPosition(mercuryX, 0, 0);  
        mercury3D.setRotationSpeed(0.24); // Visual rotation of the sphere
        celestialBodies.put("Mercury", mercury3D);
        solarSystem.getChildren().add(mercury3D.getNode()); //
        simulator.addBody(new Body("Mercury", 3.285E-7, mercuryDisplayRadius, Color.GRAY, // Mercury's mass
                new Vector2D(mercuryX, 0), new Vector2D(0, mercuryOrbitalVelocity))); // Initial position and velocity

        CelestialBody3D venus3D = new CelestialBody3D("Venus", venusDisplayRadius, Color.YELLOW);   
        venus3D.setPosition(venusX, 0, 0);
        venus3D.setRotationSpeed(0.24); // Visual rotation of the sphere
        celestialBodies.put("Venus", venus3D);
        solarSystem.getChildren().add(venus3D.getNode()); //
        simulator.addBody(new Body("Venus", 4.867E-6, venusDisplayRadius, Color.YELLOW, // Venus's mass
                new Vector2D(venusX, 0), new Vector2D(0, venusOrbitalVelocity))); // Initial position and velocity
        CelestialBody3D mars3D = new CelestialBody3D("Mars", marsDisplayRadius, Color.RED);
        mars3D.setPosition(marsX, 0, 0);
        mars3D.setRotationSpeed(0.24); // Visual rotation of the sphere
        celestialBodies.put("Mars", mars3D);
        solarSystem.getChildren().add(mars3D.getNode()); //
        simulator.addBody(new Body("Mars", 3.21E-7, marsDisplayRadius, Color.RED, // Mars's mass
                new Vector2D(marsX, 0), new Vector2D(0, marsOrbitalVelocity))); // Initial position and velocity

        CelestialBody3D jupiter3D = new CelestialBody3D("Jupiter", jupiterDisplayRadius, Color.rgb(216, 202, 157));
        jupiter3D.setPosition(jupiterX, 0, 0);
        jupiter3D.setRotationSpeed(0.24); // Visual rotation of the sphere
        celestialBodies.put("Jupiter", jupiter3D);
        solarSystem.getChildren().add(jupiter3D.getNode()); //
        simulator.addBody(new Body("Jupiter", 1.898E-3, jupiterDisplayRadius, Color.ORANGE, // Jupiter's mass
                new Vector2D(jupiterX, 0), new Vector2D(0, jupiterOrbitalVelocity))); // Initial position and velocity  

        CelestialBody3D saturn3D = new CelestialBody3D("Saturn", saturnDisplayRadius, Color.YELLOW);
        saturn3D.setPosition(saturnX, 0, 0);
        saturn3D.setRotationSpeed(0.24); // Visual rotation of the sphere
        celestialBodies.put("Saturn", saturn3D);
        solarSystem.getChildren().add(saturn3D.getNode()); //
        simulator.addBody(new Body("Saturn", 5.683E-4, saturnDisplayRadius, Color.YELLOW, // Saturn's mass
                new Vector2D(saturnX, 0), new Vector2D(0, saturnOrbitalVelocity))); // Initial position and velocit
        CelestialBody3D uranus3D = new CelestialBody3D("Uranus", uranusDisplayRadius, Color.CYAN);
        uranus3D.setPosition(uranusX, 0, 0);
        uranus3D.setRotationSpeed(0.24); // Visual rotation of the sphere
        celestialBodies.put("Uranus", uranus3D);
        solarSystem.getChildren().add(uranus3D.getNode()); //
        simulator.addBody(new Body("Uranus", 8.681E-4, uranusDisplayRadius, Color.CYAN, // Uranus's mass
                new Vector2D(uranusX, 0), new Vector2D(0, uranusOrbitalVelocity))); // Initial position and velocity

        CelestialBody3D neptune3D = new CelestialBody3D("Neptune", neptuneX, Color.BLUE);
        neptune3D.setPosition(neptuneX, 0, 0);
        neptune3D.setRotationSpeed(0.24); // Visual rotation of the sphere
        celestialBodies.put("Neptune", neptune3D);
        solarSystem.getChildren().add(neptune3D.getNode()); //
        simulator.addBody(new Body("Neptune", 1.024E-4, neptuneDisplayRadius, Color.BLUE, // Neptune's mass
                new Vector2D(neptuneX, 0), new Vector2D(0, neptuneOrbitalVelocity))); // Initial position and velocity


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
                new Vector2D(0, 0), new Vector2D(0.01, 0.01))); //

        // Create Earthf
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
        double earthOrbitalVelocity = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT / earthX);

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