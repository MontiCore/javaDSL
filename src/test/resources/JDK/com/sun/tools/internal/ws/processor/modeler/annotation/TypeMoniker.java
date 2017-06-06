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
import com.sun.mirror.type.TypeMirror;

/**
 *
 * @author  dkohlert
 */
public interface TypeMoniker {

    public TypeMirror create(AnnotationProcessorEnvironment apEnv);
}
