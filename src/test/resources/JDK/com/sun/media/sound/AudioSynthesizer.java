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

import java.util.Map;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;

/**
 * <code>AudioSynthesizer</code> is a <code>Synthesizer</code>
 * which renders it's output audio into <code>SourceDataLine</code>
 * or <code>AudioInputStream</code>.
 *
 * @see MidiSystem#getSynthesizer
 * @see Synthesizer
 *
 * @author Karl Helgason
 */
public interface AudioSynthesizer extends Synthesizer {

    /**
     * Obtains the current format (encoding, sample rate, number of channels,
     * etc.) of the synthesizer audio data.
     *
     * <p>If the synthesizer is not open and has never been opened, it returns
     * the default format.
     *
     * @return current audio data format
     * @see AudioFormat
     */
    public AudioFormat getFormat();

    /**
     * Gets information about the possible properties for the synthesizer.
     *
     * @param info a proposed list of tag/value pairs that will be sent on open.
     * @return an array of <code>AudioSynthesizerPropertyInfo</code> objects
     * describing possible properties. This array may be an empty array if
     * no properties are required.
     */
    public AudioSynthesizerPropertyInfo[] getPropertyInfo(
            Map<String, Object> info);

    /**
     * Opens the synthesizer and starts rendering audio into
     * <code>SourceDataLine</code>.
     *
     * <p>An application opening a synthesizer explicitly with this call
     * has to close the synthesizer by calling {@link #close}. This is
     * necessary to release system resources and allow applications to
     * exit cleanly.
     *
     * <p>Note that some synthesizers, once closed, cannot be reopened.
     * Attempts to reopen such a synthesizer will always result in
     * a <code>MidiUnavailableException</code>.
     *
     * @param line which <code>AudioSynthesizer</code> writes output audio into.
     * If <code>line</code> is null, then line from system default mixer is used.
     * @param info a <code>Map<String,Object></code> object containing
     * properties for additional configuration supported by synthesizer.
     * If <code>info</code> is null then default settings are used.
     *
     * @throws MidiUnavailableException thrown if the synthesizer cannot be
     * opened due to resource restrictions.
     * @throws SecurityException thrown if the synthesizer cannot be
     * opened due to security restrictions.
     *
     * @see #close
     * @see #isOpen
     */
    public void open(SourceDataLine line, Map<String, Object> info)
            throws MidiUnavailableException;

    /**
     * Opens the synthesizer and renders audio into returned
     * <code>AudioInputStream</code>.
     *
     * <p>An application opening a synthesizer explicitly with this call
     * has to close the synthesizer by calling {@link #close}. This is
     * necessary to release system resources and allow applications to
     * exit cleanly.
     *
     * <p>Note that some synthesizers, once closed, cannot be reopened.
     * Attempts to reopen such a synthesizer will always result in
     * a <code>MidiUnavailableException<code>.
     *
     * @param targetFormat specifies the <code>AudioFormat</code>
     * used in returned <code>AudioInputStream</code>.
     * @param info a <code>Map<String,Object></code> object containing
     * properties for additional configuration supported by synthesizer.
     * If <code>info</code> is null then default settings are used.
     *
     * @throws MidiUnavailableException thrown if the synthesizer cannot be
     * opened due to resource restrictions.
     * @throws SecurityException thrown if the synthesizer cannot be
     * opened due to security restrictions.
     *
     * @see #close
     * @see #isOpen
     */
    public AudioInputStream openStream(AudioFormat targetFormat,
            Map<String, Object> info) throws MidiUnavailableException;
}
