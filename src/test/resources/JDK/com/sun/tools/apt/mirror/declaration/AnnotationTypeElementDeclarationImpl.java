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
import com.sun.tools.javac.code.Symbol.MethodSymbol;


/**
 * Implementation of AnnotationTypeElementDeclaration
 */
@SuppressWarnings("deprecation")
public class AnnotationTypeElementDeclarationImpl extends MethodDeclarationImpl
                                  implements AnnotationTypeElementDeclaration {

    AnnotationTypeElementDeclarationImpl(AptEnv env, MethodSymbol sym) {
        super(env, sym);
    }

    /**
     * {@inheritDoc}
     */
    public AnnotationTypeDeclaration getDeclaringType() {
        return (AnnotationTypeDeclaration) super.getDeclaringType();
    }

    /**
     * {@inheritDoc}
     */
    public AnnotationValue getDefaultValue() {
        return (sym.defaultValue == null)
               ? null
               : new AnnotationValueImpl(env, sym.defaultValue, null);
    }

    /**
     * {@inheritDoc}
     */
    public void accept(DeclarationVisitor v) {
        v.visitAnnotationTypeElementDeclaration(this);
    }
}
