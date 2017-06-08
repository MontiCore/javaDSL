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

package com.sun.tools.internal.ws.wsdl.framework;

import com.sun.xml.internal.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.internal.ws.util.localization.Localizable;

/**
 * An exception signalling a parsing error.
 *
 * @author WS Development Team
 */
public class ParseException extends JAXWSExceptionBase {

    public ParseException(String key, Object... args) {
        super(key, args);
    }

    public ParseException(Localizable message){
        super("localized.error", message);
    }

    public ParseException(Throwable throwable) {
        super(throwable);
    }

    public String getDefaultResourceBundleName() {
        return "com.sun.tools.internal.ws.resources.wsdl";
    }
}
