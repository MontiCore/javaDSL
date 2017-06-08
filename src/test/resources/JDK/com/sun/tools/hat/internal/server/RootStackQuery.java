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


import com.sun.tools.hat.internal.model.*;

/**
 * Query to show the StackTrace for a given root
 *
 * @author      Bill Foote
 */


class RootStackQuery extends QueryHandler {

    public RootStackQuery() {
    }

    public void run() {
        int index = (int) parseHex(query);
        Root root = snapshot.getRootAt(index);
        if (root == null) {
            error("Root at " + index + " not found");
            return;
        }
        StackTrace st = root.getStackTrace();
        if (st == null || st.getFrames().length == 0) {
            error("No stack trace for " + root.getDescription());
            return;
        }
        startHtml("Stack Trace for " + root.getDescription());
        out.println("<p>");
        printStackTrace(st);
        out.println("</p>");
        endHtml();
    }

}
