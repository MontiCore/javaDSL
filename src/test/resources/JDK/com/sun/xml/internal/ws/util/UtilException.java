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

package com.sun.xml.internal.ws.util;

import com.sun.xml.internal.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.internal.ws.util.localization.Localizable;

/**
 * UtilException represents an exception that occurred while
 * one of the util classes is operating.
 *
 * @see JAXWSExceptionBase
 *
 * @author JAX-WS Development Team
 */
public class UtilException extends JAXWSExceptionBase {
    public UtilException(String key, Object... args) {
        super(key, args);
    }

    public UtilException(Throwable throwable) {
        super(throwable);
    }

    public UtilException(Localizable arg) {
        super("nestedUtilError", arg);
    }

    public String getDefaultResourceBundleName() {
        return "com.sun.xml.internal.ws.resources.util";
    }

}
