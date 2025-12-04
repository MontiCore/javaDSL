package de.monticore.java.javadsl.types3;

import com.google.common.base.Preconditions;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;
import de.monticore.symboltable.IScopeSpanningSymbol;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfFunction;
import de.monticore.types3.util.OOWithinTypeBasicSymbolsResolver;
import de.monticore.types3.util.WithinTypeBasicSymbolsResolver;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JavaDSLOOWithinTypeBasicSymbolsResolver extends OOWithinTypeBasicSymbolsResolver {
  
  @Override
  protected List<SymTypeOfFunction> resolvedFunctionsInSuperTypes(SymTypeExpression thisType,
      String name, AccessModifier accessModifier, Predicate<FunctionSymbol> predicate) {
    List<SymTypeOfFunction> resolvedFuncsInSuperTypes =
        super.resolvedFunctionsInSuperTypes(thisType, name, accessModifier, predicate);
    List<SymTypeOfFunction> filteredFuncs =
        filterMultipleInterfaceImplementation(resolvedFuncsInSuperTypes);
    
    return filteredFuncs;
  }
  
  protected List<SymTypeOfFunction> filterMultipleInterfaceImplementation(
      List<SymTypeOfFunction> funcs) {
    if (funcs.size() < 2) {
      return funcs;
    }
    List<SymTypeOfFunction> functionInheritedFromClasses = funcs.stream()
        .filter(x -> getEnclosingType(x).isPresent() && getEnclosingType(x).get().isIsClass())
        .collect(Collectors.toList());
    if (functionInheritedFromClasses.isEmpty()) {
      return funcs;
    }
    else if (functionInheritedFromClasses.size() > 1) {
      Log.error("0x7A017: Can not filter inherited functions with multiple superclasses present.");
      return funcs;
    }
    else {
      SymTypeOfFunction classFunc = functionInheritedFromClasses.get(0);
      Optional<OOTypeSymbol> classFuncSymbol = getEnclosingType(classFunc);
      if (classFuncSymbol.isEmpty()) {
        Log.error("0x7A018: Enclosing type of classFunc is empty!");
        return funcs;
      }
      List<SymTypeExpression> classFuncSuperTypes = classFuncSymbol.get().getSuperTypesList();
      List<OOTypeSymbol> classFuncSuperTypeSymbols =
          getOOTypeSymbolsIfAvailable(classFuncSuperTypes);
      List<SymTypeOfFunction> functionInheritedFromInterfaces = funcs.stream()
          .filter(x -> getEnclosingType(x).isPresent() && getEnclosingType(x).get().isIsInterface())
          .collect(Collectors.toList());
      List<SymTypeOfFunction> filteredFuncs = new ArrayList<>();
      filteredFuncs.add(classFunc);
      for (SymTypeOfFunction func : functionInheritedFromInterfaces) {
        Optional<OOTypeSymbol> funcInterface = getEnclosingType(func);
        if (funcInterface.isPresent()) {
          if (!isInterfaceContainedInSuperClass(classFuncSuperTypeSymbols, funcInterface.get())) {
            filteredFuncs.add(func);
          }
        }
      }
      return filteredFuncs;
    }
  }
  
  protected List<OOTypeSymbol> getOOTypeSymbolsIfAvailable(List<SymTypeExpression> exprs) {
    // TODO: Replace with TypeDispatcher when fixed
    return exprs.stream().filter(SymTypeExpression::hasTypeInfo).map(SymTypeExpression::getTypeInfo)
        .filter(x -> x instanceof OOTypeSymbol).map(x -> (OOTypeSymbol) x)
        .collect(Collectors.toList());
  }
  
  protected boolean isInterfaceContainedInSuperClass(List<OOTypeSymbol> parents,
      OOTypeSymbol testingType) {
    if (parents.stream().anyMatch(x -> x.equals(testingType))) {
      return true;
    }
    else {
      for (OOTypeSymbol parent : parents) {
        List<OOTypeSymbol> newParents = getOOTypeSymbolsIfAvailable(parent.getSuperTypesList());
        if (isInterfaceContainedInSuperClass(newParents, testingType)) {
          return true;
        }
      }
      return false;
    }
  }
  
  protected Optional<OOTypeSymbol> getEnclosingType(SymTypeOfFunction func) {
    if (func.hasSymbol()) {
      IScopeSpanningSymbol spanningSymbol =
          func.getSymbol().getEnclosingScope().getSpanningSymbol();
      if (spanningSymbol instanceof OOTypeSymbol) {
        return Optional.of((OOTypeSymbol) spanningSymbol);
      }
    }
    return Optional.empty();
  }
  
  // static delegate
  
  public static void init() {
    Log.trace("init JavaDSLOOWithinTypeBasicSymbolsResolver", "TypeCheck setup");
    setDelegate(new JavaDSLOOWithinTypeBasicSymbolsResolver());
  }
  
  public static void reset() {
    OOWithinTypeBasicSymbolsResolver.delegate = null;
    WithinTypeBasicSymbolsResolver.reset();
  }
  
  protected static void setDelegate(JavaDSLOOWithinTypeBasicSymbolsResolver newDelegate) {
    OOWithinTypeBasicSymbolsResolver.delegate = Preconditions.checkNotNull(newDelegate);
    WithinTypeBasicSymbolsResolver.setDelegate(newDelegate);
  }
  
  protected static OOWithinTypeBasicSymbolsResolver getDelegate() {
    if (OOWithinTypeBasicSymbolsResolver.delegate == null) {
      init();
    }
    return OOWithinTypeBasicSymbolsResolver.delegate;
  }
}
