/*
 * Copyright (c) 2001, 2012, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.doclets.internal.toolkit.taglets;

import com.sun.javadoc.Tag;

/**
 * An abstract class that implements the {@link Taglet} interface and
 * serves as a base for JavaFX property getter and setter taglets.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 *
 */
public abstract class BasePropertyTaglet extends BaseTaglet {

    public BasePropertyTaglet() {
    }

    /**
     * This method returns the text to be put in the resulting javadoc before
     * the property name.
     *
     * @param tagletWriter the taglet writer for output
     * @return the string to be put in the resulting javadoc.
     */
    abstract String getText(TagletWriter tagletWriter);

    /**
     * Given the <code>Tag</code> representation of this custom
     * tag, return its string representation, which is output
     * to the generated page.
     * @param tag the <code>Tag</code> representation of this custom tag.
     * @param tagletWriter the taglet writer for output.
     * @return the TagletOutput representation of this <code>Tag</code>.
     */
    public TagletOutput getTagletOutput(Tag tag, TagletWriter tagletWriter) {
        TagletOutput tagletOutput = tagletWriter.getOutputInstance();
        StringBuilder output = new StringBuilder("<P>");
        output.append(getText(tagletWriter));
        output.append(" <CODE>");
        output.append(tag.text());
        output.append("</CODE>.</P>");
        tagletOutput.setOutput(output.toString());
        return tagletOutput;
    }

    /**
     * Will return false because this tag may
     * only appear in Methods.
     * @return false since this is not a method.
     */
    public boolean inConstructor() {
        return false;
    }

    /**
     * Will return false because this tag may
     * only appear in Methods.
     * @return false since this is not a method.
     */
    public boolean inOverview() {
        return false;
    }

    /**
     * Will return false because this tag may
     * only appear in Methods.
     * @return false since this is not a method.
     */
    public boolean inPackage() {
        return false;
    }

    /**
     * Will return false because this tag may
     * only appear in Methods.
     * @return false since this is not a method.
     */
    public boolean inType() {
        return false;
    }

    /**
     * Will return false because this tag is not inline.
     * @return false since this is not an inline tag.
     */
    public boolean isInlineTag() {
        return false;
    }

}
