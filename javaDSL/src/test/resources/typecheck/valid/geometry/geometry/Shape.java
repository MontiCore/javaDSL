/* (c) https://github.com/MontiCore/monticore */
package geometry;

public interface Shape extends Comparable<Shape> {

  double area();

  double perimeter();

  default String name() {
    return getClass().getSimpleName();
  }

  @Override
  default int compareTo(Shape other) {
    return Double.compare(area(), other.area());
  }
}
