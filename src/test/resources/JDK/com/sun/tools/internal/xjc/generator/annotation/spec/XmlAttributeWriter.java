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

import javax.xml.bind.annotation.XmlAttribute;
import com.sun.codemodel.internal.JAnnotationWriter;

public interface XmlAttributeWriter
    extends JAnnotationWriter<XmlAttribute>
{


    XmlAttributeWriter name(String value);

    XmlAttributeWriter namespace(String value);

    XmlAttributeWriter required(boolean value);

}
