/* (c) https://github.com/MontiCore/monticore */
package geometry;

public class Point {

  public static final Point ORIGIN = new Point(0, 0);

  private final double x;

  private final double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double distanceTo(Point other) {
    double dx = x - other.x;
    double dy = y - other.y;
    return Math.sqrt(dx * dx + dy * dy);
  }

  public Point translate(double dx, double dy) {
    return new Point(x + dx, y + dy);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Point)) {
      return false;
    }
    Point other = (Point) obj;
    return x == other.x && y == other.y;
  }

  @Override
  public int hashCode() {
    return Double.hashCode(x) * 31 + Double.hashCode(y);
  }
}
