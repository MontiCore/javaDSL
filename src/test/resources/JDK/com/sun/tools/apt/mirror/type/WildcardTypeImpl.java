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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.sun.mirror.declaration.*;
import com.sun.mirror.type.*;
import com.sun.mirror.util.TypeVisitor;
import com.sun.tools.apt.mirror.AptEnv;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;


/**
 * Implementation of WildcardType
 */
@SuppressWarnings("deprecation")
public class WildcardTypeImpl extends TypeMirrorImpl implements WildcardType {

    protected Type.WildcardType type;

    WildcardTypeImpl(AptEnv env, Type.WildcardType type) {
        super(env, type);
        this.type = type;
    }


    /**
     * Returns the string form of a wildcard type, consisting of "?"
     * and any "extends" or "super" clause.
     * Delimiting brackets are not included.  Class names are qualified.
     */
    public String toString() {
        return toString(env, type);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<ReferenceType> getUpperBounds() {
        return type.isSuperBound()
                ? Collections.<ReferenceType>emptyList()
                : typeToCollection(type.type);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<ReferenceType> getLowerBounds() {
        return type.isExtendsBound()
                ? Collections.<ReferenceType>emptyList()
                : typeToCollection(type.type);
    }

    /**
     * Gets the ReferenceType for a javac Type object, and returns
     * it in a singleton collection.  If type is null, returns an empty
     * collection.
     */
    private Collection<ReferenceType> typeToCollection(Type type) {
        ArrayList<ReferenceType> res = new ArrayList<ReferenceType>(1);
        if (type != null) {
            res.add((ReferenceType) env.typeMaker.getType(type));
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    public void accept(TypeVisitor v) {
        v.visitWildcardType(this);
    }


    /**
     * Returns the string form of a wildcard type, consisting of "?"
     * and any "extends" or "super" clause.
     * See {@link #toString()} for details.
     */
    static String toString(AptEnv env, Type.WildcardType wildThing) {
        return wildThing.toString();
    }
}
