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
 * Audio processor interface.
 *
 * @author Karl Helgason
 */
public interface SoftAudioProcessor {

    public void globalParameterControlChange(int[] slothpath, long param,
            long value);

    public void init(float samplerate, float controlrate);

    public void setInput(int pin, SoftAudioBuffer input);

    public void setOutput(int pin, SoftAudioBuffer output);

    public void setMixMode(boolean mix);

    public void processAudio();

    public void processControlLogic();
}
