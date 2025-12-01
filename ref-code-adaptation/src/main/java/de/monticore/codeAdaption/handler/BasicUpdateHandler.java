package de.monticore.codeAdaption.handler;

import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._symboltable.CDTypeSymbol;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.cddiff.CDDiffUtil;
import de.monticore.codeAdaption.matcher.*;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symboltable.ISymbol;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.nio.file.Path;
import java.util.*;
// TODO: 20.09.2023 handle concrete handwritten-code

/***
 * this class is to handle is to handle the update operation.
 * -
 * 1- iterate over all adaptable code-elements (Type, field, method,local variable,formal parameters-names).
 * 2- get the match of each element in the reference class diagram.
 * 3- compute the concrete value of each element form the matching using the conformance checker.
 * 4- use the updater to update the value in the code.
 * -
 * this handler assumes that each reference element has a unique incarnation.
 */
public class BasicUpdateHandler {
  protected ASTCDCompilationUnit conCD;
  protected ASTCDCompilationUnit refCD;
  protected CDConformanceChecker checker;
  protected CodeUpdater updater;

  protected CodeValidator validator;

  public BasicUpdateHandler(
      ASTCDCompilationUnit refCD,
      ASTCDCompilationUnit conCD,
      Path conHwcPath,
      CDConformanceChecker checker,
      CodeUpdater updater,
      CodeValidator validator) {
    this.updater = updater;
    this.checker = checker;
    this.conCD = conCD;
    this.refCD = refCD;
    this.validator = validator;
  }

  public void handleUpdate(Set<ASTOrdinaryCompilationUnit> javaFiles) {

    // Collect elements of each type
    Set<JavaAstElemCollector> typeElements = new LinkedHashSet<>();
    for (ASTOrdinaryCompilationUnit ast : javaFiles) {

      // collect code elements
      JavaAstElemCollector collector = new JavaAstElemCollector();
      JavaDSLTraverser traverser = JavaDSLMill.traverser();
      traverser.add4JavaDSL(collector);
      ast.accept(traverser);
      typeElements.add(collector);
    }

    // update local variable and method parameters
    typeElements.forEach(this::handleVariableUpdate);

    // update methods and attributes
    typeElements.forEach(this::handleTMemberUpdate);

    // update types
    typeElements.forEach(this::handleTypeUpdate);

    updater.printCode();
  }

  protected void handleTypeUpdate(JavaAstElemCollector collector) {
    // update type present in the reference code
    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {
      Optional<CodeMatching> matching = validator.getMatchedType(type);
      if (matching.isPresent() && matching.get().mustBePerform()) {
        String newName = buildConcreteName(matching.get());
        updater.updateType(type, newName);
      }
    }

    // update type not present in the reference code
    for (ASTCDType cdType : CDDiffUtil.getAllCDTypes(refCD)) {
      String newName = getConTypeSymbol(cdType.getSymbol()).getName();
      updater.updateCDType(cdType, newName);
    }
  }

  protected void handleTMemberUpdate(JavaAstElemCollector collector) {
    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {

      // update method names
      for (ASTMethodDeclaration method : collector.getAllMethodDeclarations(type)) {
        Optional<CodeMatching> matching = validator.getMatchedMethod(type, method);
        if (matching.isPresent() && matching.get().mustBePerform()) {
          String newName = buildConcreteName(matching.get());
          updater.updateMethod(type, method, newName);
        }
      }

      // update type parameters names
      for (ASTFieldDeclaration field : collector.getAllFieldDeclarations(type)) {
        Optional<CodeMatching> matching = validator.getMatchedField(type, field);
        if (matching.isPresent() && matching.get().mustBePerform()) {
          String newName = buildConcreteName(matching.get());
          updater.updateField(type, field, newName);
        }
      }

      // update type parameters names
      for (ASTMCType supertype : collector.getAllFSuperTypeDeclarations(type)) {
        Optional<CodeMatching> matching = validator.getMatchedSupertype(type, supertype);
        if (matching.isPresent() && matching.get().mustBePerform()) {
          String newName = buildConcreteName(matching.get());
          updater.updateSuperType(type, supertype, newName);
        }
      }
    }
  }

  protected void handleVariableUpdate(JavaAstElemCollector collector) {
    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {
      for (ASTMethodDeclaration method : collector.getAllMethodDeclarations(type)) {

        // update local variables
        for (ASTLocalVariableDeclaration var : collector.getAllLocVariables(type, method)) {
          Optional<CodeMatching> matching = validator.getMatchedLocalVariable(type, method, var);
          if (matching.isPresent() && matching.get().mustBePerform()) {
            String newName = buildConcreteName(matching.get());
            updater.updateLocalVariable(type, method, var, newName);
          }
        }

        // update all formal parameters
        for (ASTFormalParameter param : collector.getAllParameters(type, method)) {
          Optional<CodeMatching> matching = validator.getMatchedParameter(type, method, param);
          if (matching.isPresent() && matching.get().mustBePerform()) {
            String newName = buildConcreteName(matching.get());
            updater.updateMethodParameter(type, method, param, newName);
          }
        }
      }
    }
  }

  /***
   * build concrete name of an element form the matching found in the class diagram
   */
  private String buildConcreteName(CodeMatching codeMatching) {
    List<ISymbol> conReferences = new ArrayList<>();

    // resolve concrete references
    for (ISymbol refSymbol : codeMatching.getReferences()) {
      if (refSymbol instanceof CDTypeSymbol) {
        conReferences.add(getConTypeSymbol((CDTypeSymbol) refSymbol));
      } else {
        conReferences.add(getConAttributeSymbol((FieldSymbol) refSymbol));
      }
    }
    // fill the template with the references
    return MatcherHelper.fillTemplate(codeMatching.getTemplate(), conReferences);
  }

  protected ISymbol getConTypeSymbol(CDTypeSymbol symbol) {
    return checker
        .getIncarnationMapping()
        .getIncarnations(symbol.getAstNode())
        .iterator()
        .next()
        .getSymbol();
  }

  protected ISymbol getConAttributeSymbol(FieldSymbol symbol) {

    return checker
        .getIncarnationMapping()
        .getIncarnations((ASTCDAttribute) symbol.getAstNode())
        .iterator()
        .next()
        .getSymbol();
  }
}
