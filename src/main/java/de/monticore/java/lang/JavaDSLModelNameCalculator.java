/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.lang;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import de.monticore.CommonModelNameCalculator;
import de.monticore.java.symboltable.JavaFieldSymbol;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.symboltable.SymbolKind;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.Splitters;

public class JavaDSLModelNameCalculator extends CommonModelNameCalculator {

  @Override
  public Set<String> calculateModelNames(final String name, final SymbolKind kind) {
    if (JavaTypeSymbol.KIND.isKindOf(kind)) {
      // TODO PN also calculate for inner classes
      return calculateModelNamesForTypeSymbol(name);
    }
    else if (JavaFieldSymbol.KIND.isKindOf(kind)) {
      return calculateModelNamesForFieldSymbol(name);
    }
    else if (JavaMethodSymbol.KIND.isKindOf(kind)) {
      return calculateModelNamesForMethodSymbol(name);
    }

    return Collections.emptySet();
  }

  protected Set<String> calculateModelNamesForTypeSymbol(String name) {
    return ImmutableSet.of(name);
  }

  protected Set<String> calculateModelNamesForFieldSymbol(String name) {
    return calculateModelNamesForFieldOrMethodSymbol(name);
  }

  protected Set<String> calculateModelNamesForMethodSymbol(String name) {
    return calculateModelNamesForFieldOrMethodSymbol(name);
  }

  protected Set<String> calculateModelNamesForFieldOrMethodSymbol(String name) {
    // e.g., if p.C.f return p.C
    List<String> nameParts = Splitters.DOT.splitToList(name);

    // at least 2, because of C.f
    if (nameParts.size() >= 2) {
      // cut the last name part (e.g., f)
      return ImmutableSet.of(Joiners.DOT.join(nameParts.subList(0, nameParts.size()-1)));
    }
    return Collections.emptySet();
  }
}
