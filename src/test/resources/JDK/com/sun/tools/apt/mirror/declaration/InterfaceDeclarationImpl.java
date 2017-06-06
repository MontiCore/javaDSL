/*
 * Copyright (c) 2004, Oracle and/or its affiliates. All rights reserved.
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
import com.sun.tools.apt.mirror.AptEnv;
import com.sun.tools.javac.code.Symbol.*;


/**
 * Implementation of InterfaceDeclaration
 */
@SuppressWarnings("deprecation")
public class InterfaceDeclarationImpl extends TypeDeclarationImpl
                                      implements InterfaceDeclaration {

    InterfaceDeclarationImpl(AptEnv env, ClassSymbol sym) {
        super(env, sym);
    }

    /**
     * {@inheritDoc}
     */
    public void accept(DeclarationVisitor v) {
        v.visitInterfaceDeclaration(this);
    }
}
