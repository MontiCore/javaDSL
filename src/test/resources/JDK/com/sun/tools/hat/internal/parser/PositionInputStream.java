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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream that keeps track of total bytes read (in effect
 * 'position' in stream) from the input stream.
 *
 */
public class PositionInputStream extends FilterInputStream {
    private long position = 0L;

    public PositionInputStream(InputStream in) {
        super(in);
    }

    public int read() throws IOException {
        int res = super.read();
        if (res != -1) position++;
        return res;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int res = super.read(b, off, len);
        if (res != -1) position += res;
        return res;
    }

    public long skip(long n) throws IOException {
        long res = super.skip(n);
        position += res;
        return res;
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
        return position;
    }
}
