package de.monticore.java.javadsl._prettyprint;

import de.monticore.prettyprint.IndentPrinter;
import de.monticore.statements.mccommonstatements._ast.ASTJavaModifier;

import java.util.stream.Collectors;

public class JavaDSLPrettyPrinter extends JavaDSLPrettyPrinterTOP {
  
  public JavaDSLPrettyPrinter(IndentPrinter printer, boolean printComments) {
    super(printer, printComments);
  }
  
  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTClassDeclaration node) {
    if (this.isPrintComments()) {
      de.monticore.prettyprint.CommentPrettyPrinter.printPreComments(node, getPrinter());
    }
    java.util.Iterator<de.monticore.types.mcbasictypes._ast.ASTMCType> iter_implementedInterface =
        node.getImplementedInterfaceList().iterator();
    
    node.getJavaModifierList().forEach(n -> n.accept(getTraverser()));
    
    getPrinter().print("class ");
    
    getPrinter().print(node.getName() + " ");
    
    if (node.isPresentTypeParameters()) {
      node.getTypeParameters().accept(getTraverser());
    }
    
    if (node.isPresentSuperClass()) {
      getPrinter().print("extends ");
      node.getSuperClass().accept(getTraverser());
    }
    
    if (iter_implementedInterface.hasNext()) {
      getPrinter().print("implements ");
      if (iter_implementedInterface.hasNext()) {
        iter_implementedInterface.next().accept(getTraverser());
        while (iter_implementedInterface.hasNext()) {
          getPrinter().stripTrailing();
          getPrinter().print(",");
          iter_implementedInterface.next().accept(getTraverser());
        }
      }
    }
    node.getClassBody().accept(getTraverser());
    
    getPrinter().stripTrailing();
    
    if (this.isPrintComments()) {
      de.monticore.prettyprint.CommentPrettyPrinter.printPostComments(node, getPrinter());
    }
  }
  
  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTRecordDeclaration node) {
    if (this.isPrintComments()) {
      de.monticore.prettyprint.CommentPrettyPrinter.printPreComments(node, getPrinter());
    }
    java.util.Iterator<de.monticore.types.mcbasictypes._ast.ASTMCType> iter_implementedInterface =
        node.getImplementedInterfaceList().iterator();
    
    node.getJavaModifierList().forEach(n->n.accept(getTraverser()));
    
    getPrinter().print("record ");
    
    getPrinter().print(node.getName() + " ");
    
    if (node.isPresentTypeParameters()) {
      node.getTypeParameters().accept(getTraverser());
    }
    
    node.getRecordHeader().accept(getTraverser());
    
    if (iter_implementedInterface.hasNext()) {
      getPrinter().print("implements ");
      if (iter_implementedInterface.hasNext()) {
        iter_implementedInterface.next().accept(getTraverser());
        while (iter_implementedInterface.hasNext()) {
          getPrinter().stripTrailing();
          getPrinter().print(",");
          
          iter_implementedInterface.next().accept(getTraverser());
        }
      }
    }
    
    node.getRecordBody().accept(getTraverser());
    
    getPrinter().stripTrailing();
    
    if (this.isPrintComments()) {
      de.monticore.prettyprint.CommentPrettyPrinter.printPostComments(node, getPrinter());
    }
  }
  
  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTInterfaceDeclaration node) {
    if (this.isPrintComments()) {
      de.monticore.prettyprint.CommentPrettyPrinter.printPreComments(node, getPrinter());
    }
    java.util.Iterator<de.monticore.types.mcbasictypes._ast.ASTMCType> iter_extendedInterface =
        node.getExtendedInterfaceList().iterator();
    
    node.getJavaModifierList().forEach(n -> n.accept(getTraverser()));
    
    getPrinter().print("interface ");
    
    getPrinter().print(node.getName() + " ");
    
    if (node.isPresentTypeParameters()) {
      node.getTypeParameters().accept(getTraverser());
    }
    
    if (iter_extendedInterface.hasNext()) {
      getPrinter().print("extends ");
      if (iter_extendedInterface.hasNext()) {
        iter_extendedInterface.next().accept(getTraverser());
        while (iter_extendedInterface.hasNext()) {
          getPrinter().stripTrailing();
          getPrinter().print(",");
          
          iter_extendedInterface.next().accept(getTraverser());
        }
      }
    }
    
    node.getInterfaceBody().accept(getTraverser());
    
    getPrinter().stripTrailing();
    
    if (this.isPrintComments()) {
      de.monticore.prettyprint.CommentPrettyPrinter.printPostComments(node, getPrinter());
    }
    
  }
  
  @Override
  public void handle(de.monticore.java.javadsl._ast.ASTEnumDeclaration node) {
    if (this.isPrintComments()) {
      de.monticore.prettyprint.CommentPrettyPrinter.printPreComments(node, getPrinter());
    }
    java.util.Iterator<de.monticore.java.javadsl._ast.ASTEnumConstantDeclaration>
        iter_enumConstantDeclaration = node.getEnumConstantDeclarationList().iterator();
    java.util.Iterator<de.monticore.types.mcbasictypes._ast.ASTMCType> iter_implementedInterface =
        node.getImplementedInterfaceList().iterator();
    
    node.getJavaModifierList().forEach(n -> n.accept(getTraverser()));
    
    getPrinter().print("enum ");
    
    getPrinter().print(node.getName() + " ");
    
    if (iter_implementedInterface.hasNext()) {
      getPrinter().print("implements ");
      
      if (iter_implementedInterface.hasNext()) {
        iter_implementedInterface.next().accept(getTraverser());
        while (iter_implementedInterface.hasNext()) {
          getPrinter().stripTrailing();
          getPrinter().print(",");
          
          iter_implementedInterface.next().accept(getTraverser());
        }
      }
    }
    getPrinter().println("{ ");
    getPrinter().indent();
    if (iter_enumConstantDeclaration.hasNext()) {
      iter_enumConstantDeclaration.next().accept(getTraverser());
      while (iter_enumConstantDeclaration.hasNext()) {
        getPrinter().stripTrailing();
        getPrinter().print(",");
        
        iter_enumConstantDeclaration.next().accept(getTraverser());
      }
    }
    
    getPrinter().stripTrailing();
    getPrinter().print(",");
    
    if (node.isPresentEnumBody()) {
      node.getEnumBody().accept(getTraverser());
    }
    
    getPrinter().unindent();
    getPrinter().println();
    getPrinter().println("} ");
    
    getPrinter().stripTrailing();
    
    if (this.isPrintComments()) {
      de.monticore.prettyprint.CommentPrettyPrinter.printPostComments(node, getPrinter());
    }
  }
}
