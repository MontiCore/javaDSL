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


package com.sun.tools.internal.ws.resources;

import com.sun.xml.internal.ws.util.localization.Localizable;
import com.sun.xml.internal.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.internal.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 *
 */
public final class ConfigurationMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.tools.internal.ws.resources.configuration");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableCONFIGURATION_INVALID_ELEMENT(Object arg0, Object arg1, Object arg2) {
        return messageFactory.getMessage("configuration.invalidElement", arg0, arg1, arg2);
    }

    /**
     * invalid element "{2}" in file "{0}" (line {1})
     *
     */
    public static String CONFIGURATION_INVALID_ELEMENT(Object arg0, Object arg1, Object arg2) {
        return localizer.localize(localizableCONFIGURATION_INVALID_ELEMENT(arg0, arg1, arg2));
    }

    public static Localizable localizableCONFIGURATION_NOT_BINDING_FILE(Object arg0) {
        return messageFactory.getMessage("configuration.notBindingFile", arg0);
    }

    /**
     * Ignoring: binding file ""{0}". It is not a jaxws or a jaxb binding file.
     *
     */
    public static String CONFIGURATION_NOT_BINDING_FILE(Object arg0) {
        return localizer.localize(localizableCONFIGURATION_NOT_BINDING_FILE(arg0));
    }

}
