/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javadoc.*;

/**
 * A taglet that represents the @deprecated tag.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 *
 * @author Jamie Ho
 * @since 1.5
 */

public class DeprecatedTaglet extends BaseTaglet{

    public DeprecatedTaglet() {
        name = "deprecated";
    }

    /**
     * {@inheritDoc}
     */
    public TagletOutput getTagletOutput(Doc holder, TagletWriter writer) {
        return writer.deprecatedTagOutput(holder);
    }
}
