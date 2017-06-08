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
 * Class for generating raw HTML content to be added to HTML pages of javadoc output.
 *
 * @author Bhavesh Patel
 */
public class RawHtml extends Content{

    private String rawHtmlContent;

    public static final Content nbsp = new RawHtml("&nbsp;");

    /**
     * Constructor to construct a RawHtml object.
     *
     * @param rawHtml raw HTML text to be added
     */
    public RawHtml(String rawHtml) {
        rawHtmlContent = nullCheck(rawHtml);
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
     * This method is not supported by the class.
     *
     * @param stringContent string content that needs to be added
     * @throws DocletAbortException this method will always throw a
     *                              DocletAbortException because it
     *                              is not supported.
     */
    public void addContent(String stringContent) {
        throw new DocletAbortException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return rawHtmlContent.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public void write(StringBuilder contentBuilder) {
        contentBuilder.append(rawHtmlContent);
    }
}
