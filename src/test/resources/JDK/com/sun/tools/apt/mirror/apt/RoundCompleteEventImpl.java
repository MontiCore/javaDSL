/*
 * Copyright (c) 2004, 2009, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.apt.mirror.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.RoundCompleteEvent;
import com.sun.mirror.apt.RoundState;

@SuppressWarnings("deprecation")
public class RoundCompleteEventImpl extends RoundCompleteEvent {
    private static final long serialVersionUID = 7067621446720784300L;

    public RoundCompleteEventImpl(AnnotationProcessorEnvironment source,
                                  RoundState rs) {
        super(source, rs);
    }
}
