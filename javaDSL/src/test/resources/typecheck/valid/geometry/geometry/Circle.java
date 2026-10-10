/* (c) https://github.com/MontiCore/monticore */
package geometry;

public final class Circle implements Shape {

  private final Point center;

  private final double radius;

  public Circle(Point center, double radius) {
    this.center = center;
    this.radius = radius;
  }

  public Point getCenter() {
    return center;
  }

  @Override
  public double area() {
    return Math.PI * radius * radius;
  }

  @Override
  public double perimeter() {
    return 2 * Math.PI * radius;
  }

  public boolean contains(Point point) {
    return center.distanceTo(point) <= radius;
  }
}
