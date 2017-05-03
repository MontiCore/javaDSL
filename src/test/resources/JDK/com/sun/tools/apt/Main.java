/*
 * Copyright (c) 2004, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.apt;

import java.io.PrintWriter;
import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * The main program for the command-line tool apt.
 *
 * <p>Nothing described in this source file is part of any supported
 * API.  If you write code that depends on this, you do so at your own
 * risk.  This code and its internal interfaces are subject to change
 * or deletion without notice.
 */
public class Main {

    static {
        ClassLoader loader = Main.class.getClassLoader();
        if (loader != null)
            loader.setPackageAssertionStatus("com.sun.tools.apt", true);
    }

    /** Command line interface.  If args is <tt>null</tt>, a
     * <tt>NullPointerException</tt> is thrown.
     * @param args   The command line parameters.
     */
    public static void main(String... args) {
        System.exit(process(args));
    }

    /** Programatic interface.  If args is <tt>null</tt>, a
     * <tt>NullPointerException</tt> is thrown.
     * Output is directed to <tt>System.err</tt>.
     * @param args   The command line parameters.
     */
    public static int process(String... args) {
        return processing(null, null, args);
    }

    /** Programmatic interface.  If any argument
     * is <tt>null</tt>, a <tt>NullPointerException</tt> is thrown.
     * @param args   The command line parameters.
     * @param out    Where the tool's output is directed.
     */
    public static int process(PrintWriter out, String... args) {
        if (out == null)
            throw new NullPointerException("Parameter out cannot be null.");
        return processing(null, out, args);
    }

    /** Programmatic interface.  If <tt>factory</tt> or <tt>args</tt>
     * is <tt>null</tt>, a <tt>NullPointerException</tt> is thrown.
     * The &quot;<tt>-factory</tt>&quot; and &quot;<tt>-factorypath</tt>&quot;
     * command line parameters are ignored by this entry point.
     * Output is directed to <tt>System.err</tt>.
     *
     * @param factory The annotation processor factory to use
     * @param args    The command line parameters.
     */
    public static int process(AnnotationProcessorFactory factory, String... args) {
        return process(factory, new PrintWriter(System.err, true), args);
    }

    /** Programmatic interface.  If any argument
     * is <tt>null</tt>, a <tt>NullPointerException</tt> is thrown.
     * The &quot;<tt>-factory</tt>&quot; and &quot;<tt>-factorypath</tt>&quot;
     * command line parameters are ignored by this entry point.
     *
     * @param factory The annotation processor factory to use
     * @param args   The command line parameters.
     * @param out    Where the tool's output is directed.
     */
    public static int process(AnnotationProcessorFactory factory, PrintWriter out,
                              String... args) {
        if (out == null)
            throw new NullPointerException("Parameter out cannot be null.");
        if (factory == null)
            throw new NullPointerException("Parameter factory cannot be null");
        return processing(factory, out, args);
    }

    private static int processing(AnnotationProcessorFactory factory,
                                  PrintWriter out,
                                  String... args) {
        if (out == null)
            out = new PrintWriter(System.err, true);
        com.sun.tools.apt.main.Main compiler =
            new com.sun.tools.apt.main.Main("apt", out);
        return compiler.compile(args, factory);
    }
}
