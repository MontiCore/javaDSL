/*
 * Copyright (c) 2004, 2005, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.apt.mirror.declaration;


import com.sun.mirror.declaration.*;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.type.TypeMirror;
import com.sun.tools.apt.mirror.AptEnv;
import com.sun.tools.javac.code.Symbol.MethodSymbol;


/**
 * Implementation of MethodDeclaration
 */
@SuppressWarnings("deprecation")
public class MethodDeclarationImpl extends ExecutableDeclarationImpl
                                   implements MethodDeclaration {

    MethodDeclarationImpl(AptEnv env, MethodSymbol sym) {
        super(env, sym);
    }


    /**
     * {@inheritDoc}
     */
    public TypeMirror getReturnType() {
        return env.typeMaker.getType(sym.type.getReturnType());
    }

    /**
     * {@inheritDoc}
     */
    public void accept(DeclarationVisitor v) {
        v.visitMethodDeclaration(this);
    }
}
