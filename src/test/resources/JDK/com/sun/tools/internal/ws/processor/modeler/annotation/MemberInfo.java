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

import com.sun.mirror.type.TypeMirror;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 * @author  WS Development Team
 */
final class MemberInfo implements Comparable<MemberInfo> {
    private final TypeMirror paramType;
    private final String paramName;
    private final List<Annotation> jaxbAnnotations;

    public MemberInfo(TypeMirror paramType, String paramName, List<Annotation> jaxbAnnotations) {
        this.paramType = paramType;
        this.paramName = paramName;
        this.jaxbAnnotations = jaxbAnnotations;
    }

    public List<Annotation> getJaxbAnnotations() {
        return jaxbAnnotations;
    }

    public TypeMirror getParamType() {
        return paramType;
    }

    public String getParamName() {
        return paramName;
    }

    public int compareTo(MemberInfo member) {
        return paramName.compareTo(member.paramName);
    }
}
