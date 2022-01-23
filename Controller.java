package sample;

import com.sun.istack.internal.NotNull;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceBox;

import javafx.scene.control.RadioButton;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;
import org.apache.commons.math3.complex.Complex;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.Random;


public class Controller {
  //Global Variables
  @FXML
  VBox VBOX;
  @FXML
  Pane PANE;
  @FXML
  ChoiceBox<String> CHOICEBOX;
  @FXML
  Canvas CANVAS;
  @FXML
  RadioButton D2RB;


  private BufferedImage MS;
  private int MAX_ITER = 500;

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

  String[] preBuilts = { "Sierpinski Triangle", "Sierpinski Carpet", "Koch Snowflake", "Barnsley Fern", "Vicsek", "Dragon Curve", "Mandelbrot Set",  "Burning Ship", "Feather",};

  public Controller() {
  }

  @FXML
  public void initialize() {

    D3Stage = ThreeD(new Stage());
    CANVAS.widthProperty().bind(PANE.widthProperty());
    CANVAS.heightProperty().bind(PANE.heightProperty());
    CHOICEBOX.setItems(FXCollections.observableArrayList(preBuilts));

    //Only updates when selection is changed
    CHOICEBOX.getSelectionModel()
        .selectedItemProperty()
        .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

          if(D2RB.isSelected()) drawFractal2D(newValue);
          else drawFractal3D(newValue);
          AddTracer(newValue);
        });
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

        if (MouseEvent.isControlDown()) {
          modifier = controlModifier;
        }
        if (MouseEvent.isShiftDown()) {
          modifier = shiftModifier;
        }
        if (MouseEvent.isPrimaryButtonDown()) {//Left click: rotate the camera
          cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * mouseSpeed * modifier * rotationSpeed);
          cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * mouseSpeed * modifier * rotationSpeed);
        }
        else if (MouseEvent.isSecondaryButtonDown()) {//Right click: zoom in with camera, change the z coordinate
          double z = camera.getTranslateZ();
          double newZ = z + mouseDeltaX* mouseSpeed * modifier;
          camera.setTranslateZ(newZ);
        }
        else if (MouseEvent.isMiddleButtonDown()) {//This moves the camera towards the mouse way
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

  private void AddTracer(String newValue){
    if(newValue.equals("Mandelbrot Set") || newValue.equals("Burning Ship") || newValue.equals("Feather")) {
      CANVAS.setOnMouseClicked(e -> {
        if(MS != null) {
          CANVAS.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(MS, null), 0, 0);
        }
        traceSequence(e.getX(), e.getY(), newValue);
      });
      CANVAS.setOnMouseDragged(e -> {
        if(MS != null) {
          CANVAS.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(MS, null), 0, 0);
        }
        traceSequence(e.getX(), e.getY(), newValue);
        //Now get the sounds of this point sequence
      });
    } else {
      //remove the tracers
      CANVAS.setOnMouseClicked(event -> {});
      CANVAS.setOnMouseDragged(event -> {});
    }
  }



  private double[] ptToScene(Complex c){
    double min = Math.min(CANVAS.getWidth(), CANVAS.getHeight());

    return new double[]{c.getReal() *  min / 2.0 + 100+ CANVAS.getWidth()/2.0, c.getImaginary() *  min / 2.0 + CANVAS.getHeight()/2.0};
  }

  private double[] ptToSet(double screenX, double screenY){
    double min = Math.min(CANVAS.getWidth(), CANVAS.getHeight());
    return new double[]{(screenX - CANVAS.getWidth() / 2.0 - 100) * 2.0 / min, (screenY - CANVAS.getHeight() / 2.0) * 2.0 / min};
  }

  //starting points relative to scene
  private void traceSequence(double v, double v1, String fractal) {
    GraphicsContext gc = CANVAS.getGraphicsContext2D();
    gc.setStroke(Color.RED);
    gc.setLineWidth(1);
    gc.strokeOval(v, v1, 1, 1);
    switch(fractal){
      case "Mandelbrot Set":{

        double c_re = ptToSet(v,v1)[0];
        double c_im = ptToSet(v,v1)[1];

        int iter = 0;
        Complex c = new Complex(c_re, c_im);
        Complex zero = new Complex(c_re, c_im);
        Complex oldC;
        while (iter < MAX_ITER) {
          oldC = zero;
          zero = zero.pow(2.0);
          zero = zero.add(c);
          gc.strokeLine(ptToScene(zero)[0], ptToScene(zero)[1], ptToScene(oldC)[0], ptToScene(oldC)[1]);
          iter++;
        }

        break;
      }
      case "Burning Ship":{
        double c_re = ptToSet(v,v1)[0];
        double c_im = ptToSet(v,v1)[1];

        int iter = 0;
        Complex c = new Complex(c_re, c_im);
        Complex zero = new Complex(c_re, c_im);
        Complex oldC;
        while (iter < MAX_ITER) {
          oldC = zero;
          double reZn = Math.abs(zero.getReal());
          double imZn = Math.abs(zero.getImaginary());
          zero = new Complex(reZn, imZn).pow(2);
          zero = zero.add(c);
          gc.strokeLine(ptToScene(zero)[0], ptToScene(zero)[1], ptToScene(oldC)[0], ptToScene(oldC)[1]);
          iter++;
        }
        break;
      }
      case "Feather":
        double c_re = ptToSet(v,v1)[0];
        double c_im = ptToSet(v,v1)[1];

        int iter = 0;
        Complex c = new Complex(c_re, c_im);
        Complex zero = new Complex(c_re, c_im);
        Complex oldC;
        Complex d;
        while (iter < MAX_ITER*10) {
          oldC = zero;
          zero = zero.pow(3);
          d = new Complex(zero.getReal()+c.getReal()+1, zero.getImaginary()+c.getImaginary());
          zero = zero.divide(d);
          zero = zero.add(c);
          zero = zero.conjugate();
          iter++;
          //gc.strokeLine(ptToScene(zero)[0], ptToScene(zero)[1], ptToScene(oldC)[0], ptToScene(oldC)[1]);
          iter++;
        }
        gc.strokeOval(ptToScene(zero)[0], ptToScene(zero)[1], 2, 2);
        break;
    }
  }

  private Object[] createPolygons(int sides, double delta_angle) {
    double width = CANVAS.getWidth();
    double height = CANVAS.getHeight();
    double r = width / 2 - 10;
    Point2D[] points = new Point2D[sides];
    Color[] colors = new Color[sides];
    for(int i = 0; i < sides; i++) {
      double angle = (180 + i * delta_angle) * Math.PI / 180.0;
      Color color = Color.hsb(i * delta_angle, .8, 1.0, .5);
      points[i] = new Point2D(width / 2 + r * Math.sin(angle), height / 2 + r * Math.cos(angle));
      colors[i] = color;
    }
    return new Object[]{points, colors};
  }

  private void drawFractal2D(String fractalSelected) {
    int width = (int) CANVAS.getWidth();
    int height = (int) CANVAS.getHeight();
    CANVAS.getGraphicsContext2D().clearRect(0, 0, width, height);
    BufferedImage WI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);//For non-affine fractals, write to image
    double[][][] aTm;//Affine transformation matrices
    double[][][] sAm;//Scalar add matrices
    double[] pM;//Probability matrix
    double[][] v0;//Starting vector point

    switch(fractalSelected) {
      case "Sierpinski Triangle": {
          aTm = new double[][][]{
              {{.5, 0}, {0, .5}},
              {{.5, 0}, {0, .5}},
              {{.5, 0}, {0, .5}},
          };
          sAm = new double[][][]{
              {{0}, {0}},
              {{1}, {0}},
              {{0}, {1}},
          };
          pM = new double[]{1.0 / 3, 1.0 / 3, 1.0 / 3};
          v0 = new double[][]{
              {0},
              {0}};//v[0][0] = x, v[1][0] = y
          Color[] colors = (Color[]) createPolygons(3, 360.0 / 3)[1];
          chaosGame(v0, aTm, sAm, pM, colors);
        break;
      }
      case "Barnsley Fern": {
          aTm = new double[][][]{
              {{0, 0}, {0, .16}},//f1
              {{.85, .04}, {-.04, .85}},//f2
              {{.2, -.26}, {.23, .22}},//f3
              {{-.15, .28}, {.26, .24}}//f4
          };
          sAm = new double[][][]{
              {{0}, {0}},//f1 (0,0)
              {{0}, {1.6}},//f2 (0,1.6)
              {{0}, {1.6}},//f3 (0, 1.6)
              {{0}, {.44}}//f4 (0, .44)
          };
          pM = new double[]{.1, .75, .07, .07};
        v0 = new double[][]{
            {0},
            {0}};//v[0][0] = x, v[1][0] = y
          Color[] colors = (Color[]) createPolygons(4, 0)[1];
          chaosGame(v0, aTm, sAm, pM, colors);
          break;
      }
      case "Sierpinski Carpet": {
         aTm = new double[][][] {
            {{1.0/3, 0},{0, 1.0/3}},
            {{1.0/3, 0},{0, 1.0/3}},
            {{1.0/3, 0},{0, 1.0/3}},
            {{1.0/3, 0},{0, 1.0/3}},
            {{1.0/3, 0},{0, 1.0/3}},
            {{1.0/3, 0},{0, 1.0/3}},
            {{1.0/3, 0},{0, 1.0/3}},
            {{1.0/3, 0},{0, 1.0/3}},
        };
        sAm = new double[][][] {
            {{0},{0}},
            {{0},{1.0/3}},
            {{0},{2.0/3}},
            {{1.0/3},{0}},
            {{2.0/3},{0}},
            {{1.0/3},{2.0/3}},
            {{2.0/3},{1.0/3}},
            {{2.0/3},{2.0/3}},
        };
        pM = new double[]{.125,.125,.125,.125,.125,.125,.125,.125};
        v0 = new double[][]{
            {0},
            {0}};//v[0][0] = x, v[1][0] = y
        Color[] colors = (Color[]) createPolygons(8, 360.0/8)[1];
        chaosGame(v0, aTm, sAm, pM, colors);
        break;
      }
      case "Koch Snowflake": {
        aTm = new double[][][]{
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
            {{1 / 6.0, -Math.sqrt(3) / 6}, {Math.sqrt(3) / 6, 1.0 / 6}},
            {{1 / 6.0, Math.sqrt(3) / 6}, {-Math.sqrt(3) / 6, 1.0 / 6}},
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
        };
        sAm = new double[][][]{
            {{0}, {0}},
            {{1.0 / 3}, {0}},
            {{1.0 / 2}, {Math.sqrt(3) / 6}},
            {{2.0 / 3}, {0}},
        };
        pM = new double[]{.25, .25, .25, .25};
        v0 = new double[][]{
            {0},
            {0}};//v[0][0] = x, v[1][0] = y
        Color[] colors = (Color[]) createPolygons(4, 360.0/4)[1];
        chaosGame(v0, aTm, sAm, pM, colors);
        break;
      }
      case "Dragon Curve": {
        double sqrt2 = 1 / Math.sqrt(2);
        aTm = new double[][][]{
            {{sqrt2 * Math.cos(45), -Math.sin(45) * sqrt2}, {Math.sin(45) * sqrt2, Math.cos(45) * sqrt2}},
            {{Math.cos(135) * sqrt2, -Math.sin(135) * sqrt2}, {Math.sin(135) * sqrt2, Math.cos(135) * sqrt2}},
        };
        sAm = new double[][][]{
            {{0,}, {0}},
            {{1,}, {0}},
        };
        pM = new double[]{.5, .5};
        v0 = new double[][]{
            {0},
            {0}};//v[0][0] = x, v[1][0] = y
        Color[] colors = (Color[]) createPolygons(2, 360.0/2)[1];
        chaosGame(v0, aTm, sAm, pM, colors);
        break;
      }
      case "Vicsek": {
        aTm = new double[][][]{
            {{1.0/3, 0},{0, 1.0/3}},
            {{1.0/3, 0},{0, 1.0/3}},
            {{1.0/3, 0},{0, 1.0/3}},
            {{1.0/3, 0},{0, 1.0/3}},
            {{1.0/3, 0},{0, 1.0/3}},
        };
        sAm = new double[][][]{
            {{0},{0}},
            {{0},{2.0/3}},
            {{2.0/3},{0}},
            {{2.0/3},{2.0/3}},
            {{1.0/3},{1.0/3}},
        };
        pM = new double[]{.2, .2, .2,.2,.2};
        v0 = new double[][]{
            {0},
            {0}};//v[0][0] = x, v[1][0] = y
        Color[] colors = (Color[]) createPolygons(5, 360.0/5)[1];
        chaosGame(v0, aTm, sAm, pM, colors);
        break;
      }
      case "Mandelbrot Set": {

        long start = System.nanoTime();
        int xory = Math.min(width, height);

        int cValue;

        for (int x = 0; x < width; x++) {
          for (int y = 0; y < height; y++) {
            double c_re = (x - width/2.0-100) *2 / xory;
            double c_im = (y - height/2.0) *2 / xory;

            cValue = mandelbrot(c_re, c_im);
            WI.setRGB(x, y, cValue | (cValue << 8));
          }
        }
        CANVAS.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(WI, null),0,0);
        MS = WI;
        long elapsedTime = System.nanoTime() - start;
        System.out.println(elapsedTime);
        break;
      }
      case "Burning Ship": {
        int xory = Math.min(width, height);
        int cValue;
        for (int x = 0; x < width; x++) {
          for (int y = 0; y < height; y++) {
            double c_re = (x - width/2.0-100) *3 / xory;
            double c_im = (y - height/2.0) *3 / xory;

            cValue = burningShip(c_re, c_im) * 255/MAX_ITER;
            WI.setRGB(x, y, cValue | (cValue << 8));
          }
        }
        CANVAS.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(WI, null),0,0);
        MS = WI;
        break;
      }
      case "Feather":{
        int xory = Math.min(width, height);
        int cValue;
        for (int x = 0; x < width; x++) {
          for (int y = 0; y < height; y++) {
            double c_re = (x - width/2.0-100) *3 / xory;
            double c_im = (y - height/2.0) *3 / xory;

            cValue = Feather(c_re, c_im) * 255/MAX_ITER;
            WI.setRGB(x, y, cValue | (cValue << 8));
          }
        }
        CANVAS.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(WI, null),0,0);
        MS = WI;
        break;
      }
//      case "3D":
//        Stage s = ThreeD(new Stage());
//        Sphere ss = new Sphere(10,10);
//        ss.setMaterial(new PhongMaterial(Color.ORCHID));
//        Sphere d = new Sphere(10,10);
//        ss.setMaterial(new PhongMaterial(Color.ORCHID));
//        d.relocate(10,10);
//        world.getChildren().add(ss);
//        world.getChildren().add(d);
//        break;

      default:
        drawFractal3D(fractalSelected);
    }
  }

  private void drawFractal3D(String fractalSelected){
    D3Stage.show();
    world.getChildren().clear();

    double[][][] aTm;//Affine transformation matrices
    double[][][] sAm;//Scalar add matrices
    double[] pM;//Probability matrix
    double[][] v0;//Starting vector point
    switch(fractalSelected){
      case "Sierpinski Triangle":{
        aTm = new double[][][]{
            {{.5, 0, 0}, {0, .5, 0}, {0, 0, .5}},
            {{.5, 0, 0}, {0, .5, 0}, {0, 0, .5}},
            {{.5, 0, 0}, {0, .5, 0}, {0, 0, .5}},
            {{.5, 0, 0}, {0, .5, 0}, {0, 0, .5}},
        };
        sAm = new double[][][]{
            {{0}, {0}, {0}},
            {{1}, {0}, {0}},
            {{0}, {1}, {0}},
            {{0}, {0}, {1}},
        };
        pM = new double[]{1.0 / 4, 1.0 / 4, 1.0 / 4, 1.0/4};
        v0 = new double[][]{
            {0},
            {0},
            {0}};//v[0][0] = x, v[1][0] = y
        Color[] colors = (Color[]) createPolygons(pM.length, 360.0 / pM.length)[1];

        chaosGame(v0, aTm, sAm, pM, colors);
        world.getChildren().addAll(shapeGroup.getChildren());
        break;
      }
      case "Sierpinski Carpet":{
        sAm = new double[][][] {
            {{0},{0},{0}},
            {{0},{0},{.5}},
            {{0},{0},{1}},
            {{1},{0},{0}},
            {{1},{0},{.5}},
            {{1},{0},{1}},
            {{0},{1},{0}},
            {{0},{1},{.5}},
            {{0},{1},{1}},
            {{1},{1},{0}},
            {{1},{1},{.5}},
            {{1},{1},{1}},
            {{.5},{0},{0}},
            {{.5},{0},{1}},
            {{0},{.5},{0}},
            {{0},{.5},{1}},
            {{1},{.5},{0}},
            {{1},{.5},{1}},
            {{.5},{1},{0}},
            {{.5},{1},{1}},
        };
        aTm = new double[sAm.length][3][3];
        for(int i = 0; i < sAm.length; ++i){
          aTm[i] = new double[][]{{1.0/3, 0, 0},{0, 1.0/3, 0}, {0, 0, 1.0/3}};
        }

        pM = new double[sAm.length];
        for(int i = 0; i < pM.length; i++) {
          pM[i] = 1.0/pM.length;
        }
        v0 = new double[][]{
            {0},
            {0},
            {0}};//v[0][0] = x, v[1][0] = y
        Color[] colors = (Color[]) createPolygons(pM.length, 360.0/pM.length)[1];
        chaosGame(v0, aTm, sAm, pM, colors);
        world.getChildren().addAll(shapeGroup.getChildren());
        break;
      }
      case "Vicsek":{
        aTm = new double[][][] {
            {{1.0/3, 0, 0},{0, 1.0/3, 0}, {0, 0, 1.0/3}},
            {{1.0/3, 0, 0},{0, 1.0/3, 0}, {0, 0, 1.0/3}},
            {{1.0/3, 0, 0},{0, 1.0/3, 0}, {0, 0, 1.0/3}},
            {{1.0/3, 0, 0},{0, 1.0/3, 0}, {0, 0, 1.0/3}},
            {{1.0/3, 0, 0},{0, 1.0/3, 0}, {0, 0, 1.0/3}},
            {{1.0/3, 0, 0},{0, 1.0/3, 0}, {0, 0, 1.0/3}},
            {{1.0/3, 0, 0},{0, 1.0/3, 0}, {0, 0, 1.0/3}},
            {{1.0/3, 0, 0},{0, 1.0/3, 0}, {0, 0, 1.0/3}},
            {{1.0/3, 0, 0},{0, 1.0/3, 0}, {0, 0, 1.0/3}},
        };
        sAm = new double[][][] {
            {{0},{0},{0}},
            {{1},{0},{0}},
            {{0},{1},{0}},
            {{1},{1},{0}},
            {{0},{0},{1}},
            {{1},{0},{1}},
            {{0},{1},{1}},
            {{1},{1},{1}},
            {{.5},{.5},{.5}}
        };
        pM = new double[9];
        for(int i = 0; i < pM.length; i++) {
          pM[i] = 1.0/pM.length;
        }
        v0 = new double[][]{
            {0},
            {0},
            {0}};//v[0][0] = x, v[1][0] = y
        Color[] colors = (Color[]) createPolygons(pM.length, 360.0/pM.length)[1];
        chaosGame(v0, aTm, sAm, pM, colors);
        world.getChildren().addAll(shapeGroup.getChildren());
        break;
      }
      case "Mandelbrot Set":{}


      default:

    }
  }

  private int Feather(double c_re, double c_im) {
    Complex zero = new Complex(c_re, c_im);
    Complex c = new Complex(c_re, c_im);
    Complex d;
    int iter = 0;
    while (zero.pow(2).getReal() < 4 && iter < MAX_ITER) {
      //Z^3/(1+Z circ Z)+C
      zero = zero.pow(3);
      d = new Complex(zero.getReal()+c.getReal()+1, zero.getImaginary()+c.getImaginary());
      zero = zero.divide(d);
      zero = zero.add(c);
      zero = zero.conjugate();
      iter++;
    }
    return (iter < MAX_ITER) ? iter : MAX_ITER;
  }

  private int burningShip(double c_re, double c_im) {
    Complex j = new Complex(c_re, c_im);
    Complex c = new Complex(c_re, c_im);
    int iter = 0;
    while (j.pow(2).getReal() < 4 && j.pow(2.0).getImaginary() < 4&& iter < MAX_ITER) {
      double reZn = Math.abs(j.getReal());
      double imZn = Math.abs(j.getImaginary());
      j = new Complex(reZn, imZn).pow(2);
      j = j.add(c);
      iter++;
    }
    return (iter < MAX_ITER) ? iter : MAX_ITER;
  }

  private int mandelbrot(double c_re, double c_im) {

    Complex j = new Complex(c_re, c_im);
    Complex c = new Complex(c_re, c_im);
    int iter = 0;

    //period checking is about 7 times faster
    //Period checking of period 2 bulb
    if(Math.pow(c_re+1,2)+Math.pow(c_im,2) < 1.0/16) return MAX_ITER;
    //Period checking of main cardioid
    double q = Math.pow(c_re-.25,2)+Math.pow(c_im,2);
    if(q*(q+(c_re-.25))< .25*Math.pow(c_im, 2)) return MAX_ITER;
    //Wasn't in either (only 9% of all datapoints, so iterate)

    while (j.pow(2).getReal() < 4 && j.pow(2).getImaginary() < 4 && iter < MAX_ITER) {
      j = j.pow(2);
      j = j.add(c);
      iter++;
    }
    return (iter < MAX_ITER) ? iter : MAX_ITER;
  }

  private void pertubationMandelbrot() {
    //pick reference in center of screen called Z
    //Iterate Z with full precision
    //  store each iterate
    //go through each other pixel
    //  other pixel is Z + perturb
    //  iterate Z+perturb in double value
    //  if(pauldebrot_test) add pixel to buffer
    //    for new reference point needed
    double[] pt = ptToSet(CANVAS.getWidth()/2.0, CANVAS.getHeight()/2.0);

    BigComplex Z = computeFullPrecision(pt[0], pt[1]);//if Z is null, then it escaped, pick a new reference
    System.out.println(Z);
  }

  private BigComplex computeFullPrecision(double v, double v1) {
    BigComplex Z = new BigComplex(new BigDecimal(v), new BigDecimal(v1));
    BigComplex C = new BigComplex(new BigDecimal(v), new BigDecimal(v1));
    for(int iter = 0; iter < MAX_ITER; iter++) {
      if(Math.pow(Z.getImaginary().doubleValue(),2) > 4 || Math.pow(Z.getReal().doubleValue(), 2) > 4) return null;
      Z = Z.square().add(C);
    }
    return Z;
  }

  private double[][] multiplyMatrices(double[][] firstMatrix, double[][] secondMatrix) {
    double[][] result = new double[firstMatrix.length][secondMatrix[0].length];

    for(int row = 0; row < result.length; row++) {
      for(int col = 0; col < result[row].length; col++) {
        result[row][col] = multiplyMatricesCell(firstMatrix, secondMatrix, row, col);
      }
    }

    return result;
  }

  private double multiplyMatricesCell(double[][] firstMatrix, double[][] secondMatrix, int row, int col) {
    double cell = 0;
    for(int i = 0; i < secondMatrix.length; i++) {
      cell += firstMatrix[row][i] * secondMatrix[i][col];
    }
    return cell;
  }

  /**
   * @param v               - the point we are transforming
   * @param transformMatrix - the affine transformation matrix
   * @param scalarAddMatrix - the scalar add matrix
   * @return - the transformed v
   */
  private double[][] affineTransform(double[][] v, double[][] transformMatrix, double[][] scalarAddMatrix) {
    //transform matrix 2x2
    double[][] vT = multiplyMatrices(transformMatrix, v);
    for(int i = 0; i < v.length; i++) {
      vT[i][0]+= scalarAddMatrix[i][0];
    }
    return vT;
  }

  private double[][] resizeToWindow(double[][] vn) {
    switch(CHOICEBOX.getSelectionModel().getSelectedItem()) {

      case "Barnsley Fern":
        vn[0][0] *= CANVAS.getWidth() / 6;
        vn[0][0] += CANVAS.getWidth() / 2;
        vn[1][0] *= CANVAS.getHeight() / 10;
        break;
      case "Sierpinski Triangle":
        vn[0][0] *= CANVAS.getWidth();
//        vn[0][0] +=  CANVAS.getWidth() / 3;
        vn[1][0] *= CANVAS.getHeight();
//        vn[1][0] += CANVAS.getHeight() /  3;
        break;
      case "Dragon Curve":
        vn[0][0] *= CANVAS.getWidth();
        vn[0][0] +=  CANVAS.getWidth() / 3;
        vn[1][0] *= CANVAS.getHeight();
        vn[1][0] += CANVAS.getHeight() /  3;
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

  private double[][] desizeToWindow(double[][] vn) {
    switch(CHOICEBOX.getSelectionModel().getSelectedItem()) {

      case "Barnsley Fern":
        vn[0][0] -= CANVAS.getWidth() / 2;
        vn[0][0] /= CANVAS.getWidth() / 6;
        vn[1][0] /= CANVAS.getHeight() / 10;
        break;
      case "Sierpinski Triangle":
        vn[0][0] /= CANVAS.getWidth();
        vn[1][0] /= CANVAS.getHeight();
        break;
      case "Dragon Curve":
        vn[0][0] -= CANVAS.getWidth() /  3;
        vn[0][0] /= CANVAS.getWidth();

        vn[1][0] -= CANVAS.getHeight() /  3;
        vn[1][0] /= CANVAS.getHeight();
        break;
      case "Koch Snowflake":
      case "Vicsek":
      case "Sierpinski Carpet":
        vn[0][0] /= CANVAS.getWidth();
        vn[1][0] /= CANVAS.getHeight();
        break;

      default:
        break;
    }
    return vn;
  }

  private void chaosGame(double[][] v0, double[][][] tMatrix, double[][][] sMatrix, double[] pMatrix, Color[] colors) {
    //randomly pick a tMatrix and sMatrix (binded) using the weights in pMatrix
    double sum;
    double[][] transform;
    double[][] scalAdd;
    double[][] vn = v0;
    double weight;
    boolean D2 = D2RB.isSelected();
    Random rand = new Random(123);
    CANVAS.getGraphicsContext2D().setFill(Color.WHITE);
    CANVAS.getGraphicsContext2D().setStroke(Color.WHITE);
    for(int j = 0; j < 1000; j++) {
      //find transform matrix randomly based off weights
      sum = 0;
      weight = rand.nextDouble();
      int sIndex = 0;//selected index
      for(int i = 0; i < pMatrix.length; i++) {
        sum += pMatrix[i];
        if(sum >= weight) {
          sIndex = i;
          break;
        }
      }

      transform = tMatrix[sIndex];
      scalAdd = sMatrix[sIndex];

      //apply affine transfomration to find new point
      vn = affineTransform(vn, transform, scalAdd);
      if(D2) {
        vn = resizeToWindow(vn);
        //draw vn onto canvas
        CANVAS.getGraphicsContext2D().fillOval(vn[0][0], vn[1][0], 1, 1);
        CANVAS.getGraphicsContext2D().setFill(colors[sIndex]);
        vn = desizeToWindow(vn);
      } else {
        Sphere sphere = new Sphere(1, 3);
        sphere.relocate(0,0);
        sphere.setTranslateX(vn[0][0]*120);
        sphere.setTranslateY(vn[1][0]*120);
        sphere.setTranslateZ(vn[2][0]*120);
        sphere.setMaterial(new PhongMaterial(colors[sIndex]));
        shapeGroup.getChildren().add(sphere);
      }
    }
  }

//  private void chaosGame(Point2D v0, Point2D[] points, Color[] colors, double df, int iterations) {
//    //recursively construct a sequence of points such that
//    // V_{n+1} = (1-df)p_i+df*v_n
//    // where p_i is a randomly selected vertex
//    // v_0 is given
//    GraphicsContext gc = CANVAS.getGraphicsContext2D();
//    gc.clearRect(0, 0, CANVAS.getWidth(), CANVAS.getHeight());
//    Random rand = new Random(1);
//    int selectedIndex;
//    while(iterations > 0) {
//      gc.fillOval(v0.getX(), v0.getY(), 1, 1);
//      selectedIndex = rand.nextInt(points.length);
//      gc.setFill(colors[selectedIndex]);
//      v0 = new Point2D(
//          (df) * (points[selectedIndex].getX()) + (1 - df) * v0.getX(),
//          (df) * (points[selectedIndex].getY()) + (1 - df) * v0.getY()
//      );
//      iterations--;
//
//    }
//  }
}

