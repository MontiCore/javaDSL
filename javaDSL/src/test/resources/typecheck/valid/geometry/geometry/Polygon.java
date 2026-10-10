/* (c) https://github.com/MontiCore/monticore */
package geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** A polygon given by its corners; it can be iterated over its edges. */
public class Polygon implements Shape, Iterable<Polygon.Edge> {

  /** An edge between two corners. */
  public static class Edge {

    public final Point from;

    public final Point to;

    public Edge(Point from, Point to) {
      this.from = from;
      this.to = to;
    }

    public double length() {
      return from.distanceTo(to);
    }
  }

  private final List<Point> corners;

  public Polygon(Point... corners) {
    if (corners.length < 3) {
      throw new IllegalArgumentException("a polygon has at least three corners");
    }
    this.corners = new ArrayList<>(Arrays.asList(corners));
  }

  public int size() {
    return corners.size();
  }

  @Override
  public Iterator<Edge> iterator() {
    return new EdgeIterator();
  }

  @Override
  public double area() {
    double sum = 0;
    for (Edge edge : this) {
      sum += edge.from.getX() * edge.to.getY() - edge.to.getX() * edge.from.getY();
    }
    return Math.abs(sum) / 2;
  }

  @Override
  public double perimeter() {
    double sum = 0;
    for (Edge edge : this) {
      sum += edge.length();
    }
    return sum;
  }

  /** Iterates the edges; an inner class that accesses the corners of the polygon. */
  private class EdgeIterator implements Iterator<Edge> {

    private int index = 0;

    @Override
    public boolean hasNext() {
      return index < corners.size();
    }

    @Override
    public Edge next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      Point from = corners.get(index);
      Point to = corners.get((index + 1) % corners.size());
      index++;
      return new Edge(from, to);
    }
  }
}
