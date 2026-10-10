/* (c) https://github.com/MontiCore/monticore */
package geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** Utility methods for collections of shapes. */
public final class Shapes {

  private Shapes() {
  }

  public static double totalArea(Collection<? extends Shape> shapes) {
    double total = 0;
    for (Shape shape : shapes) {
      total += shape.area();
    }
    return total;
  }

  public static <T extends Comparable<? super T>> T largest(List<T> elements) {
    T result = elements.get(0);
    for (T element : elements) {
      if (element.compareTo(result) > 0) {
        result = element;
      }
    }
    return result;
  }

  public static <S extends Shape> List<S> filter(List<S> shapes, Predicate<? super S> condition) {
    List<S> result = new ArrayList<>();
    for (S shape : shapes) {
      if (condition.test(shape)) {
        result.add(shape);
      }
    }
    return result;
  }

  public static void addCircles(List<? super Circle> target, int count) {
    for (int i = 0; i < count; i++) {
      target.add(new Circle(Point.ORIGIN.translate(i, i), i + 1));
    }
  }

  public static Map<String, List<Shape>> groupByName(List<Shape> shapes) {
    Map<String, List<Shape>> groups = new HashMap<>();
    for (Shape shape : shapes) {
      List<Shape> group = groups.get(shape.name());
      if (group == null) {
        group = new ArrayList<>();
        groups.put(shape.name(), group);
      }
      group.add(shape);
    }
    return groups;
  }

  public static Pair<Shape, Double> withPerimeter(Shape shape) {
    return Pair.of(shape, shape.perimeter());
  }

  public static void main(String[] args) {
    List<Shape> shapes = new ArrayList<>();
    shapes.add(new Circle(new Point(1, 2), 3));
    shapes.add(new Polygon(Point.ORIGIN, new Point(4, 0), new Point(4, 3)));
    addCircles(shapes, 2);

    Shape largest = largest(shapes);
    List<Shape> small = filter(shapes, shape -> shape.area() < 10);
    shapes.sort(Comparator.comparingDouble(Shape::perimeter));

    Pair<Shape, Double> pair = withPerimeter(largest);
    Pair<Double, Shape> swapped = pair.swap();
    double perimeter = swapped.getFirst();
    System.out.println(largest.name() + " " + perimeter + " " + small.size());

    for (Map.Entry<String, List<Shape>> entry : groupByName(shapes).entrySet()) {
      System.out.println(entry.getKey() + ": " + entry.getValue().size());
    }

    int[] counts = new int[3];
    for (Shape shape : shapes) {
      int index = shape instanceof Circle ? 0 : shape instanceof Polygon ? 1 : 2;
      counts[index]++;
    }
    System.out.println(totalArea(shapes) + " " + counts.length);
  }
}
