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

package com.sun.tools.hat.internal.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.Hashtable;

/**
 * This represents a set of data members that should be excluded from the
 * reachable objects query.
 * This is useful to exclude observers from the
 * transitive closure of objects reachable from a given object, allowing
 * some kind of real determination of the "size" of that object.
 *
 * @author      Bill Foote
 */
public class ReachableExcludesImpl implements ReachableExcludes {

    private File excludesFile;
    private long lastModified;
    private Hashtable methods;  // Hashtable<String, String>, used as a bag

    /**
     * Create a new ReachableExcludesImpl over the given file.  The file will be
     * re-read whenever the timestamp changes.
     */
    public ReachableExcludesImpl(File excludesFile) {
        this.excludesFile = excludesFile;
        readFile();
    }

    private void readFileIfNeeded() {
        if (excludesFile.lastModified() != lastModified) {
            synchronized(this) {
                if (excludesFile.lastModified() != lastModified) {
                    readFile();
                }
            }
        }
    }

    private void readFile() {
        long lm = excludesFile.lastModified();
        Hashtable<String, String> m = new Hashtable<String, String>();

        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(
                                    new FileInputStream(excludesFile)));

            String method;
            while ((method = r.readLine()) != null) {
                m.put(method, method);
            }
            lastModified = lm;
            methods = m;        // We want this to be atomic
        } catch (IOException ex) {
            System.out.println("Error reading " + excludesFile + ":  " + ex);
        }
    }

    /**
     * @return true iff the given field is on the histlist of excluded
     *          fields.
     */
    public boolean isExcluded(String fieldName) {
        readFileIfNeeded();
        return methods.get(fieldName) != null;
    }
}
