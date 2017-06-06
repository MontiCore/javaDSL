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

import com.sun.tools.doclets.internal.toolkit.Content;
import com.sun.tools.doclets.internal.toolkit.util.*;

/**
 * Class for generating string content for HTML tags of javadoc output.
 *
 * @author Bhavesh Patel
 */
public class StringContent extends Content{

    private StringBuilder stringContent;

    /**
     * Constructor to construct StringContent object.
     */
    public StringContent() {
        stringContent = new StringBuilder();
    }

    /**
     * Constructor to construct StringContent object with some initial content.
     *
     * @param initialContent initial content for the object
     */
    public StringContent(String initialContent) {
        stringContent = new StringBuilder(
                Util.escapeHtmlChars(nullCheck(initialContent)));
    }

    /**
     * This method is not supported by the class.
     *
     * @param content content that needs to be added
     * @throws DocletAbortException this method will always throw a
     *                              DocletAbortException because it
     *                              is not supported.
     */
    public void addContent(Content content) {
        throw new DocletAbortException();
    }

    /**
     * Adds content for the StringContent object.  The method escapes
     * HTML characters for the string content that is added.
     *
     * @param strContent string content to be added
     */
    public void addContent(String strContent) {
        stringContent.append(Util.escapeHtmlChars(nullCheck(strContent)));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return (stringContent.length() == 0);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return stringContent.toString();
    }

    /**
     * {@inheritDoc}
     */
    public void write(StringBuilder contentBuilder) {
        contentBuilder.append(stringContent);
    }
}
