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


import com.sun.mirror.type.*;
import com.sun.mirror.util.TypeVisitor;
import com.sun.tools.apt.mirror.AptEnv;
import com.sun.tools.javac.code.Type;


/**
 * Implementation of ArrayType
 */
@SuppressWarnings("deprecation")
public class ArrayTypeImpl extends TypeMirrorImpl implements ArrayType {

    protected Type.ArrayType type;


    ArrayTypeImpl(AptEnv env, Type.ArrayType type) {
        super(env, type);
        this.type = type;
    }


    /**
     * {@inheritDoc}
     */
    public TypeMirror getComponentType() {
        return env.typeMaker.getType(type.elemtype);
    }

    /**
     * {@inheritDoc}
     */
    public void accept(TypeVisitor v) {
        v.visitArrayType(this);
    }
}
