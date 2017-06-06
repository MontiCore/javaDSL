/*
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.sun.tools.javadoc;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.tree.JCTree.*;
import javax.tools.JavaFileObject;

/**
 *  Javadoc's own enter phase does a few things above and beyond that
 *  done by javac.
 *  @author Neal Gafter
 */
public class JavadocEnter extends Enter {
    public static JavadocEnter instance0(Context context) {
        Enter instance = context.get(enterKey);
        if (instance == null)
            instance = new JavadocEnter(context);
        return (JavadocEnter)instance;
    }

    public static void preRegister(Context context) {
        context.put(enterKey, new Context.Factory<Enter>() {
               public Enter make(Context c) {
                   return new JavadocEnter(c);
               }
        });
    }

    protected JavadocEnter(Context context) {
        super(context);
        messager = Messager.instance0(context);
        docenv = DocEnv.instance(context);
    }

    final Messager messager;
    final DocEnv docenv;

    @Override
    public void main(List<JCCompilationUnit> trees) {
        // count all Enter errors as warnings.
        int nerrors = messager.nerrors;
        super.main(trees);
        messager.nwarnings += (messager.nerrors - nerrors);
        messager.nerrors = nerrors;
    }

    @Override
    public void visitTopLevel(JCCompilationUnit tree) {
        super.visitTopLevel(tree);
        if (tree.sourcefile.isNameCompatible("package-info", JavaFileObject.Kind.SOURCE)) {
            String comment = tree.docComments.get(tree);
            docenv.makePackageDoc(tree.packge, comment, tree);
        }
    }

    @Override
    public void visitClassDef(JCClassDecl tree) {
        super.visitClassDef(tree);
        if (tree.sym == null) return;
        if (tree.sym.kind == Kinds.TYP || tree.sym.kind == Kinds.ERR) {
            String comment = env.toplevel.docComments.get(tree);
            ClassSymbol c = tree.sym;
            docenv.makeClassDoc(c, comment, tree, env.toplevel.lineMap);
        }
    }

    /** Don't complain about a duplicate class. */
    @Override
    protected void duplicateClass(DiagnosticPosition pos, ClassSymbol c) {}

}
