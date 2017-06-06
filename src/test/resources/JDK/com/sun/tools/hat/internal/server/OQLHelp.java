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

package com.sun.tools.hat.internal.server;

import java.io.*;

/**
 * This handles Object Query Language (OQL) help.
 *
 * @author A. Sundararajan
 */

class OQLHelp extends QueryHandler {

    public OQLHelp() {
    }

    public void run() {
        InputStream is = getClass().getResourceAsStream("/com/sun/tools/hat/resources/oqlhelp.html");
        int ch = -1;
        try {
            is = new BufferedInputStream(is);
            while ( (ch = is.read()) != -1) {
                out.print((char)ch);
            }
        } catch (Exception exp) {
            out.println(exp.getMessage());
            out.println("<pre>");
            exp.printStackTrace(out);
            out.println("</pre>");
        }
    }
}
