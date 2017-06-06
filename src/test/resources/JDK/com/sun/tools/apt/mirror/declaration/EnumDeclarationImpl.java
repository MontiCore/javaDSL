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
import com.sun.mirror.util.*;
import com.sun.tools.apt.mirror.AptEnv;
import com.sun.tools.javac.code.Symbol.*;


/**
 * Implementation of EnumDeclaration
 */
@SuppressWarnings("deprecation")
public class EnumDeclarationImpl extends ClassDeclarationImpl
                                 implements EnumDeclaration {

    EnumDeclarationImpl(AptEnv env, ClassSymbol sym) {
        super(env, sym);
    }


    /**
     * {@inheritDoc}
     */
    public Collection<EnumConstantDeclaration> getEnumConstants() {
        return identityFilter.filter(getFields(),
                                     EnumConstantDeclaration.class);
    }

    /**
     * {@inheritDoc}
     */
    public void accept(DeclarationVisitor v) {
        v.visitEnumDeclaration(this);
    }
}
