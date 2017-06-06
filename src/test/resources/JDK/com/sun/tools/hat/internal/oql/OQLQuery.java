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
 * This represents a parsed OQL query
 *
 */
class OQLQuery {
    OQLQuery(String selectExpr, boolean isInstanceOf,
             String className, String identifier, String whereExpr) {
        this.selectExpr = selectExpr;
        this.isInstanceOf = isInstanceOf;
        this.className = className;
        this.identifier = identifier;
        this.whereExpr = whereExpr;
    }

    String   selectExpr;
    boolean  isInstanceOf;
    String   className;
    String   identifier;
    String   whereExpr;
}
