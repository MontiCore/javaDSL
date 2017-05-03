/*
 * Copyright (c) 1997, 2005, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.javadoc;

import com.sun.javadoc.*;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Position;

/**
 * Represents a member of a java class: field, constructor, or method.
 * This is an abstract class dealing with information common to
 * method, constructor and field members. Class members of a class
 * (nested classes) are represented instead by ClassDocImpl.
 *
 * @see MethodDocImpl
 * @see FieldDocImpl
 * @see ClassDocImpl
 *
 * @author Robert Field
 * @author Neal Gafter
 */

public abstract class MemberDocImpl
    extends ProgramElementDocImpl
    implements MemberDoc {

    /**
     * constructor.
     */
    public MemberDocImpl(DocEnv env, Symbol sym, String doc, JCTree tree, Position.LineMap lineMap) {
        super(env, sym, doc, tree, lineMap);
    }

    /**
     * Returns true if this field was synthesized by the compiler.
     */
    public abstract boolean isSynthetic();
}
