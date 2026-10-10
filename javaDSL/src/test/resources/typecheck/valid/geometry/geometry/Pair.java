/* (c) https://github.com/MontiCore/monticore */
package geometry;

public class Pair<A, B> {

  private final A first;

  private final B second;

  public Pair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  public A getFirst() {
    return first;
  }

  public B getSecond() {
    return second;
  }

  public Pair<B, A> swap() {
    return new Pair<>(second, first);
  }

  public static <X, Y> Pair<X, Y> of(X first, Y second) {
    return new Pair<>(first, second);
  }
}
