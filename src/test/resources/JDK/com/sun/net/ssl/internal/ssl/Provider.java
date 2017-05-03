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

package com.sun.net.ssl.internal.ssl;

import sun.security.ssl.SunJSSE;

/**
 * Main class for the SunJSSE provider. The actual code was moved to the
 * class sun.security.ssl.SunJSSE, but for backward compatibility we
 * continue to use this class as the main Provider class.
 */
public final class Provider extends SunJSSE {

    private static final long serialVersionUID = 3231825739635378733L;

    // standard constructor
    public Provider() {
        super();
    }

    // prefered constructor to enable FIPS mode at runtime
    public Provider(java.security.Provider cryptoProvider) {
        super(cryptoProvider);
    }

    // constructor to enable FIPS mode from java.security file
    public Provider(String cryptoProvider) {
        super(cryptoProvider);
    }

    // public for now, but we may want to change it or not document it.
    public static synchronized boolean isFIPS() {
        return SunJSSE.isFIPS();
    }

    /**
     * Installs the JSSE provider.
     */
    public static synchronized void install() {
        /* nop. Remove this method in the future. */
    }

}
