/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.internal.jxc.apt;

import java.io.File;

/**
 * Defines constants used in the APT driver.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Const {
    private Const() {}

    /**
     * Name of the APT command-line option to take user-specified config files.
     *
     * <p>
     * It can take multiple file names separately by {@link File#pathSeparator}.
     */
    public static final String CONFIG_FILE_OPTION = "-Ajaxb.config";

    public static final String DEBUG_OPTION = "-Ajaxb.debug";
}
