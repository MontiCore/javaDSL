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

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  dkohlert
 */
public class FaultInfo {
    public String beanName;
    public TypeMoniker beanTypeMoniker;
    public boolean isWSDLException;
    public QName elementName;

    /** Creates a new instance of FaultInfo */
    public FaultInfo() {
    }
    public FaultInfo(String beanName) {
        this.beanName = beanName;
    }
    public FaultInfo(String beanName, boolean isWSDLException) {
        this.beanName = beanName;
        this.isWSDLException = isWSDLException;
    }
    public FaultInfo(TypeMoniker typeMoniker, boolean isWSDLException) {
        this.beanTypeMoniker = typeMoniker;
        this.isWSDLException = isWSDLException;
    }

    public void setIsWSDLException(boolean isWSDLException) {
        this.isWSDLException = isWSDLException;
    }

    public boolean isWSDLException() {
        return isWSDLException;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setElementName(QName elementName) {
        this.elementName =  elementName;
    }

    public QName getElementName() {
        return elementName;
    }
    public void setBeanTypeMoniker(TypeMoniker typeMoniker) {
        this.beanTypeMoniker = typeMoniker;
    }
    public TypeMoniker getBeanTypeMoniker() {
        return beanTypeMoniker;
    }

}
