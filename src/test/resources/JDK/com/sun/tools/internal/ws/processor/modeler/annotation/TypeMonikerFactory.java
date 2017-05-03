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


import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author  dkohlert
 */
public class TypeMonikerFactory {

    public static TypeMoniker getTypeMoniker(TypeMirror typeMirror) {
        if (typeMirror instanceof PrimitiveType)
            return new PrimitiveTypeMoniker((PrimitiveType)typeMirror);
        else if (typeMirror instanceof ArrayType)
            return new ArrayTypeMoniker((ArrayType)typeMirror);
        else if (typeMirror instanceof DeclaredType)
            return new DeclaredTypeMoniker((DeclaredType)typeMirror);
        return getTypeMoniker(typeMirror.toString());
    }

    public static TypeMoniker getTypeMoniker(String typeName) {
        return new StringMoniker(typeName);
    }

    static class ArrayTypeMoniker implements TypeMoniker {
        private TypeMoniker arrayType;

        public ArrayTypeMoniker(ArrayType type) {
            arrayType = TypeMonikerFactory.getTypeMoniker(type.getComponentType());
        }

        public TypeMirror create(AnnotationProcessorEnvironment apEnv) {
            return apEnv.getTypeUtils().getArrayType(arrayType.create(apEnv));
        }
    }
    static class DeclaredTypeMoniker implements TypeMoniker {
        private String typeDeclName;
        private Collection<TypeMoniker> typeArgs = new ArrayList<TypeMoniker>();

        public DeclaredTypeMoniker(DeclaredType type) {
            typeDeclName = type.getDeclaration().getQualifiedName();
            for (TypeMirror arg : type.getActualTypeArguments())
                typeArgs.add(TypeMonikerFactory.getTypeMoniker(arg));
        }

        public TypeMirror create(AnnotationProcessorEnvironment apEnv) {
            TypeDeclaration typeDecl = apEnv.getTypeDeclaration(typeDeclName);
            TypeMirror[] tmpArgs = new TypeMirror[typeArgs.size()];
            int idx = 0;
            for (TypeMoniker moniker : typeArgs)
                tmpArgs[idx++] = moniker.create(apEnv);

            return apEnv.getTypeUtils().getDeclaredType(typeDecl, tmpArgs);
        }
    }
    static class PrimitiveTypeMoniker implements TypeMoniker {
        private PrimitiveType.Kind kind;

        public PrimitiveTypeMoniker(PrimitiveType type) {
            kind = type.getKind();
        }

        public TypeMirror create(AnnotationProcessorEnvironment apEnv) {
            return apEnv.getTypeUtils().getPrimitiveType(kind);
        }
    }
    static class StringMoniker implements TypeMoniker {
        private String typeName;

        public StringMoniker(String typeName) {
            this.typeName = typeName;
        }

        public TypeMirror create(AnnotationProcessorEnvironment apEnv) {
            return apEnv.getTypeUtils().getDeclaredType(apEnv.getTypeDeclaration(typeName));
        }
    }
}
