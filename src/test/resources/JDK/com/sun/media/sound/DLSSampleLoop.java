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
 * This class is used to store loop points inside DLSSampleOptions class.
 *
 * @author Karl Helgason
 */
public class DLSSampleLoop {

    public final static int LOOP_TYPE_FORWARD = 0;
    public final static int LOOP_TYPE_RELEASE = 1;
    protected long type;
    protected long start;
    protected long length;

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }
}
