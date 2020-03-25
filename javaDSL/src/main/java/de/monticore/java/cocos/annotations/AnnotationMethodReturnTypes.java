/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.cocos.annotations;

import de.monticore.java.javadsl._ast.ASTAnnotationMethod;
import de.monticore.java.javadsl._cocos.JavaDSLASTAnnotationMethodCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class AnnotationMethodReturnTypes implements JavaDSLASTAnnotationMethodCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  // JLS3_9.6-2
  public AnnotationMethodReturnTypes(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTAnnotationMethod node) {
    node.getMCType().accept(typeResolver);
    JavaTypeSymbolReference type = typeResolver.getResult().get();
    if ("String".equals(type.getName()) || "Class".equals(type.getName())
        || "java.lang.String".equals(type.getName())
        || "java.lang.Class".equals(type.getName())) {
      return;
    }
    if (JavaDSLHelper.isPrimitiveType(type)) {
      return;
    }
    if (JavaDSLHelper.isEnum(type)) {
      return;
    }
    if (JavaDSLHelper.isAnnotation(type)) {
      return;
    }
    else {
      Log.error("0xA0110 return type of a method in an annotation type declaration " +
          "may only be a primitive, String, Class, an Annotation or an " +
          "Enum at method '" + node.getName() + "'.",
          node.get_SourcePositionStart());
    }
    
  }
}
