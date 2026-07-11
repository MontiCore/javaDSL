package de.monticore.codeAdaption.handler;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.symboltable.ISymbol;
import java.util.Map;
import java.util.Optional;

/** Test access to package-private handler resolution without a production inheritance hook. */
public final class BasicUpdateHandlerTestAccess {

  private BasicUpdateHandlerTestAccess() {}

  public static Optional<ISymbol> resolve(
      IncarnationContext incarnationContext,
      ASTCDCompilationUnit reference,
      ASTCDCompilationUnit concrete,
      Map<StableElementKey, IncarnationContext.MappedElement> selection,
      boolean useCommonParentForMultipleIncarnations,
      ISymbol symbol) {
    BasicUpdateHandler handler =
        new BasicUpdateHandler(
            reference,
            concrete,
            null,
            null,
            null,
            incarnationContext,
            selection,
            useCommonParentForMultipleIncarnations);
    return handler.getSymbolFromContext(symbol);
  }
}
