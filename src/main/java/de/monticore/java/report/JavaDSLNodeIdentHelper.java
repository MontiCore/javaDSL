/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.report;

import java.util.List;

import de.monticore.ast.ASTNode;
import de.monticore.generating.templateengine.reporting.commons.Layouter;
import de.monticore.java.javadsl._ast.ASTAnnotationMethod;
import de.monticore.java.javadsl._ast.ASTAnnotationTypeDeclaration;
import de.monticore.java.javadsl._ast.ASTCatchClause;
import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._ast.ASTConstantDeclarator;
import de.monticore.java.javadsl._ast.ASTConstructorDeclaration;
import de.monticore.java.javadsl._ast.ASTDeclaratorId;
import de.monticore.java.javadsl._ast.ASTElementValuePair;
import de.monticore.java.javadsl._ast.ASTEnumConstantDeclaration;
import de.monticore.java.javadsl._ast.ASTEnumConstantSwitchLabel;
import de.monticore.java.javadsl._ast.ASTEnumDeclaration;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTFormalParameter;
import de.monticore.java.javadsl._ast.ASTIdentifierAndTypeArgument;
import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._ast.ASTLabeledStatement;
import de.monticore.java.javadsl._ast.ASTLastFormalParameter;
import de.monticore.java.javadsl._ast.ASTMethodSignature;
import de.monticore.java.javadsl._ast.ASTVariableDeclarator;
import de.monticore.literals.literals._ast.ASTIntLiteral;
import de.monticore.literals.literals._ast.ASTStringLiteral;
import de.monticore.types.TypesNodeIdentHelper;
import de.monticore.types.types._ast.ASTPrimitiveType;
import de.monticore.types.types._ast.ASTQualifiedName;
import de.monticore.types.types._ast.ASTSimpleReferenceType;
import de.monticore.types.types._ast.ASTTypeParameters;

/**
 * @author Marita Breuer
 */
public class JavaDSLNodeIdentHelper extends TypesNodeIdentHelper {
  
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
      n += l.get(0).getDeclaratorId().getName();
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
  
  public String getIdent(ASTMethodSignature a) {
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
    String name = a.getDeclaratorId().getName();
    return format(name, type);
  }
  
  public String getIdent(ASTFormalParameter a) {
    String type = Layouter.nodeName(a);
    String name = a.getDeclaratorId().getName();
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
  
  public String getIdent(ASTCatchClause a) {
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
    String name = a.getEnumConstantName();
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
  
  /**
   * @see de.monticore.generating.templateengine.reporting.commons.ASTNodeIdentHelper#getIdent(de.monticore.ast.ASTNode)
   */
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
    else if (a instanceof ASTCatchClause) {
      return getIdent((ASTCatchClause) a);
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
    else if (a instanceof ASTMethodSignature) {
      return getIdent((ASTMethodSignature) a);
    }
    else if (a instanceof ASTFormalParameter) {
      return getIdent((ASTFormalParameter) a);
    }
    else if (a instanceof ASTPrimitiveType) {
      return getIdent((ASTPrimitiveType) a);
    }
    else if (a instanceof ASTQualifiedName) {
      return getIdent((ASTQualifiedName) a);
    }
    else if (a instanceof ASTFieldDeclaration) {
      return getIdent((ASTFieldDeclaration) a);
    }
    else if (a instanceof ASTSimpleReferenceType) {
      return getIdent((ASTSimpleReferenceType) a);
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
