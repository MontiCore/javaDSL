package de.monticore.java.javadsl._symboltable;

import de.monticore.javalight._symboltable.JavaMethodSymbol;
import de.monticore.symboltable.serialization.json.JsonObject;
import de.monticore.types.check.SymTypeExpression;

import java.util.List;

public class EnumDeclarationSymbolDeSer extends EnumDeclarationSymbolDeSerTOP {

    @Override
    protected void serializeConstructor(List<JavaMethodSymbol> constructor, JavaDSLSymbols2Json s2j) {

    }

    @Override
    protected void serializeMethods(List<JavaMethodSymbol> methods, JavaDSLSymbols2Json s2j) {

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