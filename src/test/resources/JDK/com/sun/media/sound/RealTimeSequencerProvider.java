/*
 * Copyright (c) 2003, 2007, Oracle and/or its affiliates. All rights reserved.
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

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.spi.MidiDeviceProvider;

/**
 * Provider for RealTimeSequencer objects.
 *
 * @author Florian Bomers
 */
public class RealTimeSequencerProvider extends MidiDeviceProvider {


    public MidiDevice.Info[] getDeviceInfo() {

        MidiDevice.Info[] localArray = { RealTimeSequencer.info };
        return localArray;
    }


    public MidiDevice getDevice(MidiDevice.Info info) {
        if ((info != null) && (!info.equals(RealTimeSequencer.info))) {
            return null;
        }

        try {
            return new RealTimeSequencer();
        } catch (MidiUnavailableException e) {
            return null;
        }
    }
}
