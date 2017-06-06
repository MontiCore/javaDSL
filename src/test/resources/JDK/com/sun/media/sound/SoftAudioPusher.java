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

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;

/**
 * This is a processor object that writes into SourceDataLine
 *
 * @author Karl Helgason
 */
public class SoftAudioPusher implements Runnable {

    private volatile boolean active = false;
    private SourceDataLine sourceDataLine = null;
    private Thread audiothread;
    private AudioInputStream ais;
    private byte[] buffer;

    public SoftAudioPusher(SourceDataLine sourceDataLine, AudioInputStream ais,
            int workbuffersizer) {
        this.ais = ais;
        this.buffer = new byte[workbuffersizer];
        this.sourceDataLine = sourceDataLine;
    }

    public synchronized void start() {
        if (active)
            return;
        active = true;
        audiothread = new Thread(this);
        audiothread.setDaemon(true);
        audiothread.setPriority(Thread.MAX_PRIORITY);
        audiothread.start();
    }

    public synchronized void stop() {
        if (!active)
            return;
        active = false;
        try {
            audiothread.join();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }

    public void run() {
        byte[] buffer = SoftAudioPusher.this.buffer;
        AudioInputStream ais = SoftAudioPusher.this.ais;
        SourceDataLine sourceDataLine = SoftAudioPusher.this.sourceDataLine;

        try {
            while (active) {
                // Read from audio source
                int count = ais.read(buffer);
                if(count < 0) break;
                // Write byte buffer to source output
                sourceDataLine.write(buffer, 0, count);
            }
        } catch (IOException e) {
            active = false;
            //e.printStackTrace();
        }

    }
}
