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
import java.util.*;

public class FinalizerObjectsQuery extends QueryHandler {
    public void run() {
        Enumeration objs = snapshot.getFinalizerObjects();
        startHtml("Objects pending finalization");

        out.println("<a href='/finalizerSummary/'>Finalizer summary</a>");

        out.println("<h1>Objects pending finalization</h1>");

        while (objs.hasMoreElements()) {
            printThing((JavaHeapObject)objs.nextElement());
            out.println("<br>");
        }

        endHtml();
    }
}
