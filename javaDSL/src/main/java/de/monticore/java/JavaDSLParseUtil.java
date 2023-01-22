package de.monticore.java;

import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class JavaDSLParseUtil {

  public static void main(String[] args) throws IOException {
    Log.enableFailQuick(false);
    List<Path> paths = Files
        .walk(Paths.get(args[0]))
        .filter(p -> p.toFile().isFile())
        .filter(p -> p.toFile().getName().endsWith(".java"))
        .collect(Collectors.toList());

    int errors = 0;
    for (Path path : paths) {
      try {
        Log.getFindings().clear();
        System.out.println("Parsing " + path);
        JavaDSLTool.loadArtifact(path);
        if (!Log.getFindings().isEmpty()) {
          errors++;
        }
      }catch (Exception e){
        e.printStackTrace();
        errors++;
      }finally {
        System.out.println("Findings: " + Log.getFindings().size());
        for (Finding finding : Log.getFindings()) {
          System.out.println(finding.toString());
        }
      }
    }

    System.out.println("\nErrors: " + errors + " of " + paths.size());
  }

}
