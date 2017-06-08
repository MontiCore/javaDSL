/*
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.extcheck;

import java.io.*;

/**
 * Main program of extcheck
 */

public final class Main {
    public static final String INSUFFICIENT = "Insufficient number of arguments";
    public static final String MISSING = "Missing <jar file> argument";
    public static final String DOES_NOT_EXIST = "Jarfile does not exist: ";
    public static final String EXTRA = "Extra command line argument: ";

    /**
     * Terminates with one of the following codes
     *  1 A newer (or same version) jar file is already installed
     *  0 No newer jar file was found
     *  -1 An internal error occurred
     */
    public static void main(String args[]) {
        try {
            realMain(args);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(-1);
        }
    }

    public static void realMain(String[] args) throws Exception {
        if (args.length < 1) {
            usage(INSUFFICIENT);
        }
        int argIndex = 0;
        boolean verboseFlag = false;
        if (args[argIndex].equals("-verbose")) {
            verboseFlag = true;
            argIndex++;
            if (argIndex >= args.length) {
                usage(MISSING);
            }
        }
        String jarName = args[argIndex];
        argIndex++;
        File jarFile = new File(jarName);
        if (!jarFile.exists()){
            usage(DOES_NOT_EXIST + jarName);
        }
        if (argIndex < args.length) {
            usage(EXTRA + args[argIndex]);
        }
        ExtCheck jt = ExtCheck.create(jarFile,verboseFlag);
        boolean result = jt.checkInstalledAgainstTarget();
        if (result) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }

    private static void usage(String msg) throws Exception {
        throw new Exception(msg + "\nUsage: extcheck [-verbose] <jar file>");
    }
}

