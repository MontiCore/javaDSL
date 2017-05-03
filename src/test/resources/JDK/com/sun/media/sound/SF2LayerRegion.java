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
 * Soundfont layer region.
 *
 * @author Karl Helgason
 */
public class SF2LayerRegion extends SF2Region {

    protected SF2Sample sample;

    public SF2Sample getSample() {
        return sample;
    }

    public void setSample(SF2Sample sample) {
        this.sample = sample;
    }
}
