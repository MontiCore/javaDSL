/*
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.media.sound;

/**
 * This exception is used when a RIFF file contains illegal or unexpected data.
 *
 * @author Karl Helgason
 */
public class RIFFInvalidDataException extends InvalidDataException {

    private static final long serialVersionUID = 1L;

    public RIFFInvalidDataException() {
        super("Invalid Data!");
    }

    public RIFFInvalidDataException(String s) {
        super(s);
    }
}
