package sample;

import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;

import static java.lang.Math.max;

public class ComputationManager {

  int MAX_ITER = 500;
  private double cam_x = 0, cam_y = 0;//centered at (cam_x, cam_y) as complex tuple
  private double cam_zoom = 300;
  private double width, height;
  ArrayList<BigComplex> Z_n = new ArrayList<>();
  ArrayList<Double> A_nr = new ArrayList<>();
  ArrayList<Double> B_nr = new ArrayList<>();
  ArrayList<Double> C_nr = new ArrayList<>();
  ArrayList<Double> A_ni = new ArrayList<>();
  ArrayList<Double> B_ni = new ArrayList<>();
  ArrayList<Double> C_ni = new ArrayList<>();

  public ComputationManager(double width, double height){
    this.width = width;
    this.height = height;
  }

  public void setCam_x(double cam_x) {
    this.cam_x = cam_x;
  }

  public void setCam_y(double cam_y){
    this.cam_y = cam_y;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public void setWidth(double width) {
    this.width = width;
  }

  public void setMAX_ITER(int MAX_ITER) {
    this.MAX_ITER = MAX_ITER;
  }

  public void setCam_zoom(double cam_zoom) {
    this.cam_zoom = max(1, this.cam_zoom + cam_zoom);
  }

  double[] ptToScene(Complex c) {
    return new double[]{cam_zoom * (c.getReal()+cam_x) + width/2, cam_zoom * (c.getImaginary()+cam_y) + height/2};
  }

  double[] ptToSet(double screenX, double screenY) {
    return new double[]{(screenX - width / 2.0) / cam_zoom - cam_x, (screenY - height / 2.0) / cam_zoom - cam_y};
  }

  int mandelbrot(double c_re, double c_im) {

    Complex constant = new Complex(c_re, c_im);
    Complex vn = constant;
    Complex vnMinusOne = vn;

    int iter = 0;

    //Period checking of period 2 bulb
    if(Math.pow(c_re + 1, 2) + Math.pow(c_im, 2) < 1.0 / 16) return MAX_ITER;
    //Period checking of main cardioid
    double q = Math.pow(c_re - .25, 2) + Math.pow(c_im, 2);
    if(q * (q + (c_re - .25)) < .25 * Math.pow(c_im, 2)) return MAX_ITER;
    //Wasn't in either (only 9% of all datapoints, so iterate)


    int stepsTaken = 0;
    int stepLimit = 2;
    Complex derivative = new Complex(1, 0);
    double epsilon = 0.05;


    //use cycle detection and max iterations
    while(vn.abs() < 4  && iter <= MAX_ITER) {
      if(derivative.abs() < epsilon * epsilon) return MAX_ITER;
      derivative = derivative.multiply(2).multiply(vn);
      vn = vn.pow(2).add(constant);
      if(vnMinusOne.equals(vn)) return MAX_ITER;

      if (stepsTaken == stepLimit) {
        vnMinusOne = vn;
        stepsTaken = 0;
        stepLimit *= 2;
      }
      stepsTaken++;
      iter++;
    }
    return iter;
  }

  int burningShip(double c_re, double c_im) {
    Complex constant = new Complex(c_re, c_im);
    Complex vn = constant;
    int iter = 0;

    while(vn.abs() < 4 && iter <= MAX_ITER) {
      double reZ = Math.abs(vn.getReal());
      double imZ = Math.abs(vn.getImaginary());
      vn = new Complex(reZ, imZ).pow(2.0).add(constant);
      iter++;
    }
    return iter;
  }
}
