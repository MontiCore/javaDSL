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
 * This is a wavetable oscillator interface.
 *
 * @author Karl Helgason
 */
public interface ModelWavetable extends ModelOscillator {

    public static final int LOOP_TYPE_OFF = 0;
    public static final int LOOP_TYPE_FORWARD = 1;
    public static final int LOOP_TYPE_RELEASE = 2;
    public static final int LOOP_TYPE_PINGPONG = 4;
    public static final int LOOP_TYPE_REVERSE = 8;

    public AudioFloatInputStream openStream();

    public float getLoopLength();

    public float getLoopStart();

    public int getLoopType();

    public float getPitchcorrection();
}
