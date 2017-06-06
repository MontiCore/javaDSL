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

import java.util.*;
import com.sun.tools.doclets.internal.toolkit.Content;
import com.sun.tools.doclets.internal.toolkit.util.*;

/**
 * Class for generating an HTML document for javadoc output.
 *
 * @author Bhavesh Patel
 */
public class HtmlDocument extends Content {

    private List<Content> docContent = Collections.<Content>emptyList();

    /**
     * Constructor to construct an HTML document.
     *
     * @param docType document type for the HTML document
     * @param docComment comment for the document
     * @param htmlTree HTML tree of the document
     */
    public HtmlDocument(Content docType, Content docComment, Content htmlTree) {
        docContent = new ArrayList<Content>();
        addContent(nullCheck(docType));
        addContent(nullCheck(docComment));
        addContent(nullCheck(htmlTree));
    }

    /**
     * Constructor to construct an HTML document.
     *
     * @param docType document type for the HTML document
     * @param htmlTree HTML tree of the document
     */
    public HtmlDocument(Content docType, Content htmlTree) {
        docContent = new ArrayList<Content>();
        addContent(nullCheck(docType));
        addContent(nullCheck(htmlTree));
    }

    /**
     * Adds content for the HTML document.
     *
     * @param htmlContent html content to be added
     */
    public void addContent(Content htmlContent) {
        if (htmlContent.isValid())
            docContent.add(htmlContent);
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
        return (docContent.isEmpty());
    }

    /**
     * {@inheritDoc}
     */
    public void write(StringBuilder contentBuilder) {
        for (Content c : docContent)
            c.write(contentBuilder);
    }
}
