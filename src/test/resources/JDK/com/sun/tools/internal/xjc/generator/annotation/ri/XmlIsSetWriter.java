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

package com.sun.tools.internal.xjc.generator.annotation.ri;

import com.sun.codemodel.internal.JAnnotationWriter;
import com.sun.xml.internal.bind.annotation.XmlIsSet;

public interface XmlIsSetWriter
    extends JAnnotationWriter<XmlIsSet>
{


    XmlIsSetWriter value(String value);

}
