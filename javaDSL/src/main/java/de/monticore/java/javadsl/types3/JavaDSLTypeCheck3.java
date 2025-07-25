package de.monticore.java.javadsl.types3;

import de.monticore.expressions.assignmentexpressions.types3.AssignmentExpressionsCTTIVisitor;
import de.monticore.expressions.commonexpressions.types3.CommonExpressionsCTTIVisitor;
import de.monticore.expressions.expressionsbasis.types3.ExpressionBasisCTTIVisitor;
import de.monticore.expressions.uglyexpressions.types3.UglyExpressionsCTTIVisitor;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.literals.mccommonliterals.types3.MCCommonLiteralsTypeVisitor;
import de.monticore.literals.mcjavaliterals.types3.MCJavaLiteralsTypeVisitor;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.types.mcbasictypes.types3.MCBasicTypesTypeVisitor;
import de.monticore.types.mccollectiontypes.types3.MCCollectionTypesTypeVisitor;
import de.monticore.types.mcfullgenerictypes.types3.MCFullGenericTypesTypeVisitor;
import de.monticore.types.mcsimplegenerictypes.types3.MCSimpleGenericTypesTypeVisitor;
import de.monticore.types3.Type4Ast;
import de.monticore.types3.generics.context.InferenceContext4Ast;
import de.monticore.types3.util.*;
import de.monticore.visitor.ITraverser;
import de.se_rwth.commons.logging.Log;

public class JavaDSLTypeCheck3 extends MapBasedTypeCheck3 {
  
  public static void init(){
    Log.trace("init JavaDSLTypeCheck3", "TypeCheck setup");
    
    // initialize static delegates
    BasicSymbolsMill.initializePrimitives();
    OOWithinScopeBasicSymbolsResolver.init();
    OOWithinTypeBasicSymbolsResolver.init();
    TypeContextCalculator.init();
    TypeVisitorOperatorCalculator.init();
    TypeVisitorLifting.init();
    FunctionRelations.init();
    
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    Type4Ast type4Ast = new Type4Ast();
    InferenceContext4Ast ctx4Ast = new InferenceContext4Ast();
    
    JavaDSLTypeVisitor visJavaDSL = new JavaDSLTypeVisitor();
    visJavaDSL.setType4Ast(type4Ast);
    traverser.add4JavaDSL(visJavaDSL);
    
    JavaDSLArrayTypesTypeVisitor visJavaDSLArrayTypesType = new JavaDSLArrayTypesTypeVisitor();
    visJavaDSLArrayTypesType.setType4Ast(type4Ast);
    traverser.add4JavaDSL(visJavaDSLArrayTypesType);
    
    JavaDSLSimpleGenericTypesTypeVisitor visSimpleGenericTypesType = new JavaDSLSimpleGenericTypesTypeVisitor();
    visSimpleGenericTypesType.setType4Ast(type4Ast);
    traverser.add4JavaDSL(visSimpleGenericTypesType);
    
    // Literals
    MCJavaLiteralsTypeVisitor visMCJavaLiterals = new MCJavaLiteralsTypeVisitor();
    visMCJavaLiterals.setType4Ast(type4Ast);
    traverser.add4MCJavaLiterals(visMCJavaLiterals);
    
    MCCommonLiteralsTypeVisitor visMCCommonLiterals = new MCCommonLiteralsTypeVisitor();
    visMCCommonLiterals.setType4Ast(type4Ast);
    traverser.add4MCCommonLiterals(visMCCommonLiterals);
    
    
    // Expressions
    
    AssignmentExpressionsCTTIVisitor visAssignmentExpressions = new AssignmentExpressionsCTTIVisitor();
    visAssignmentExpressions.setType4Ast(type4Ast);
    visAssignmentExpressions.setContext4Ast(ctx4Ast);
    traverser.add4AssignmentExpressions(visAssignmentExpressions);
    traverser.setAssignmentExpressionsHandler(visAssignmentExpressions);
    
    CommonExpressionsCTTIVisitor visCommonExpressions = new CommonExpressionsCTTIVisitor();
    visCommonExpressions.setType4Ast(type4Ast);
    visCommonExpressions.setContext4Ast(ctx4Ast);
    traverser.add4CommonExpressions(visCommonExpressions);
    traverser.setCommonExpressionsHandler(visCommonExpressions);
    
    ExpressionBasisCTTIVisitor visExpressionBasis = new ExpressionBasisCTTIVisitor();
    visExpressionBasis.setType4Ast(type4Ast);
    visExpressionBasis.setContext4Ast(ctx4Ast);
    traverser.add4ExpressionsBasis(visExpressionBasis);
    traverser.setExpressionsBasisHandler(visExpressionBasis);
    
    UglyExpressionsCTTIVisitor visUglyExpressions = new UglyExpressionsCTTIVisitor();
    visUglyExpressions.setType4Ast(type4Ast);
    visUglyExpressions.setContext4Ast(ctx4Ast);
    traverser.add4UglyExpressions(visUglyExpressions);
    traverser.setUglyExpressionsHandler(visUglyExpressions);
    
    // MCTypes
    
    MCBasicTypesTypeVisitor visMCBasicTypes = new MCBasicTypesTypeVisitor();
    visMCBasicTypes.setType4Ast(type4Ast);
    traverser.add4MCBasicTypes(visMCBasicTypes);
    
    MCCollectionTypesTypeVisitor visMCCollectionTypes = new MCCollectionTypesTypeVisitor();
    visMCCollectionTypes.setType4Ast(type4Ast);
    traverser.add4MCCollectionTypes(visMCCollectionTypes);
    
    MCSimpleGenericTypesTypeVisitor visMCSimpleGenericTypes = new MCSimpleGenericTypesTypeVisitor();
    visMCSimpleGenericTypes.setType4Ast(type4Ast);
    traverser.add4MCSimpleGenericTypes(visMCSimpleGenericTypes);
    
    MCFullGenericTypesTypeVisitor visMCFullGenericTypes = new MCFullGenericTypesTypeVisitor();
    visMCFullGenericTypes.setType4Ast(type4Ast);
    traverser.add4MCFullGenericTypes(visMCFullGenericTypes);
    
    JavaDSLTypeCheck3 javaDSLTC3 = new JavaDSLTypeCheck3(traverser, type4Ast, ctx4Ast);
    javaDSLTC3.setThisAsDelegate();
  }
  
  public static void reset() {
    Log.trace("reset JavaDSLTypeCheck3", "TypeCheck setup");
    JavaDSLTypeCheck3.resetDelegate();
    OOWithinScopeBasicSymbolsResolver.reset();
    OOWithinTypeBasicSymbolsResolver.reset();
    TypeContextCalculator.reset();
    TypeVisitorOperatorCalculator.reset();
    TypeVisitorLifting.reset();
    FunctionRelations.reset();
  }
  
  protected JavaDSLTypeCheck3(ITraverser typeTraverser, Type4Ast type4Ast, InferenceContext4Ast ctx4Ast){
    super(typeTraverser, type4Ast, ctx4Ast);
  }
}
