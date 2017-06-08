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

import com.sun.javadoc.*;

/**
 * A taglet that adds the initial line of documentation to the JavaFX
 * property setters.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 */
public class PropertySetterTaglet extends BasePropertyTaglet {

    /**
     * Construct a new PropertyGetterTaglet.
     */
    public PropertySetterTaglet () {
        name = "propertySetter";
    }

    @Override
    String getText(TagletWriter tagletWriter) {
        return tagletWriter.configuration().getText("doclet.PropertySetter");
    }
}
