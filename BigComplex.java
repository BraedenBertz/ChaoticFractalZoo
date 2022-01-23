package sample;

import java.math.BigDecimal;

public class BigComplex {
  BigDecimal real;
  BigDecimal imaginary;

  public BigComplex(){
    this(new BigDecimal(0), new BigDecimal(0));
  }

  public BigComplex(BigDecimal real, BigDecimal imaginary){
    this.real = real.setScale(20, BigDecimal.ROUND_UP);
    this.imaginary = imaginary.setScale(20, BigDecimal.ROUND_UP);
  }

  public BigComplex square(){
    BigComplex tempR = this;
    BigDecimal newR = real.pow(2).subtract(imaginary.pow(2));
    BigDecimal newIm = real.multiply(imaginary).multiply(new BigDecimal(2));
    return new BigComplex(newR, newIm);
  }

  public BigComplex add(BigComplex addend){
    return new BigComplex(this.real.add(addend.real), this.imaginary.add(addend.imaginary));
  }

  public BigComplex sub(BigComplex addend){
    return new BigComplex(this.real.subtract(addend.real), this.imaginary.subtract(addend.imaginary));
  }

  public BigDecimal getReal() {
    return real;
  }

  public BigDecimal getImaginary() {
    return imaginary;
  }

  @Override
  public String toString() {
    return "("+real.toString()+", "+imaginary.toString()+")";
  }
}
