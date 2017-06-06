/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
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


package sun.io;

import sun.nio.cs.ext.MacTurkish;

/**
 * A table to convert to MacTurkish to Unicode
 *
 * @author  ConverterGenerator tool
 */

public class ByteToCharMacTurkish extends ByteToCharSingleByte {

    private final static MacTurkish nioCoder = new MacTurkish();

    public String getCharacterEncoding() {
        return "MacTurkish";
    }

    public ByteToCharMacTurkish() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
