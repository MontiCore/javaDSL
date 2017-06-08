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
 * Implementation of ClassType
 */
@SuppressWarnings("deprecation")
public class ClassTypeImpl extends DeclaredTypeImpl implements ClassType {

    ClassTypeImpl(AptEnv env, Type.ClassType type) {
        super(env, type);
    }


    /**
     * {@inheritDoc}
     */
    public ClassDeclaration getDeclaration() {
        return (ClassDeclaration) super.getDeclaration();
    }

    /**
     * {@inheritDoc}
     */
    public ClassType getSuperclass() {
        //  java.lang.Object has no superclass
        if (type.tsym == env.symtab.objectType.tsym) {
            return null;
        }
        Type sup = env.jctypes.supertype(type);
        return (ClassType) env.typeMaker.getType(sup);
    }

    /**
     * {@inheritDoc}
     */
    public void accept(TypeVisitor v) {
        v.visitClassType(this);
    }
}
