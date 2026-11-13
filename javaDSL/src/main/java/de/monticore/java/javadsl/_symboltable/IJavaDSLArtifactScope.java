package de.monticore.java.javadsl._symboltable;

import com.google.common.collect.FluentIterable;
import de.monticore.symboltable.ImportStatement;
import de.se_rwth.commons.Splitters;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.se_rwth.commons.Names.getQualifier;
import static de.se_rwth.commons.Names.getSimpleName;
import static de.se_rwth.commons.logging.Log.trace;

public interface IJavaDSLArtifactScope extends IJavaDSLArtifactScopeTOP {
  
  @Override
  default Set<String> calculateQualifiedNames(String name, String packageName,
      List<ImportStatement> imports) {
    final Set<String> potentialSymbolNames = new LinkedHashSet<>();
    
    // the simple name (in default package)
    potentialSymbolNames.add(name);
    
    // maybe the model belongs to the same package
    if (!packageName.isEmpty()) {
      potentialSymbolNames.add(packageName + "." + name);
    }
    
    for (ImportStatement importStatement : imports) {
      if (importStatement.isStar()) {
        potentialSymbolNames.add(importStatement.getStatement() + "." + name);
      }
      else if (getSimpleName(importStatement.getStatement()).equals(name)) {
        potentialSymbolNames.add(importStatement.getStatement());
      }
      else if (getSimpleName(importStatement.getStatement()).equals(getFirstQualifier(name))) {
        potentialSymbolNames.add(getQualifier(importStatement.getStatement()) + "." + name);
      }
    }
    trace("Potential qualified names for \"" + name + "\": " + potentialSymbolNames.toString(),
        "IArtifactScope");
    
    return potentialSymbolNames;
  }
  
  /**
   * @return The first part of the given qualified name (e.g. "a" from
   * "a.b.c.Name"). Leading or trailing dots are ignored.
   */
  default String getFirstQualifier(String qualifiedName) {
    
    checkNotNull(qualifiedName);
    
    FluentIterable<String> parts = FluentIterable.from(
        Splitters.DOT.split(qualifiedName));
    
    return parts.first().get();
  }
}
