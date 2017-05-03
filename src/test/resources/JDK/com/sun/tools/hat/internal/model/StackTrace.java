/*
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
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


/*
 * The Original Code is HAT. The Initial Developer of the
 * Original Code is Bill Foote, with contributions from others
 * at JavaSoft/Sun.
 */

package com.sun.tools.hat.internal.model;

/**
 *
 * @author      Bill Foote
 */


/**
 * Represents a stack trace, that is, an ordered collection of stack frames.
 */

public class StackTrace {

    private StackFrame[] frames;

    public StackTrace(StackFrame[] frames) {
        this.frames = frames;
    }

    /**
     * @param depth.  The minimum reasonable depth is 1.
     *
     * @return a (possibly new) StackTrace that is limited to depth.
     */
    public StackTrace traceForDepth(int depth) {
        if (depth >= frames.length) {
            return this;
        } else {
            StackFrame[] f = new StackFrame[depth];
            System.arraycopy(frames, 0, f, 0, depth);
            return new StackTrace(f);
        }
    }

    public void resolve(Snapshot snapshot) {
        for (int i = 0; i < frames.length; i++) {
            frames[i].resolve(snapshot);
        }
    }

    public StackFrame[] getFrames() {
        return frames;
    }
}
