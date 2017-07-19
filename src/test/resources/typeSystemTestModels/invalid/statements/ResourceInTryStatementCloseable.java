package statements;

public class ResourceInTryStatementCloseable {
  
  public void method1() {
    
    try (Integer i = 1;
         Object o = new Object()) {
      
    }
  }
}
