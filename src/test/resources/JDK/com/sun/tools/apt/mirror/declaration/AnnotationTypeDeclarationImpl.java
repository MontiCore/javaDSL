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


import java.util.Collection;

import com.sun.mirror.declaration.*;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.tools.apt.mirror.AptEnv;
import com.sun.tools.javac.code.Symbol.*;


/**
 * Implementation of AnnotationTypeDeclaration
 */
@SuppressWarnings("deprecation")
public class AnnotationTypeDeclarationImpl extends InterfaceDeclarationImpl
                                           implements AnnotationTypeDeclaration
{
    AnnotationTypeDeclarationImpl(AptEnv env, ClassSymbol sym) {
        super(env, sym);
    }


    /**
     * {@inheritDoc}
     */
    public Collection<AnnotationTypeElementDeclaration> getMethods() {
        return identityFilter.filter(super.getMethods(),
                                     AnnotationTypeElementDeclaration.class);
    }

    /**
     * {@inheritDoc}
     */
    public void accept(DeclarationVisitor v) {
        v.visitAnnotationTypeDeclaration(this);
    }
}
