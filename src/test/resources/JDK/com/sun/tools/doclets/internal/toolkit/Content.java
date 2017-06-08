/*
 * Copyright (c) 2010, 2011, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.doclets.internal.toolkit;

import com.sun.tools.doclets.internal.toolkit.util.*;

/**
 * A class to create content for javadoc output pages.
 *
 * @author Bhavesh Patel
 */
public abstract class Content {

    /**
     * Returns a string representation of the content.
     *
     * @return string representation of the content
     */
    public String toString() {
        StringBuilder contentBuilder = new StringBuilder();
        write(contentBuilder);
        return contentBuilder.toString();
    }

    /**
     * Adds content to the existing content.
     *
     * @param content content that needs to be added
     */
    public abstract void addContent(Content content);

    /**
     * Adds a string content to the existing content.
     *
     * @param stringContent the string content to be added
     */
    public abstract void addContent(String stringContent);

    /**
     * Writes content to a StringBuilder.
     *
     */
    public abstract void write(StringBuilder contentBuilder);

    /**
     * Returns true if the content is empty.
     *
     * @return true if no content to be displayed else return false
     */
    public abstract boolean isEmpty();

    /**
     * Returns true if the content is valid.
     *
     * @return true if the content is valid else return false
     */
    public boolean isValid() {
        return !isEmpty();
    }

    /**
     * Checks for null values.
     *
     * @param t reference type to check for null values
     * @return the reference type if not null or else throws a null pointer exception
     */
    protected static <T> T nullCheck(T t) {
        t.getClass();
        return t;
    }

    /**
     * Returns true if the content ends with a newline character. Empty content
     * is considered as ending with new line.
     *
     * @param contentBuilder content to test for newline character at the end
     * @return true if the content ends with newline.
     */
    protected boolean endsWithNewLine(StringBuilder contentBuilder) {
        int contentLength = contentBuilder.length();
        if (contentLength == 0) {
            return true;
        }
        int nlLength = DocletConstants.NL.length();
        if (contentLength < nlLength) {
            return false;
        }
        int contentIndex = contentLength - 1;
        int nlIndex = nlLength - 1;
        while (nlIndex >= 0) {
            if (contentBuilder.charAt(contentIndex) != DocletConstants.NL.charAt(nlIndex)) {
                return false;
            }
            contentIndex--;
            nlIndex--;
        }
        return true;
    }
}
