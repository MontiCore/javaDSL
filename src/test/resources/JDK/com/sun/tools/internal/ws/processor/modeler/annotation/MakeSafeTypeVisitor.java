/*
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.internal.ws.processor.modeler.annotation;

import com.sun.istack.internal.tools.APTTypeVisitor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.*;
import com.sun.mirror.util.Types;

import java.util.Collection;

/**
 *
 * @author dkohlert
 */
public class MakeSafeTypeVisitor extends APTTypeVisitor<TypeMirror, Types> implements WebServiceConstants {
    TypeDeclaration collectionDecl;
    TypeDeclaration mapDecl;

    /**
     * Creates a new instance of MakeSafeTypeVisitor
     */
    public MakeSafeTypeVisitor(AnnotationProcessorEnvironment apEnv) {
        collectionDecl = apEnv.getTypeDeclaration(COLLECTION_CLASSNAME);
        mapDecl = apEnv.getTypeDeclaration(MAP_CLASSNAME);
    }

    protected TypeMirror onArrayType(ArrayType type, Types apTypes) {
        return apTypes.getErasure(type);
    }

    protected TypeMirror onPrimitiveType(PrimitiveType type, Types apTypes) {
        return apTypes.getErasure(type);
    }

    protected TypeMirror onClassType(ClassType type, Types apTypes) {
        return processDeclaredType(type, apTypes);
    }

    protected TypeMirror onInterfaceType(InterfaceType type, Types apTypes) {
        return processDeclaredType(type, apTypes);
    }

    private TypeMirror processDeclaredType(DeclaredType type, Types apTypes) {
        if (TypeModeler.isSubtype(type.getDeclaration(), collectionDecl) ||
            TypeModeler.isSubtype(type.getDeclaration(), mapDecl)) {
            Collection<TypeMirror> args = type.getActualTypeArguments();
            TypeMirror[] safeArgs = new TypeMirror[args.size()];
            int i = 0;
            for (TypeMirror arg : args) {
                safeArgs[i++]= apply(arg, apTypes);
            }
            return apTypes.getDeclaredType(type.getDeclaration(), safeArgs);
        }
        return apTypes.getErasure(type);
    }

    protected TypeMirror onTypeVariable(TypeVariable type, Types apTypes) {
        return apTypes.getErasure(type);
    }

    protected TypeMirror onVoidType(VoidType type, Types apTypes) {
        return type;
    }

    protected TypeMirror onWildcard(WildcardType type, Types apTypes) {
        return apTypes.getErasure(type);
    }
}
