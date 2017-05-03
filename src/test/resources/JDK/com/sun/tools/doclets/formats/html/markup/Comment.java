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
 * Class for generating a comment for HTML pages of javadoc output.
 *
 * @author Bhavesh Patel
 */
public class Comment extends Content{

    private String commentText;

    /**
     * Constructor to construct a Comment object.
     *
     * @param comment comment text for the comment
     */
    public Comment(String comment) {
        commentText = nullCheck(comment);
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
        return commentText.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public void write(StringBuilder contentBuilder) {
        if (!endsWithNewLine(contentBuilder))
            contentBuilder.append(DocletConstants.NL);
        contentBuilder.append("<!-- ");
        contentBuilder.append(commentText);
        contentBuilder.append(" -->" + DocletConstants.NL);
    }
}
