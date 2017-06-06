/*
 * Copyright (c) 2003, 2009, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.doclets.formats.html;

import com.sun.tools.doclets.internal.toolkit.taglets.*;

/**
 * The output for HTML taglets.
 *
 * @since 1.5
 * @author Jamie Ho
 */

public class TagletOutputImpl implements TagletOutput {

    private StringBuffer output;

    public TagletOutputImpl(String o) {
        setOutput(o);
    }

    /**
     * {@inheritDoc}
     */
    public void setOutput (Object o) {
        output = new StringBuffer(o == null ? "" : (String) o);
    }

    /**
     * {@inheritDoc}
     */
    public void appendOutput(TagletOutput o) {
        output.append(o.toString());
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasInheritDocTag() {
        return output.indexOf(InheritDocTaglet.INHERIT_DOC_INLINE_TAG) != -1;
    }

    public String toString() {
        return output.toString();
    }

    /**
     * Check whether the taglet output is empty.
     */
    public boolean isEmpty() {
        return (toString().trim().isEmpty());
    }
}
