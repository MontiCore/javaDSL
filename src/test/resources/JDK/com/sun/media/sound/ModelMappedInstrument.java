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

import javax.sound.midi.MidiChannel;
import javax.sound.midi.Patch;
import javax.sound.sampled.AudioFormat;

/**
 * This class is used to map instrument to another patch.
 *
 * @author Karl Helgason
 */
public class ModelMappedInstrument extends ModelInstrument {

    private ModelInstrument ins;

    public ModelMappedInstrument(ModelInstrument ins, Patch patch) {
        super(ins.getSoundbank(), patch, ins.getName(), ins.getDataClass());
        this.ins = ins;
    }

    public Object getData() {
        return ins.getData();
    }

    public ModelPerformer[] getPerformers() {
        return ins.getPerformers();
    }

    public ModelDirector getDirector(ModelPerformer[] performers,
            MidiChannel channel, ModelDirectedPlayer player) {
        return ins.getDirector(performers, channel, player);
    }

    public ModelChannelMixer getChannelMixer(MidiChannel channel,
            AudioFormat format) {
        return ins.getChannelMixer(channel, format);
    }
}
