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

package com.sun.tools.hat.internal.parser;

import java.io.DataInputStream;
import java.io.InputStream;

/**
 * A DataInputStream that keeps track of total bytes read
 * (in effect 'position' in stream) so far.
 *
 */
public class PositionDataInputStream extends DataInputStream {
    public PositionDataInputStream(InputStream in) {
        super(in instanceof PositionInputStream?
              in : new PositionInputStream(in));
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int readLimit) {
        throw new UnsupportedOperationException("mark");
    }

    public void reset() {
        throw new UnsupportedOperationException("reset");
    }

    public long position() {
        return ((PositionInputStream)in).position();
    }
}
