/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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

import javax.sound.midi.*;


/**
 * Helper class which allows to convert {@code Transmitter}
 * to {@code MidiDeviceTransmitter}.
 *
 * @author Alex Menkov
 */
public class MidiDeviceTransmitterEnvelope implements MidiDeviceTransmitter {

    private final MidiDevice device;
    private final Transmitter transmitter;

    /**
     * Creates a new {@code MidiDeviceTransmitterEnvelope} object which
     * envelops the specified {@code Transmitter}
     * and is owned by the specified {@code MidiDevice}.
     *
     * @param device the owner {@code MidiDevice}
     * @param transmitter the {@code Transmitter} to be enveloped
     */
    public MidiDeviceTransmitterEnvelope(MidiDevice device, Transmitter transmitter) {
        if (device == null || transmitter == null) {
            throw new NullPointerException();
        }
        this.device = device;
        this.transmitter = transmitter;
    }

    // Transmitter implementation
    public void setReceiver(Receiver receiver) {
        transmitter.setReceiver(receiver);
    }

    public Receiver getReceiver() {
        return transmitter.getReceiver();
    }

    public void close() {
        transmitter.close();
    }


    // MidiDeviceReceiver implementation
    public MidiDevice getMidiDevice() {
        return device;
    }

    /**
     * Obtains the transmitter enveloped
     * by this {@code MidiDeviceTransmitterEnvelope} object.
     *
     * @return the enveloped transmitter
     */
    public Transmitter getTransmitter() {
        return transmitter;
    }
}
