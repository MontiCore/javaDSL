/*
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
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

import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import javax.sound.sampled.spi.FormatConversionProvider;


/**
 * A codec can encode and/or decode audio data.  It provides an
 * AudioInputStream from which processed data may be read.
 * <p>
 * Its input format represents the format of the incoming
 * audio data, or the format of the data in the underlying stream.
 * <p>
 * Its output format represents the format of the processed, outgoing
 * audio data.  This is the format of the data which may be read from
 * the filtered stream.
 *
 * @author Kara Kytle
 */
abstract class SunCodec extends FormatConversionProvider {

    AudioFormat.Encoding[] inputEncodings;
    AudioFormat.Encoding[] outputEncodings;

    /**
     * Constructs a new codec object.
     */
    protected SunCodec(AudioFormat.Encoding[] inputEncodings, AudioFormat.Encoding[] outputEncodings) {

        this.inputEncodings = inputEncodings;
        this.outputEncodings = outputEncodings;
    }


    /**
     */
    public AudioFormat.Encoding[] getSourceEncodings() {

        AudioFormat.Encoding[] encodings = new AudioFormat.Encoding[inputEncodings.length];
        System.arraycopy(inputEncodings, 0, encodings, 0, inputEncodings.length);
        return encodings;
    }
    /**
     */
    public AudioFormat.Encoding[] getTargetEncodings() {

        AudioFormat.Encoding[] encodings = new AudioFormat.Encoding[outputEncodings.length];
        System.arraycopy(outputEncodings, 0, encodings, 0, outputEncodings.length);
        return encodings;
    }

    /**
     */
    public abstract AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat);


    /**
     */
    public abstract AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat);


    /**
     */
    public abstract AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream);
    /**
     */
    public abstract AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream);


}
