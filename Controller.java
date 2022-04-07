package sample;

import com.sun.istack.internal.NotNull;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;


public class Controller {
  //Global Variables
  @FXML
  VBox VBOX;
  @FXML
  Pane PANE;
  @FXML
  ComboBox<String> COMBOBOX;
  @FXML
  Canvas CANVAS;
  @FXML
  RadioButton D2RB;

  private BufferedImage MS;

  //Declare Container Objects
  private final Group root = new Group();
  private final Xform shapeGroup = new Xform();
  private final Xform world = new Xform();

  //Declare Camera Objects
  private final PerspectiveCamera camera = new PerspectiveCamera(true);//False value sets it to the absolute position, true value centers it.
  private final Xform cameraXform = new Xform();
  private final Xform cameraXform2 = new Xform();
  private final Xform cameraXform3 = new Xform();
  private Stage D3Stage;

  //Declare Camera Variables
  private static final double cameraInitialDistance = -1800;//in the z axis
  private static final double cameraInitialXAngle = 0;
  private static final double cameraInitialYAngle = 180;
  private static final double cameraNearClip = 0.1;
  private static final double cameraFarClip = 10000.0;

  //Declare UserEvent Variables
  private static final double controlModifier = 0.1;//When holding Control, the speed at which User Events happen get multiplied by this
  private static final double shiftModifier = 7.0;//When holding Shift, the speed at which User Events happen get multiplied by this
  private static final double mouseSpeed = 0.1;
  private static final double rotationSpeed = 2.0;

  //Declare Mouse Variables
  private double mousePosX;
  private double mousePosY;
  private double mouseOldX;
  private double mouseOldY;
  private double mouseDeltaX;
  private double mouseDeltaY;

  private String[] preBuilts = {"Sierpinski Triangle", "Sierpinski Carpet", "Koch Snowflake", "Barnsley Fern", "Vicsek", "Dragon Curve", "Mandelbrot Set", "Burning Ship",};

  private Tracer tracer;
  private ComputationManager cm = new ComputationManager(600, 600);

  public Controller() {

  }

  @FXML
  public void initialize() {

    D3Stage = ThreeD(new Stage());
    CANVAS.widthProperty().bind(PANE.widthProperty());
    CANVAS.heightProperty().bind(PANE.heightProperty());


    //Only updates when selection is changed
    COMBOBOX.getSelectionModel()
        .selectedItemProperty()
        .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

          if(D2RB.isSelected()) drawFractal2D(newValue);
          else drawFractal3D(newValue);
          AddTracer(newValue);
        });

    COMBOBOX.setCellFactory(lv -> new ListCell<String>() {
      @Override
      public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if(empty) return;
        setText(item);
        if(item.equals("Koch Snowflake") || item.equals("Barnsley Fern") || item.equals("Dragon Curve") || item.equals("Mandelbrot Set") || item.equals("Burning Ship")){
          this.disableProperty().bind(D2RB.selectedProperty().not());
        }
      }
    });

    COMBOBOX.getItems().addAll(FXCollections.observableArrayList(preBuilts));
  }

  private void buildCamera() {
    root.getChildren().add(cameraXform);
    cameraXform.getChildren().add(cameraXform2);
    cameraXform2.getChildren().add(cameraXform3);
    cameraXform3.getChildren().add(camera);
    cameraXform3.setRotateZ(180.0);

    camera.setNearClip(cameraNearClip);
    camera.setFarClip(cameraFarClip);
    camera.setTranslateZ(cameraInitialDistance);
    cameraXform.ry.setAngle(cameraInitialYAngle);
    cameraXform.rx.setAngle(cameraInitialXAngle);
  }//End buildCamera

  private void handleMouse(@NotNull Scene scene) {
    scene.setOnMousePressed(MouseEvent -> {

      mousePosX = MouseEvent.getSceneX();
      mousePosY = MouseEvent.getSceneY();

    });
    scene.setOnMouseDragged(MouseEvent -> {
      {
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mousePosX = MouseEvent.getSceneX();
        mousePosY = MouseEvent.getSceneY();
        mouseDeltaX = (mousePosX - mouseOldX);
        mouseDeltaY = (mousePosY - mouseOldY);

        double modifier = 1.0;

        if(MouseEvent.isControlDown()) {
          modifier = controlModifier;
        }
        if(MouseEvent.isShiftDown()) {
          modifier = shiftModifier;
        }
        if(MouseEvent.isPrimaryButtonDown()) {//Left click: rotate the camera
          cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * mouseSpeed * modifier * rotationSpeed);
          cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * mouseSpeed * modifier * rotationSpeed);
        } else if(MouseEvent.isSecondaryButtonDown()) {//Right click: zoom in with camera, change the z coordinate
          double z = camera.getTranslateZ();
          double newZ = z + mouseDeltaX * mouseSpeed * modifier;
          camera.setTranslateZ(newZ);
        } else if(MouseEvent.isMiddleButtonDown()) {//This moves the camera towards the mouse way
          cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * mouseSpeed * modifier);
          cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * mouseSpeed * modifier);
        }
      }
    });
  }

  //Create a new scene that can be shown when 3d is invoked
  private Stage ThreeD(@NotNull Stage primaryStage) {
    buildCamera();
    root.getChildren().add(world);
    root.setDepthTest(DepthTest.ENABLE);//This says that whatever is in another shape will not be shown
    Scene D3 = new Scene(root, 1024, 768, true);
    handleMouse(D3);
    D3.setFill(Color.BLACK);//set the background color
    primaryStage.setScene(D3);
    D3.setCamera(camera);
    return primaryStage;
  }

  private void AddTracer(String newValue) {

    if(newValue.equals("Mandelbrot Set") || newValue.equals("Burning Ship")) {
      tracer = new Tracer(0, 0, CANVAS);
      CANVAS.setOnMouseClicked(e -> {
        if(MS != null) {
          CANVAS.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(MS, null), 0, 0);
        }
        tracer.setC_re(e.getX());
        tracer.setC_im(e.getY());
        tracer.traceSequence(newValue);
      });
      CANVAS.setOnMouseDragged(e -> {
        if(MS != null) {
          CANVAS.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(MS, null), 0, 0);
        }
        tracer.setC_re(e.getX());
        tracer.setC_im(e.getY());
        tracer.traceSequence(newValue);
        //Now get the sounds of this point sequence
      });
    } else {
      //remove the tracers
      CANVAS.setOnMouseClicked(event -> {
      });
      CANVAS.setOnMouseDragged(event -> {
      });
    }
  }

  private void drawFractal2D(String fractalSelected) {
    int width = (int) CANVAS.getWidth();
    int height = (int) CANVAS.getHeight();
    CANVAS.getGraphicsContext2D().clearRect(0, 0, width, height);
    BufferedImage WI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);//For non-affine fractals, write to image
    double[][] v0;//Starting vector point
    cm.setHeight(height);
    cm.setWidth(width);
    switch(fractalSelected) {
      case "Mandelbrot Set": {
        int cValue;

        for(int x = 0; x < width; x++) {
          for(int y = 0; y < height; y++) {
            double c_re = cm.ptToSet(x, y)[0];
            double c_im = cm.ptToSet(x, y)[1];

            cValue = cm.mandelbrot(c_re, c_im);
            WI.setRGB(x, y, cValue | (cValue << 8));
          }
        }
        CANVAS.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(WI, null), 0, 0);
        MS = WI;

        break;
      }
      case "Burning Ship": {

        int cValue;
        for(int x = 0; x < width; x++) {
          for(int y = 0; y < height; y++) {
            double c_re = cm.ptToSet(x, y)[0];
            double c_im = cm.ptToSet(x, y)[1];

            cValue = cm.burningShip(c_re, c_im) * 255 / cm.MAX_ITER;
            WI.setRGB(x, y, cValue | (cValue << 8));
          }
        }
        CANVAS.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(WI, null), 0, 0);
        MS = WI;
        break;
      }
      default:
        ChaosGame cg = new ChaosGame(CANVAS, fractalSelected, true);
        for(int i = 0; i < 100000; i++) {
          v0 = resizeToWindow(cg.nextPoint());
          CANVAS.getGraphicsContext2D().fillOval(v0[0][0], v0[1][0], 1, 1);
        }
    }
  }



  private void drawFractal3D(String fractalSelected) {
    D3Stage.show();
    world.getChildren().clear();

    switch(fractalSelected) {
      case "Mandelbrot Set":
        ComputationManager cm = new ComputationManager(CANVAS.getWidth(), CANVAS.getHeight());

        break;
      default:
        double[][] v0;//Starting vector point
        ChaosGame cg = new ChaosGame(CANVAS, fractalSelected, false);
        for(int i = 0; i < 10000; i++) {
          v0 = cg.nextPoint();
          Sphere sphere = new Sphere(1, 3);
          sphere.relocate(0, 0);
          sphere.setTranslateX(v0[0][0] * 120);
          sphere.setTranslateY(v0[1][0] * 120);
          sphere.setTranslateZ(v0[2][0] * 120);
          sphere.setMaterial(new PhongMaterial((Color) CANVAS.getGraphicsContext2D().getFill()));
          shapeGroup.getChildren().add(sphere);
        }
        world.getChildren().addAll(shapeGroup.getChildren());
    }
  }



  private double[][] resizeToWindow(double[][] vn) {
    switch(COMBOBOX.getSelectionModel().getSelectedItem()) {

      case "Barnsley Fern":
        vn[0][0] *= CANVAS.getWidth() / 6;
        vn[0][0] += CANVAS.getWidth() / 2;
        vn[1][0] *= CANVAS.getHeight() / 10;
        break;
      case "Sierpinski Triangle":
        vn[0][0] *= CANVAS.getWidth();
        vn[1][0] *= CANVAS.getHeight();
        break;
      case "Dragon Curve":
        vn[0][0] *= CANVAS.getWidth();
        vn[0][0] += CANVAS.getWidth() / 3;
        vn[1][0] *= CANVAS.getHeight();
        vn[1][0] += CANVAS.getHeight() / 3;
        break;
      case "Vicsek":
      case "Koch Snowflake":
      case "Sierpinski Carpet":
        vn[0][0] *= CANVAS.getWidth();
        vn[1][0] *= CANVAS.getHeight();
        break;

      default:
        break;
    }
    return vn;
  }
}
