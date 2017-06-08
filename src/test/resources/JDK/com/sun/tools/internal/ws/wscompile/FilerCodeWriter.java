/*
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.internal.ws.wscompile;

import com.sun.codemodel.internal.JPackage;
import com.sun.mirror.apt.Filer;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * Writes all the source files using the specified Filer.
 *
 * @author WS Development Team
 */
public class FilerCodeWriter extends WSCodeWriter {

    /** The Filer used to create files. */
    private final Filer filer;

    private Writer w;

    public FilerCodeWriter(File outDir, WsgenOptions options) throws IOException {
        super(outDir, options);
        this.filer = options.filer;
    }


    public Writer openSource(JPackage pkg, String fileName) throws IOException {
        String tmp = fileName.substring(0, fileName.length()-5);
        w = filer.createSourceFile(pkg.name()+"."+tmp);
        return w;
    }


    public void close() throws IOException {
        super.close();
        if (w != null)
            w.close();
        w = null;
    }
}
