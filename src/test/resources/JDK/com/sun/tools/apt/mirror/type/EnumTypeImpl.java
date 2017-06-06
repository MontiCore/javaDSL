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
 * Implementation of EnumType
 */
@SuppressWarnings("deprecation")
public class EnumTypeImpl extends ClassTypeImpl implements EnumType {

    EnumTypeImpl(AptEnv env, Type.ClassType type) {
        super(env, type);
    }


    /**
     * {@inheritDoc}
     */
    public EnumDeclaration getDeclaration() {
        return (EnumDeclaration) super.getDeclaration();
    }

    /**
     * {@inheritDoc}
     */
    public void accept(TypeVisitor v) {
        v.visitEnumType(this);
    }
}
