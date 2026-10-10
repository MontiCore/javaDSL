/* (c) https://github.com/MontiCore/monticore */
package calculator;

import calculator.Expr.Binary;
import calculator.Expr.Neg;
import calculator.Expr.Num;
import calculator.Expr.Operator;
import calculator.Expr.Var;

import java.util.Map;

/** Evaluates, simplifies, and prints expressions using pattern matching (Java 21). */
public final class Evaluator {

  private final Map<String, Double> variables;

  public Evaluator(Map<String, Double> variables) {
    this.variables = variables;
  }

  public double evaluate(Expr expr) {
    return switch (expr) {
      case Num(double value) -> value;
      case Var(String name) when variables.containsKey(name) -> variables.get(name);
      case Var var -> throw new IllegalArgumentException("unknown variable " + var.name());
      case Neg(Expr operand) -> -evaluate(operand);
      case Binary(Operator operator, Expr left, Expr right) -> {
        double l = evaluate(left);
        double r = evaluate(right);
        yield switch (operator) {
          case PLUS -> l + r;
          case MINUS -> l - r;
          case TIMES -> l * r;
          case DIVIDE -> {
            if (r == 0) {
              throw new ArithmeticException("division by zero");
            }
            yield l / r;
          }
        };
      }
    };
  }

  public static Expr simplify(Expr expr) {
    if (expr instanceof Binary(var operator, Num(var a), Num(var b))
        && operator != Operator.DIVIDE) {
      return new Num(switch (operator) {
        case PLUS -> a + b;
        case MINUS -> a - b;
        case TIMES -> a * b;
        default -> throw new IllegalStateException();
      });
    }
    if (expr instanceof Neg(Neg(var inner))) {
      return simplify(inner);
    }
    return expr;
  }

  public static String print(Expr expr) {
    return switch (expr) {
      case Num n when n.value() == Math.rint(n.value()) -> String.valueOf((long) n.value());
      case Num n -> String.valueOf(n.value());
      case Var v -> v.name();
      case Neg(var operand) -> "-" + print(operand);
      case Binary b -> "(" + print(b.left()) + " " + b.operator().symbol() + " "
          + print(b.right()) + ")";
    };
  }

  public static void main(String[] args) {
    Expr expr = Expr.plus(Expr.num(2), Expr.times(new Var("x"), Expr.num(3)));
    var evaluator = new Evaluator(Map.of("x", 4.0));
    String text = """
        %s = %s
        """.formatted(print(expr), evaluator.evaluate(expr));
    System.out.print(text);
    Object o = simplify(Expr.plus(Expr.num(1), Expr.num(2)));
    if (o instanceof Num num && num.value() > 2) {
      System.out.println(print(num));
    }
  }
}
