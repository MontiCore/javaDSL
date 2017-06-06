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
 *
 * @author      Bill Foote
 */


class ReachableQuery extends QueryHandler {
        // We inherit printFullClass from ClassQuery


    public ReachableQuery() {
    }

    public void run() {
        startHtml("Objects Reachable From " + query);
        long id = parseHex(query);
        JavaHeapObject root = snapshot.findThing(id);
        ReachableObjects ro = new ReachableObjects(root,
                                   snapshot.getReachableExcludes());
        // Now, print out the sorted list, but start with root
        long totalSize = ro.getTotalSize();
        JavaThing[] things = ro.getReachables();
        long instances = things.length;

        out.print("<strong>");
        printThing(root);
        out.println("</strong><br>");
        out.println("<br>");
        for (int i = 0; i < things.length; i++) {
            printThing(things[i]);
            out.println("<br>");
        }

        printFields(ro.getUsedFields(), "Data Members Followed");
        printFields(ro.getExcludedFields(), "Excluded Data Members");
        out.println("<h2>Total of " + instances + " instances occupying " + totalSize + " bytes.</h2>");

        endHtml();
    }

    private void printFields(String[] fields, String title) {
        if (fields.length == 0) {
            return;
        }
        out.print("<h3>");
        print(title);
        out.println("</h3>");

        for (int i = 0; i < fields.length; i++) {
            print(fields[i]);
            out.println("<br>");
        }
    }
}
