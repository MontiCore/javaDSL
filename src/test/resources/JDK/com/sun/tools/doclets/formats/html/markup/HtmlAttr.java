/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.doclets.formats.html.markup;

/**
 * Enum representing HTML tag attributes.
 *
 * @author Bhavesh Patel
 */
public enum HtmlAttr {
    ALT,
    BORDER,
    CELLPADDING,
    CELLSPACING,
    CHARSET,
    CLASS,
    CLEAR,
    COLS,
    CONTENT,
    HREF,
    HTTP_EQUIV("http-equiv"),
    ID,
    LANG,
    NAME,
    ONLOAD,
    REL,
    ROWS,
    SCOPE,
    SCROLLING,
    SRC,
    SUMMARY,
    TARGET,
    TITLE,
    TYPE,
    WIDTH;

    private final String value;

    HtmlAttr() {
        this.value = name().toLowerCase();
    }

    HtmlAttr(String name) {
        this.value = name;
    }

    public String toString() {
        return value;
    }
}
