/*
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.doclets.internal.toolkit.util;

import com.sun.javadoc.*;

/**
 * Find a tagged method.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 *
 * @author Atul M Dambalkar
 */
public class TaggedMethodFinder extends MethodFinder {
    public boolean isCorrectMethod(MethodDoc method) {
        return method.paramTags().length + method.tags("return").length +
               method.throwsTags().length + method.seeTags().length > 0;
    }
}
