package com.nbody.simulator;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class Main extends Application {
    // Simulation components
    private Simulator simulator;
    private AnimationTimer gameLoop;
    private boolean isPaused = false;
    private double simulationSpeed = 1.0;
    private final Map<String, CelestialBody3D> celestialBody3DMap = new HashMap<>();
    private final Random random = new Random();
    private int randomBodyCounter = 0;

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

    // UI Components
    private ComboBox<String> bodySelectorComboBox;
    private TextField massTextField;
    private TextField velocityXTextField;
    private TextField velocityYTextField;
    private TextField gravitationalConstantTextField;


    @Override
    public void start(Stage primaryStage) {
        mainLayout = new BorderPane();
        simulator = new Simulator();

        SubScene subScene3D = setup3DScene();
        mainLayout.setCenter(subScene3D);

        setupSolarSystemBodies();

        VBox controlPanel = setupControlPanel();
        mainLayout.setRight(controlPanel);

        scene = new Scene(mainLayout, 1400, 900, true);
        initMouseControl(subScene3D, solarSystem);

        startGameLoop();
        updateBodySelector();

        primaryStage.setScene(scene);
        primaryStage.setTitle("Interactive Solar System Simulator");
        primaryStage.show();
    }

    private SubScene setup3DScene() {
        solarSystem = new Group();
        world.getChildren().add(solarSystem);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        // Adjusted for better orbit viewing
        camera.setFarClip(Constants.CAMERA_FAR_CLIP);
        camera.setTranslateZ(Constants.CAMERA_INITIAL_Z);

        SubScene subScene3D = new SubScene(world, 1000, 880, true, SceneAntialiasing.DISABLED);
        subScene3D.setFill(Color.BLACK);
        subScene3D.setCamera(camera);
        
        return subScene3D;
    }

    private VBox setupControlPanel() {
        VBox controlPanel = new VBox(15);
        controlPanel.setPadding(new Insets(15));
        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setPrefWidth(380);
        controlPanel.setStyle("-fx-background-color:rgb(189, 189, 189);");

        // Simulation Speed Control
        Label speedLabel = new Label("Simulation Speed:");
        Slider speedSlider = new Slider(0.1, 10, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(2);
        speedSlider.setMinorTickCount(1);
        speedSlider.setBlockIncrement(1);
        Label currentSpeedLabel = new Label();
        currentSpeedLabel.textProperty().bind(
                Bindings.createStringBinding(() -> String.format("Speed: %.1fx", speedSlider.getValue()), speedSlider.valueProperty())
        );
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> simulationSpeed = newVal.doubleValue());
        controlPanel.getChildren().addAll(speedLabel, speedSlider, currentSpeedLabel);

        // Gravitational Constant Control
        controlPanel.getChildren().add(new Separator());
        Label gLabel = new Label("Gravitational Constant (G):");
        gravitationalConstantTextField = new TextField(String.valueOf(Constants.GRAVITATIONAL_CONSTANT));
        Button updateGButton = new Button("Update G");
        updateGButton.setOnAction(e -> {
            try {
                double newG = Double.parseDouble(gravitationalConstantTextField.getText());
                Constants.GRAVITATIONAL_CONSTANT = newG;
                System.out.println("Gravitational constant updated to: " + newG);
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid G Value", "Please enter a valid number for G.");
            }
        });
        HBox gBox = new HBox(10, gravitationalConstantTextField, updateGButton);
        gBox.setAlignment(Pos.CENTER_LEFT);
        controlPanel.getChildren().addAll(gLabel, gBox);

        // Body Selection and Modification
        controlPanel.getChildren().add(new Separator());
        Label selectBodyLabel = new Label("Modify Celestial Body:");
        bodySelectorComboBox = new ComboBox<>();
        bodySelectorComboBox.setPromptText("Select Body");
        bodySelectorComboBox.setMaxWidth(Double.MAX_VALUE);
        bodySelectorComboBox.setOnAction(e -> loadSelectedBodyProperties());

        GridPane propertiesGrid = new GridPane();
        propertiesGrid.setHgap(10);
        propertiesGrid.setVgap(8);
        propertiesGrid.setPadding(new Insets(5,0,5,0));
        propertiesGrid.add(new Label("Mass:"), 0, 0);
        massTextField = new TextField();
        propertiesGrid.add(massTextField, 1, 0);
        propertiesGrid.add(new Label("Velocity X:"), 0, 1);
        velocityXTextField = new TextField();
        propertiesGrid.add(velocityXTextField, 1, 1);
        propertiesGrid.add(new Label("Velocity Y:"), 0, 2);
        velocityYTextField = new TextField();
        propertiesGrid.add(velocityYTextField, 1, 2);
        Button applyChangesButton = new Button("Apply Body Changes");
        applyChangesButton.setMaxWidth(Double.MAX_VALUE);
        applyChangesButton.setOnAction(e -> applyBodyModifications());
        controlPanel.getChildren().addAll(selectBodyLabel, bodySelectorComboBox, propertiesGrid, applyChangesButton);
        
        // Add Random Body
        controlPanel.getChildren().add(new Separator());
        Button addRandomBodyButton = new Button("Add Random Body");
        addRandomBodyButton.setMaxWidth(Double.MAX_VALUE);
        addRandomBodyButton.setOnAction(e -> addRandomCelestialBody());
        controlPanel.getChildren().add(addRandomBodyButton);

        // Pause Button
        controlPanel.getChildren().add(new Separator());
        Button pauseButton = new Button("Pause/Resume");
        pauseButton.setMaxWidth(Double.MAX_VALUE);
        pauseButton.setOnAction(e -> isPaused = !isPaused);
        controlPanel.getChildren().add(pauseButton);

        return controlPanel;
    }

    private void loadSelectedBodyProperties() {
        String selectedBodyId = bodySelectorComboBox.getValue();
        if (selectedBodyId == null) return;
        simulator.getBodyById(selectedBodyId).ifPresent(body -> {
            massTextField.setText(String.format("%.2e", body.getMass()));
            velocityXTextField.setText(String.format("%.2e", body.getVelocity().x));
            velocityYTextField.setText(String.format("%.2e", body.getVelocity().y));
        });
    }

    private void applyBodyModifications() {
        String selectedBodyId = bodySelectorComboBox.getValue();
        if (selectedBodyId == null) {
            showErrorDialog("No Body Selected", "Please select a body to modify.");
            return;
        }
        Optional<Body> bodyOpt = simulator.getBodyById(selectedBodyId);
        if (!bodyOpt.isPresent()) {
            showErrorDialog("Body Not Found", "The selected body no longer exists.");
            return;
        }
        Body body = bodyOpt.get();
        try {
            double newMass = Double.parseDouble(massTextField.getText());
            double newVelX = Double.parseDouble(velocityXTextField.getText());
            double newVelY = Double.parseDouble(velocityYTextField.getText());
            if (newMass <= 0) {
                 showErrorDialog("Invalid Mass", "Mass must be a positive value.");
                 return;
            }
            body.setMass(newMass);
            body.setVelocity(new Vector2D(newVelX, newVelY));
            // Optional: Clear trail for this body as its orbit will change drastically
            celestialBody3DMap.get(selectedBodyId).clearTrail();
            System.out.println("Applied changes to: " + selectedBodyId);
        } catch (NumberFormatException ex) {
            showErrorDialog("Invalid Input", "Please enter valid numbers.");
        }
    }
    
    private void addRandomCelestialBody() {
        randomBodyCounter++;
        String name = "RandBody-" + randomBodyCounter;
        double mass = (random.nextDouble() * 1e-5) + 1e-7; 
        double displayRadius = (random.nextDouble() * 0.03) + 0.01;
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = (random.nextDouble() * 10) + 2; 
        double posX = distance * Math.cos(angle);
        double posY = distance * Math.sin(angle);
        double centralMass = simulator.getBodyById("Sun").map(Body::getMass).orElse(1.0);
        double orbitalVelMag = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT * centralMass / distance);
        double velX = -orbitalVelMag * Math.sin(angle) + (random.nextDouble() - 0.5) * 0.5;
        double velY = orbitalVelMag * Math.cos(angle) + (random.nextDouble() - 0.5) * 0.5;
        Color bodyColor = Color.rgb(random.nextInt(200)+55, random.nextInt(200)+55, random.nextInt(200)+55); // Brighter random colors
        Color trailColor = Color.BLACK; // Trail color set to black

        createAndAddCelestialBody(name, displayRadius, bodyColor, trailColor, mass, posX, posY, velX, velY, 0.1);
        updateBodySelector();
    }

    // Overload for color-defined bodies
    private void createAndAddCelestialBody(String name, double displayRadius, Color bodyColor, Color trailColor,
                                           double mass, double initialX, double initialY,
                                           double initialVelX, double initialVelY,
                                           double visualRotationSpeed) {
        CelestialBody3D body3D = new CelestialBody3D(name, displayRadius, bodyColor, trailColor, solarSystem);
        setupCelestialBody(body3D, name, mass, displayRadius, initialX, initialY, initialVelX, initialVelY, visualRotationSpeed);
    }

    // Overload for texture-defined bodies
    private void createAndAddCelestialBody(String name, double displayRadius, String texturePath, Color trailColor,
                                           double mass, double initialX, double initialY,
                                           double initialVelX, double initialVelY,
                                           double visualRotationSpeed) {
        CelestialBody3D body3D = new CelestialBody3D(name, displayRadius, texturePath, trailColor, solarSystem);
        setupCelestialBody(body3D, name, mass, displayRadius, initialX, initialY, initialVelX, initialVelY, visualRotationSpeed);
    }

    private void setupCelestialBody(CelestialBody3D body3D, String name, double mass, double displayRadius,
                                    double initialX, double initialY, double initialVelX, double initialVelY,
                                    double visualRotationSpeed) {
        body3D.setPosition(initialX, initialY, 0);
        body3D.setRotationSpeed(visualRotationSpeed);
        celestialBody3DMap.put(name, body3D);

        Color physicsBodyColor = Color.GRAY; // Fallback for the Body object
        Node node = body3D.getNode();
        if (node instanceof Sphere) {
            Sphere sphere = (Sphere) node;
            if (sphere.getMaterial() instanceof PhongMaterial) {
                PhongMaterial phongMaterial = (PhongMaterial) sphere.getMaterial();
                if (phongMaterial.getDiffuseColor() != null) {
                     physicsBodyColor = phongMaterial.getDiffuseColor();
                }
            }
        }
        
        simulator.addBody(new Body(name, mass, displayRadius, physicsBodyColor,
                new Vector2D(initialX, initialY), new Vector2D(initialVelX, initialVelY)));
    }

    private void setupSolarSystemBodies() {
        // Display Radii
        double sunDisplayRadius = 0.2;
        double mercuryDisplayRadius = 0.02;
        double venusDisplayRadius = 0.03;
        double earthDisplayRadius = 0.04;
        double marsDisplayRadius = 0.025;
        double jupiterDisplayRadius = 0.1;
        double saturnDisplayRadius = 0.09;
        double uranusDisplayRadius = 0.06;
        double neptuneDisplayRadius = 0.055;

        double sunMass = 1.0;

        // Define Trail Colors (inspired by the diagram)
        Color mercuryTrailColor = Color.rgb(180, 180, 180); // Light Grey
        Color venusTrailColor = Color.rgb(255, 200, 100); // Light Yellow
        Color earthTrailColor = Color.rgb(0, 122, 255); // Bright Blue
        Color marsTrailColor = Color.rgb(255, 100, 100); // Light Red
        Color jupiterTrailColor = Color.rgb(255, 200, 150); // Light Brown
        Color saturnTrailColor = Color.rgb(160, 100, 200); // Purple-ish
        Color uranusTrailColor = Color.rgb(0, 220, 180);   // Cyan/Green
        Color neptuneTrailColor = Color.rgb(0, 0, 205);  // Medium Blue / DodgerBlue

        // Sun (no trail needed or very faint if desired)
        createAndAddCelestialBody("Sun", sunDisplayRadius, "/2k_sun.jpg", Color.rgb(255,223,186,0.1) , sunMass, 0, 0, 0, 0, 0.01);

        double mercuryX = 0.39; double mercuryMass = 1.652e-7;
        double mercuryVel = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT * sunMass / mercuryX);
        createAndAddCelestialBody("Mercury", mercuryDisplayRadius, Color.GRAY, mercuryTrailColor, mercuryMass, mercuryX, 0, 0, mercuryVel, 0.24);

        double venusX = 0.72; double venusMass = 2.447e-6;
        double venusVel = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT * sunMass / venusX);
        createAndAddCelestialBody("Venus", venusDisplayRadius, Color.rgb(255,230,100), venusTrailColor, venusMass, venusX, 0, 0, venusVel, 0.15);

        double earthX = 1.0; double earthMass = 3.003e-6;
        double earthVel = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT * sunMass / earthX);
        createAndAddCelestialBody("Earth", earthDisplayRadius, "/earth/earth-d.jpg", earthTrailColor, earthMass, earthX, 0, 0, earthVel, 0.24);

        double marsX = 1.52; double marsMass = 3.213e-7;
        double marsVel = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT * sunMass / marsX);
        createAndAddCelestialBody("Mars", marsDisplayRadius, Color.rgb(190,80,60), marsTrailColor, marsMass, marsX, 0, 0, marsVel, 0.20);

        double jupiterX = 5.2; double jupiterMass = 9.548e-4;
        double jupiterVel = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT * sunMass / jupiterX);
        createAndAddCelestialBody("Jupiter", jupiterDisplayRadius, Color.rgb(216, 202, 157), jupiterTrailColor, jupiterMass, jupiterX, 0, 0, jupiterVel, 0.1);

        double saturnX = 9.58; double saturnMass = 2.857e-4;
        double saturnVel = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT * sunMass / saturnX);
        createAndAddCelestialBody("Saturn", saturnDisplayRadius, Color.KHAKI, saturnTrailColor, saturnMass, saturnX, 0, 0, saturnVel, 0.08);
        CelestialBody3D saturn3D = celestialBody3DMap.get("Saturn");
        if (saturn3D != null) saturn3D.addRing(saturnDisplayRadius * 1.5, saturnDisplayRadius * 2.2, Color.rgb(220,220,190,0.5));

        double uranusX = 19.22; double uranusMass = 4.366e-5;
        double uranusVel = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT * sunMass / uranusX);
        createAndAddCelestialBody("Uranus", uranusDisplayRadius, Color.rgb(170,225,230), uranusTrailColor, uranusMass, uranusX, 0, 0, uranusVel, 0.05);

        double neptuneX = 30.05; double neptuneMass = 5.151e-5;
        double neptuneVel = Math.sqrt(Constants.GRAVITATIONAL_CONSTANT * sunMass / neptuneX);
        createAndAddCelestialBody("Neptune", neptuneDisplayRadius, Color.rgb(60,100,200), neptuneTrailColor, neptuneMass, neptuneX, 0, 0, neptuneVel, 0.04);
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isPaused) {
                    int steps = (int) Math.max(1, Math.ceil(simulationSpeed));
                    double timeStepPerPhysicsUpdate = Constants.TIME_STEP * (simulationSpeed / steps);

                    for (int i = 0; i < steps; i++) {
                        simulator.update(timeStepPerPhysicsUpdate);
                    }

                    List<String> removedBodyIds = simulator.processRemovals();
                    if (!removedBodyIds.isEmpty()) {
                        removedBodyIds.forEach(id -> {
                            CelestialBody3D cBody3D = celestialBody3DMap.remove(id);
                            if (cBody3D != null) cBody3D.removeFromScene();
                        });
                        updateBodySelector();
                        if (bodySelectorComboBox.getValue() != null && removedBodyIds.contains(bodySelectorComboBox.getValue())) {
                            bodySelectorComboBox.setValue(null);
                            massTextField.clear();
                            velocityXTextField.clear();
                            velocityYTextField.clear();
                        }
                    }

                    for (Body body : simulator.getBodies()) {
                        CelestialBody3D body3D = celestialBody3DMap.get(body.getId());
                        if (body3D != null) {
                            body3D.setPosition(body.getPosition().x, body.getPosition().y, 0);
                            body3D.updateTrail(body.getTrail(), false);
                        }
                    }
                }
            }
        };
        gameLoop.start();
    }
    
    private void updateBodySelector() {
        List<String> bodyNames = simulator.getBodies().stream()
                                        .map(Body::getId)
                                        .sorted()
                                        .collect(Collectors.toList());
        bodySelectorComboBox.setItems(FXCollections.observableArrayList(bodyNames));
    }

    private void initMouseControl(Node eventNode, Group groupToTransform) {
        Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
        groupToTransform.getTransforms().addAll(xRotate, yRotate);
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        eventNode.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        eventNode.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()) * 0.5);
            angleY.set(anchorAngleY + (anchorX - event.getSceneX()) * 0.5);
        });

        eventNode.setOnScroll(event -> {
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 1 / 1.1;
            groupToTransform.setScaleX(groupToTransform.getScaleX() * zoomFactor);
            groupToTransform.setScaleY(groupToTransform.getScaleY() * zoomFactor);
            // groupToTransform.setScaleZ(groupToTransform.getScaleZ() * zoomFactor);
        });
    }

    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}