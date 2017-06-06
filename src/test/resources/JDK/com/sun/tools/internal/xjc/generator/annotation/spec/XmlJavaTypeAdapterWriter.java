/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.internal.xjc.generator.annotation.spec;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.sun.codemodel.internal.JAnnotationWriter;
import com.sun.codemodel.internal.JType;

public interface XmlJavaTypeAdapterWriter
    extends JAnnotationWriter<XmlJavaTypeAdapter>
{


    XmlJavaTypeAdapterWriter type(Class value);

    XmlJavaTypeAdapterWriter type(JType value);

    XmlJavaTypeAdapterWriter value(Class value);

    XmlJavaTypeAdapterWriter value(JType value);

}
