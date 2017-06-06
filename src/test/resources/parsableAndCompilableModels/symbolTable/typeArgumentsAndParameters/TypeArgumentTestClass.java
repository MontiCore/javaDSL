package symbolTable.typeArgumentsAndParameters;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class TypeArgumentTestClass<T> {
  Consumer<?> wildcardOnly = new MyConsumer<Integer>();

  Consumer<T> typeVariableOnly = new MyConsumer<T>();

  Consumer<? super T> superTypeVariable = new MyConsumer<T>();

  Consumer<? extends T> extendsTypeVariable = new MyConsumer<T>();

  Consumer<? super String> superReferenceType = new MyConsumer<String>();

  Consumer<? extends String> extendsReferenceType = new MyConsumer<String>();

  Consumer<? super Integer[]> superIntegerArrayType = new MyConsumer<Integer[]>();

  Consumer<? extends Integer[]> extendsIntegerArrayType = new MyConsumer<Integer[]>();

  Consumer<? super int[]> superIntArrayType = new MyConsumer<int[]>();

  Consumer<? extends int[]> extendsIntArrayType = new MyConsumer<int[]>();

  Function<Collection<?>, Set<?>> multipleAndRecursiveTypeArguments = new MyFunction<Collection<?>, Set<?>>();
}

class MyConsumer<T> implements Consumer<T> {
  public void accept(T t) {
  }
}

class MyFunction<T, R> implements Function<T, R> {
  public R apply(T t) {
    return null;
  }
}
