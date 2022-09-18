package de.monticore.java.javadsl._symboltable;

import de.monticore.javalight._symboltable.JavaMethodSymbol;
import de.monticore.symboltable.serialization.json.JsonObject;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

import java.util.List;
import java.util.Optional;

public class ClassDeclarationSymbolDeSer extends ClassDeclarationSymbolDeSerTOP {


    @Override
    protected void serializeSuperInterface(List<ASTMCType> superInterface, JavaDSLSymbols2Json s2j) {

    }

    @Override
    protected void serializeConstructor(List<JavaMethodSymbol> constructor, JavaDSLSymbols2Json s2j) {

    }

    @Override
    protected void serializeMethods(List<JavaMethodSymbol> methods, JavaDSLSymbols2Json s2j) {

    }

    @Override
    protected List<ASTMCType> deserializeSuperInterface(JsonObject symbolJson) {
        return null;
    }

    @Override
    protected List<JavaMethodSymbol> deserializeConstructor(JsonObject symbolJson) {
        return null;
    }

    @Override
    protected List<JavaMethodSymbol> deserializeMethods(JsonObject symbolJson) {
        return null;
    }

}
