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

package com.sun.tools.apt.mirror.type;


import com.sun.mirror.declaration.*;
import com.sun.mirror.type.*;
import com.sun.mirror.util.TypeVisitor;
import com.sun.tools.apt.mirror.AptEnv;
import com.sun.tools.javac.code.Type;


/**
 * Implementation of AnnotationType
 */
@SuppressWarnings("deprecation")
public class AnnotationTypeImpl extends InterfaceTypeImpl
                                implements AnnotationType {

    AnnotationTypeImpl(AptEnv env, Type.ClassType type) {
        super(env, type);
    }


    /**
     * {@inheritDoc}
     */
    public AnnotationTypeDeclaration getDeclaration() {
        return (AnnotationTypeDeclaration) super.getDeclaration();
    }

    /**
     * {@inheritDoc}
     */
    public void accept(TypeVisitor v) {
        v.visitAnnotationType(this);
    }
}
