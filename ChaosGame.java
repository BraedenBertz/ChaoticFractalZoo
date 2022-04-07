package sample;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

public class ChaosGame {
  private double[][] lastPoint;
  private double width;
  private double height;
  private GraphicsContext gc;
  private Random rand = new Random(123);
  private double[][][] tMatrix, sMatrix;//Affine transformation matrices and scalar add matrices
  private double[] pMatrix;//Probability matrix
  private Color[] colors;

  public ChaosGame(Canvas c, String selectedFractal, boolean _2D) {
    width = c.getWidth();
    height = c.getHeight();
    gc = c.getGraphicsContext2D();
    if(_2D) setMatrices2D(selectedFractal);
    else setMatrices3D(selectedFractal);
    lastPoint[0][0] = -1;
  }

  private void setMatrices3D(String selectedFractal) {
    lastPoint = new double[][]{
        {0},
        {0},
        {0}};
    switch(selectedFractal){
      case "Sierpinski Triangle":{
        tMatrix = new double[][][]{
            {{.5, 0, 0}, {0, .5, 0}, {0, 0, .5}},
            {{.5, 0, 0}, {0, .5, 0}, {0, 0, .5}},
            {{.5, 0, 0}, {0, .5, 0}, {0, 0, .5}},
            {{.5, 0, 0}, {0, .5, 0}, {0, 0, .5}},
        };
        sMatrix = new double[][][]{
            {{0}, {0}, {0}},
            {{1}, {0}, {0}},
            {{0}, {1}, {0}},
            {{0}, {0}, {1}},
        };
        pMatrix = new double[]{1.0 / 4, 1.0 / 4, 1.0 / 4, 1.0/4};
        //v[0][0] = x, v[1][0] = y
        colors = (Color[]) createPolygons(pMatrix.length, 360.0 / pMatrix.length)[1];
        break;
      }
      case "Sierpinski Carpet":{
        sMatrix = new double[][][] {
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
        tMatrix = new double[sMatrix.length][3][3];
        for(int i = 0; i < sMatrix.length; ++i){
          tMatrix[i] = new double[][]{{1.0/3, 0, 0},{0, 1.0/3, 0}, {0, 0, 1.0/3}};
        }

        pMatrix = new double[sMatrix.length];
        for(int i = 0; i < pMatrix.length; i++) {
          pMatrix[i] = 1.0/pMatrix.length;
        }
        colors = (Color[]) createPolygons(pMatrix.length, 360.0/pMatrix.length)[1];
        break;
      }
      case "Vicsek":{
        tMatrix = new double[][][] {
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
        sMatrix = new double[][][] {
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
        pMatrix = new double[9];
        for(int i = 0; i < pMatrix.length; i++) {
          pMatrix[i] = 1.0/pMatrix.length;
        }
        colors = (Color[]) createPolygons(pMatrix.length, 360.0/pMatrix.length)[1];
        break;
      }
      default:

    }
  }

  private void setMatrices2D(String selectedFractal) {
    lastPoint = new double[][]{
        {0},
        {0}};
    switch(selectedFractal) {
      case "Sierpinski Triangle": {
        tMatrix = new double[][][]{
            {{.5, 0}, {0, .5}},
            {{.5, 0}, {0, .5}},
            {{.5, 0}, {0, .5}},
        };
        sMatrix = new double[][][]{
            {{0}, {0}},
            {{1}, {0}},
            {{0}, {1}},
        };
        pMatrix = new double[]{1.0 / 3, 1.0 / 3, 1.0 / 3};
       colors = (Color[]) createPolygons(3, 360.0 / 3)[1];
        break;
      }
      case "Barnsley Fern": {
        tMatrix = new double[][][]{
            {{0, 0}, {0, .16}},//f1
            {{.85, .04}, {-.04, .85}},//f2
            {{.2, -.26}, {.23, .22}},//f3
            {{-.15, .28}, {.26, .24}}//f4
        };
        sMatrix = new double[][][]{
            {{0}, {0}},//f1 (0,0)
            {{0}, {1.6}},//f2 (0,1.6)
            {{0}, {1.6}},//f3 (0, 1.6)
            {{0}, {.44}}//f4 (0, .44)
        };
        pMatrix = new double[]{.1, .75, .07, .07};

        colors = (Color[]) createPolygons(4, 0)[1];
        break;
      }
      case "Sierpinski Carpet": {
        tMatrix = new double[][][]{
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
        };
        sMatrix = new double[][][]{
            {{0}, {0}},
            {{0}, {1.0 / 3}},
            {{0}, {2.0 / 3}},
            {{1.0 / 3}, {0}},
            {{2.0 / 3}, {0}},
            {{1.0 / 3}, {2.0 / 3}},
            {{2.0 / 3}, {1.0 / 3}},
            {{2.0 / 3}, {2.0 / 3}},
        };
        pMatrix = new double[]{.125, .125, .125, .125, .125, .125, .125, .125};

        colors = (Color[]) createPolygons(8, 360.0 / 8)[1];
        break;
      }
      case "Koch Snowflake": {
        tMatrix = new double[][][]{
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
            {{1 / 6.0, -Math.sqrt(3) / 6}, {Math.sqrt(3) / 6, 1.0 / 6}},
            {{1 / 6.0, Math.sqrt(3) / 6}, {-Math.sqrt(3) / 6, 1.0 / 6}},
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
        };
        sMatrix = new double[][][]{
            {{0}, {0}},
            {{1.0 / 3}, {0}},
            {{1.0 / 2}, {Math.sqrt(3) / 6}},
            {{2.0 / 3}, {0}},
        };
        pMatrix = new double[]{.25, .25, .25, .25};

        colors = (Color[]) createPolygons(4, 360.0 / 4)[1];
        break;
      }
      case "Dragon Curve": {
        double sqrt2 = 1 / Math.sqrt(2);
        tMatrix = new double[][][]{
            {{sqrt2 * Math.cos(45), -Math.sin(45) * sqrt2}, {Math.sin(45) * sqrt2, Math.cos(45) * sqrt2}},
            {{Math.cos(135) * sqrt2, -Math.sin(135) * sqrt2}, {Math.sin(135) * sqrt2, Math.cos(135) * sqrt2}},
        };
        sMatrix = new double[][][]{
            {{0,}, {0}},
            {{1,}, {0}},
        };
        pMatrix = new double[]{.5, .5};

        colors = (Color[]) createPolygons(2, 360.0 / 2)[1];
        break;
      }
      case "Vicsek": {
        tMatrix = new double[][][]{
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
            {{1.0 / 3, 0}, {0, 1.0 / 3}},
        };
        sMatrix = new double[][][]{
            {{0}, {0}},
            {{0}, {2.0 / 3}},
            {{2.0 / 3}, {0}},
            {{2.0 / 3}, {2.0 / 3}},
            {{1.0 / 3}, {1.0 / 3}},
        };
        pMatrix = new double[]{.2, .2, .2, .2, .2};

        colors = (Color[]) createPolygons(5, 360.0 / 5)[1];
        break;
      }
      default:
    }
  }

  private Object[] createPolygons(int sides, double delta_angle) {
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

  public double[][] nextPoint() {
    //randomly pick a tMatrix and sMatrix (binded) using the weights in pMatrix
    double sum;
    double[][] transform;
    double[][] scalAdd;
    double weight;
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

    //apply affine transformation to find new point
    lastPoint = affineTransform(lastPoint, transform, scalAdd);
    //color according to transformation picked
    gc.setFill(colors[sIndex]);
    gc.setStroke(colors[sIndex]);
    //return transformed point
    double[][] ret = new double[lastPoint.length][];
    for(int i = 0; i < lastPoint.length; i++) {
      ret[i] = lastPoint[i].clone();
    }
    return ret;
  }
}
