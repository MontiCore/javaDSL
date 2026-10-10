/* (c) https://github.com/MontiCore/monticore */
package calculator;

/** Arithmetic expressions as algebraic data type (Java 21: sealed types and records). */
public sealed interface Expr permits Expr.Num, Expr.Var, Expr.Neg, Expr.Binary {

  record Num(double value) implements Expr { }

  record Var(String name) implements Expr { }

  record Neg(Expr operand) implements Expr { }

  record Binary(Operator operator, Expr left, Expr right) implements Expr { }

  enum Operator {
    PLUS("+"), MINUS("-"), TIMES("*"), DIVIDE("/");

    private final String symbol;

    Operator(String symbol) {
      this.symbol = symbol;
    }

    public String symbol() {
      return symbol;
    }
  }

  static Expr num(double value) {
    return new Num(value);
  }

  static Expr plus(Expr left, Expr right) {
    return new Binary(Operator.PLUS, left, right);
  }

  static Expr times(Expr left, Expr right) {
    return new Binary(Operator.TIMES, left, right);
  }
}
