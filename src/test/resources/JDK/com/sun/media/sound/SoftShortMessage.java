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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

/**
 * A short message class that support for than 16 midi channels.
 *
 * @author Karl Helgason
 */
public class SoftShortMessage extends ShortMessage {

    int channel = 0;

    public int getChannel() {
        return channel;
    }

    public void setMessage(int command, int channel, int data1, int data2)
            throws InvalidMidiDataException {
        this.channel = channel;
        super.setMessage(command, channel & 0xF, data1, data2);
    }

    public Object clone() {
        SoftShortMessage clone = new SoftShortMessage();
        try {
            clone.setMessage(getCommand(), getChannel(), getData1(), getData2());
        } catch (InvalidMidiDataException e) {
            throw new IllegalArgumentException(e);
        }
        return clone;
    }
}
