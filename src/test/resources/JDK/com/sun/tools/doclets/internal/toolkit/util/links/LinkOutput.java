/*
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.doclets.internal.toolkit.util.links;

/**
 * Stores output of a link.
 *
 * @author Jamie Ho
 * @since 1.5
 */
public interface LinkOutput {

    /**
     * Append the given object to the output.
     *
     * @param o the object to append.
     */
    public void append(Object o);
}
