package statements;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class ResourceInTryStatementCloseable {
  
  public void method() throws FileNotFoundException {
    
    try (PrintStream printStream = new PrintStream("");
         PrintStreamEx printStreamEx = new PrintStreamEx("")) {
      
    } catch(FileNotFoundException e) {
      
    }
  }
}

class PrintStreamEx extends PrintStream {
  
  public PrintStreamEx(String string) throws FileNotFoundException {
    super(string);
  }
}