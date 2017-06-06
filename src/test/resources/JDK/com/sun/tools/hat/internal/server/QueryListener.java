/*
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
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


/*
 * The Original Code is HAT. The Initial Developer of the
 * Original Code is Bill Foote, with contributions from others
 * at JavaSoft/Sun.
 */

package com.sun.tools.hat.internal.server;

/**
 *
 * @author      Bill Foote
 */


import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedOutputStream;

import com.sun.tools.hat.internal.model.Snapshot;
import com.sun.tools.hat.internal.oql.OQLEngine;

public class QueryListener implements Runnable {


    private Snapshot snapshot;
    private OQLEngine engine;
    private int port;

    public QueryListener(int port) {
        this.port = port;
        this.snapshot = null;   // Client will setModel when it's ready
        this.engine = null; // created when snapshot is set
    }

    public void setModel(Snapshot ss) {
        this.snapshot = ss;
        if (OQLEngine.isOQLSupported()) {
            this.engine = new OQLEngine(ss);
        }
    }

    public void run() {
        try {
            waitForRequests();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private void waitForRequests() throws IOException {
        ServerSocket ss = new ServerSocket(port);
        Thread last = null;
        for (;;) {
            Socket s = ss.accept();
            Thread t = new Thread(new HttpReader(s, snapshot, engine));
            if (snapshot == null) {
                t.setPriority(Thread.NORM_PRIORITY+1);
            } else {
                t.setPriority(Thread.NORM_PRIORITY-1);
                if (last != null) {
                    try {
                        last.setPriority(Thread.NORM_PRIORITY-2);
                    } catch (Throwable ignored) {
                    }
                    // If the thread is no longer alive, we'll get a
                    // NullPointerException
                }
            }
            t.start();
            last = t;
        }
    }

}
