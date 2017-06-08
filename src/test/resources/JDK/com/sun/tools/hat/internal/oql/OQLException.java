/*
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
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


/*
 * The Original Code is HAT. The Initial Developer of the
 * Original Code is Bill Foote, with contributions from others
 * at JavaSoft/Sun.
 */

package com.sun.tools.hat.internal.oql;

/**
 * OQLException is thrown if OQL execution results in error
 *
 */
public class OQLException extends Exception {
    public OQLException(String msg) {
        super(msg);
    }

    public OQLException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public OQLException(Throwable cause) {
        super(cause);
    }
}
