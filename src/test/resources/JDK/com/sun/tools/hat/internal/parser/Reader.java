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

import java.io.*;
import com.sun.tools.hat.internal.model.*;

/**
 * Abstract base class for reading object dump files.  A reader need not be
 * thread-safe.
 *
 * @author      Bill Foote
 */


public abstract class Reader {
    protected PositionDataInputStream in;

    protected Reader(PositionDataInputStream in) {
        this.in = in;
    }

    /**
     * Read a snapshot from a data input stream.  It is assumed that the magic
     * number has already been read.
     */
    abstract public Snapshot read() throws IOException;

    /**
     * Read a snapshot from a file.
     *
     * @param heapFile The name of a file containing a heap dump
     * @param callStack If true, read the call stack of allocaation sites
     */
    public static Snapshot readFile(String heapFile, boolean callStack,
                                    int debugLevel)
            throws IOException {
        int dumpNumber = 1;
        int pos = heapFile.lastIndexOf('#');
        if (pos > -1) {
            String num = heapFile.substring(pos+1, heapFile.length());
            try {
                dumpNumber = Integer.parseInt(num, 10);
            } catch (java.lang.NumberFormatException ex) {
                String msg = "In file name \"" + heapFile
                             + "\", a dump number was "
                             + "expected after the :, but \""
                             + num + "\" was found instead.";
                System.err.println(msg);
                throw new IOException(msg);
            }
            heapFile = heapFile.substring(0, pos);
        }
        PositionDataInputStream in = new PositionDataInputStream(
            new BufferedInputStream(new FileInputStream(heapFile)));
        try {
            int i = in.readInt();
            if (i == HprofReader.MAGIC_NUMBER) {
                Reader r
                    = new HprofReader(heapFile, in, dumpNumber,
                                      callStack, debugLevel);
                return r.read();
            } else {
                throw new IOException("Unrecognized magic number: " + i);
            }
        } finally {
            in.close();
        }
    }
}
