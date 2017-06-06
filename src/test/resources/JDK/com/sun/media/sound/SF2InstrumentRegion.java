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
 * Soundfont instrument region.
 *
 * @author Karl Helgason
 */
public class SF2InstrumentRegion extends SF2Region {

    protected SF2Layer layer;

    public SF2Layer getLayer() {
        return layer;
    }

    public void setLayer(SF2Layer layer) {
        this.layer = layer;
    }
}
