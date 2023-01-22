/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.reporting;

import java.util.List;

import de.monticore.ast.ASTNode;
import de.monticore.generating.templateengine.reporting.commons.ASTNodeIdentHelper;
import de.monticore.generating.templateengine.reporting.commons.Layouter;
import de.monticore.java.javadsl._ast.*;
import de.monticore.javalight._ast.ASTConstructorDeclaration;
import de.monticore.javalight._ast.ASTElementValuePair;
import de.monticore.javalight._ast.ASTLastFormalParameter;
import de.monticore.literals.mccommonliterals._ast.ASTStringLiteral;
import de.monticore.literals.mcjavaliterals._ast.ASTIntLiteral;
import de.monticore.statements.mccommonstatements._ast.ASTEnumConstantSwitchLabel;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTDeclaratorId;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTVariableDeclarator;
import de.monticore.types.mcbasictypes.MCBasicTypesMill;
import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedName;

<<<<<<< HEAD:javaDSL/src/main/java/de/monticore/java/report/JavaDSLNodeIdentHelper.java
/**
 */
public class JavaDSLNodeIdentHelper extends TypesNodeIdentHelper {
=======
public class JavaDSLNodeIdentHelper extends ASTNodeIdentHelper {
>>>>>>> b1632b992b7e88612c226e7c337d14e8cbff6536:javaDSL/src/main/java/de/monticore/java/reporting/JavaDSLNodeIdentHelper.java
  
  public String getIdent(ASTClassDeclaration a) {
    String type = Layouter.nodeName(a);
    String name = a.getName();
    return format(name, type);
  }
  
  public String getIdent(ASTConstantDeclarator a) {
    String type = Layouter.nodeName(a);
    String name = a.getName();
    return format(name, type);
  }
  
  public String getIdent(ASTFieldDeclaration a) {
    String type = Layouter.nodeName(a);
    List<ASTVariableDeclarator> l = a.getVariableDeclaratorList();
    String n = "";
    if (l.isEmpty()) {
      n += "-";
    }
    if (!l.isEmpty()) {
      n += l.get(0).getDeclarator().getName();
    }
    if (l.size() > 1) {
      n += "..";
    }
    return format(n, type);
  }
  
  public String getIdent(ASTDeclaratorId a) {
    String type = Layouter.nodeName(a);
    String name = a.getName();
    return format(name, type);
  }
  
  public String getIdent(ASTInterfaceDeclaration a) {
    String type = Layouter.nodeName(a);
    String name = a.getName();
    return format(name, type);
  }

  public String getIdent(ASTConstructorDeclaration a) {
    String type = Layouter.nodeName(a);
    String name = a.getName();
    return format(name, type);
  }
  
  public String getIdent(ASTVariableDeclarator a) {
    String type = Layouter.nodeName(a);
    String name = a.getDeclarator().getName();
    return format(name, type);
  }
  
  public String getIdent(ASTFormalParameter a) {
    String type = Layouter.nodeName(a);
    String name = a.getDeclarator().getName();
    return format(name, type);
  }
  
  public String getIdent(ASTLastFormalParameter a) {
    String type = Layouter.nodeName(a);
    String name = a.getDeclaratorId().getName();
    return format(name, type);
  }
  
  public String getIdent(ASTElementValuePair a) {
    String type = Layouter.nodeName(a);
    String name = a.getName();
    return format(name, type);
  }
  
  public String getIdent(ASTAnnotationTypeDeclaration a) {
    String type = Layouter.nodeName(a);
    String name = a.getName();
    return format(name, type);
  }
  
  public String getIdent(ASTAnnotationMethod a) {
    String type = Layouter.nodeName(a);
    String name = a.getName();
    return format(name, type);
  }
  
  public String getIdent(ASTLabeledStatement a) {
    String type = Layouter.nodeName(a);
    String name = a.getLabel();
    return format(name, type);
  }
  
  public String getIdent(ASTEnumConstantSwitchLabel a) {
    String type = Layouter.nodeName(a);
    String name = a.getEnumConstant();
    return format(name, type);
  }
  
  public String getIdent(ASTIdentifierAndTypeArgument a) {
    String type = Layouter.nodeName(a);
    String name = a.getName();
    return format(name, type);
  }

  public String getIdent(ASTEnumDeclaration a) {
    return format(a.getName(), Layouter.nodeName(a));
  }
  
  public String getIdent(ASTEnumConstantDeclaration a) {
    return format(a.getName(), Layouter.nodeName(a));
  }

  public String getIdent(ASTMCQualifiedName node) {
    String type = Layouter.nodeName(node);
    String name = node.getQName();
    return format(name, type);
  }

  public String getIdent(ASTMCPrimitiveType node) {
    String type = Layouter.nodeName(node);
    String name = node.printType(MCBasicTypesMill.mcBasicTypesPrettyPrinter());
    return format(name, type);
  }

  @Override
  public String getIdent(ASTNode a) {
    if (a instanceof ASTConstructorDeclaration) {
      return getIdent((ASTConstructorDeclaration) a);
    }
    else if (a instanceof ASTAnnotationMethod) {
      return getIdent((ASTAnnotationMethod) a);
    }
    else if (a instanceof ASTAnnotationTypeDeclaration) {
      return getIdent((ASTAnnotationTypeDeclaration) a);
    }
    else if (a instanceof ASTClassDeclaration) {
      return getIdent((ASTClassDeclaration) a);
    }
    else if (a instanceof ASTConstantDeclarator) {
      return getIdent((ASTConstantDeclarator) a);
    }
    else if (a instanceof ASTDeclaratorId) {
      return getIdent((ASTDeclaratorId) a);
    }
    else if (a instanceof ASTElementValuePair) {
      return getIdent((ASTElementValuePair) a);
    }
    else if (a instanceof ASTEnumConstantSwitchLabel) {
      return getIdent((ASTEnumConstantSwitchLabel) a);
    }
    else if (a instanceof ASTIdentifierAndTypeArgument) {
      return getIdent((ASTIdentifierAndTypeArgument) a);
    }
    else if (a instanceof ASTLabeledStatement) {
      return getIdent((ASTLabeledStatement) a);
    }
    else if (a instanceof ASTLastFormalParameter) {
      return getIdent((ASTLastFormalParameter) a);
    }
    else if (a instanceof ASTFormalParameter) {
      return getIdent((ASTFormalParameter) a);
    }
    else if (a instanceof ASTMCPrimitiveType) {
      return getIdent((ASTMCPrimitiveType) a);
    }
    else if (a instanceof ASTMCQualifiedName) {
      return getIdent((ASTMCQualifiedName) a);
    }
    else if (a instanceof ASTFieldDeclaration) {
      return getIdent((ASTFieldDeclaration) a);
    }
    else if (a instanceof ASTTypeParameters) {
      return getIdent((ASTTypeParameters) a);
    }
    else if (a instanceof ASTEnumDeclaration) {
      return getIdent((ASTEnumDeclaration) a);
    }
    else if (a instanceof ASTEnumConstantDeclaration) {
      return getIdent((ASTEnumConstantDeclaration) a);
    }
    else if (a instanceof ASTIntLiteral) {
      return getIdent((ASTIntLiteral) a);
    }
    else if (a instanceof ASTStringLiteral) {
      return getIdent((ASTStringLiteral) a);
    }
    else if (a instanceof ASTVariableDeclarator) {
      return getIdent((ASTVariableDeclarator) a);
    }
    else {
      String type = Layouter.nodeName(a);
      return format(type);
    }
  }
  
}
