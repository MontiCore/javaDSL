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

import java.io.IOException;

/**
 * Positionable read only buffer
 *
 * @author A. Sundararajan
 */
public interface ReadBuffer {
    // read methods - only byte array and int primitive types.
    // read position has to be specified always.
    public void  get(long pos, byte[] buf) throws IOException;
    public char  getChar(long pos) throws IOException;
    public byte  getByte(long pos) throws IOException;
    public short getShort(long pos) throws IOException;
    public int   getInt(long pos) throws IOException;
    public long  getLong(long pos) throws IOException;
}
