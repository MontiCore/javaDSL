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

import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
import com.sun.codemodel.internal.JAnnotationWriter;

public interface XmlSchemaWriter
    extends JAnnotationWriter<XmlSchema>
{


    XmlSchemaWriter location(String value);

    XmlSchemaWriter namespace(String value);

    XmlNsWriter xmlns();

    XmlSchemaWriter elementFormDefault(XmlNsForm value);

    XmlSchemaWriter attributeFormDefault(XmlNsForm value);

}
