/*
 * Copyright (c) 2004, 2009, Oracle and/or its affiliates. All rights reserved.
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


import java.util.Collection;
import java.util.ArrayList;

import com.sun.mirror.declaration.*;
import com.sun.mirror.type.*;
import com.sun.mirror.util.TypeVisitor;
import com.sun.tools.apt.mirror.AptEnv;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;


/**
 * Implementation of TypeVariable
 */
@SuppressWarnings("deprecation")
public class TypeVariableImpl extends TypeMirrorImpl implements TypeVariable {

    protected Type.TypeVar type;


    TypeVariableImpl(AptEnv env, Type.TypeVar type) {
        super(env, type);
        this.type = type;
    }


    /**
     * Returns the simple name of this type variable.  Bounds are
     * not included.
     */
    public String toString() {
        return type.tsym.name.toString();
    }

    /**
     * {@inheritDoc}
     */
    public TypeParameterDeclaration getDeclaration() {
        TypeSymbol sym = type.tsym;
        return env.declMaker.getTypeParameterDeclaration(sym);
    }

    /**
     * {@inheritDoc}
     */
    public void accept(TypeVisitor v) {
        v.visitTypeVariable(this);
    }
}
