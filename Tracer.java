package sample;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.apache.commons.math3.complex.Complex;

public class Tracer extends ComputationManager {
  double c_re, c_im;

  GraphicsContext gc;

  public Tracer(double c_re, double c_im, Canvas c) {
    super(c.getWidth(), c.getHeight());
    this.c_im = c_re;
    this.c_re = c_im;
    this.gc = c.getGraphicsContext2D();
  }

  void setC_im(double c_im) {
    this.c_im = c_im;
  }

  void setC_re(double c_re) {
    this.c_re = c_re;
  }

  //starting points relative to scene
  void traceSequence(String fractal) {
    c_re = ptToSet(c_re, c_im)[0];
    c_im = ptToSet(c_re, c_im)[1];
    gc.setStroke(Color.RED);
    gc.setLineWidth(1);
    gc.strokeOval(c_re, c_im, 1, 1);
    switch(fractal) {
      case "Mandelbrot Set": {
        int iter = 0;
        Complex constant = new Complex(c_re, c_im);
        Complex vn = constant;
        Complex vnMinusOne;
        while(iter <= MAX_ITER*2) {
          vnMinusOne = vn;
          vn = vn.pow(2.0).add(constant);
          gc.strokeLine(ptToScene(vn)[0], ptToScene(vn)[1], ptToScene(vnMinusOne)[0], ptToScene(vnMinusOne)[1]);
          iter++;
        }

        break;
      }
      case "Burning Ship": {
        int iter = 0;
        Complex constant = new Complex(c_re, c_im);
        Complex vn = constant;
        Complex vnMinusOne;

        while(iter <= MAX_ITER) {
          double reZ = Math.abs(vn.getReal());
          double imZ = Math.abs(vn.getImaginary());
          vnMinusOne = vn;
          vn = new Complex(reZ, imZ).pow(2.0).add(constant);
          gc.strokeLine(ptToScene(vn)[0], ptToScene(vn)[1], ptToScene(vnMinusOne)[0], ptToScene(vnMinusOne)[1]);
          iter++;
        }

        break;
      }
    }
  }

  @Override
  public String toString() {
    return ptToSet(c_re, c_im)[0] + "," + ptToSet(c_re, c_im)[1];
  }
}
