/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.net.ssl.internal.www.protocol.https;

import java.io.IOException;
import java.net.URL;
import java.net.Proxy;

/**
 * This class exists for compatibility with previous JSSE releases
 * only. The HTTPS implementation can now be found in
 * sun.net.www.protocol.https.
 *
 */
public class Handler extends sun.net.www.protocol.https.Handler {

    public Handler() {
        super();
    }

    public Handler(String proxy, int port) {
        super(proxy, port);
    }

    protected java.net.URLConnection openConnection(URL u) throws IOException {
        return openConnection(u, (Proxy)null);
    }

    protected java.net.URLConnection openConnection(URL u, Proxy p) throws IOException {
        return new HttpsURLConnectionOldImpl(u, p, this);
    }
}
