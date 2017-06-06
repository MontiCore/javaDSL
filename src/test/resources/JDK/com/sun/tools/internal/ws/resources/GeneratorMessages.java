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
public final class GeneratorMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.tools.internal.ws.resources.generator");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableGENERATOR_SERVICE_CLASS_ALREADY_EXIST(Object arg0, Object arg1) {
        return messageFactory.getMessage("generator.service.classAlreadyExist", arg0, arg1);
    }

    /**
     * Could not generate Service, class: {0} already exists. Rename wsdl:Service "{1}" using JAX-WS customization
     *
     */
    public static String GENERATOR_SERVICE_CLASS_ALREADY_EXIST(Object arg0, Object arg1) {
        return localizer.localize(localizableGENERATOR_SERVICE_CLASS_ALREADY_EXIST(arg0, arg1));
    }

    public static Localizable localizableGENERATOR_SEI_CLASS_ALREADY_EXIST(Object arg0, Object arg1) {
        return messageFactory.getMessage("generator.sei.classAlreadyExist", arg0, arg1);
    }

    /**
     * Could not generate SEI, class: {0} already exists. Rename wsdl:portType "{1}" using JAX-WS customization
     *
     */
    public static String GENERATOR_SEI_CLASS_ALREADY_EXIST(Object arg0, Object arg1) {
        return localizer.localize(localizableGENERATOR_SEI_CLASS_ALREADY_EXIST(arg0, arg1));
    }

    public static Localizable localizableGENERATOR_NESTED_GENERATOR_ERROR(Object arg0) {
        return messageFactory.getMessage("generator.nestedGeneratorError", arg0);
    }

    /**
     * generator error: {0}
     *
     */
    public static String GENERATOR_NESTED_GENERATOR_ERROR(Object arg0) {
        return localizer.localize(localizableGENERATOR_NESTED_GENERATOR_ERROR(arg0));
    }

    public static Localizable localizableGENERATOR_INTERNAL_ERROR_SHOULD_NOT_HAPPEN(Object arg0) {
        return messageFactory.getMessage("generator.internal.error.should.not.happen", arg0);
    }

    /**
     * internal error (should not happen): {0}
     *
     */
    public static String GENERATOR_INTERNAL_ERROR_SHOULD_NOT_HAPPEN(Object arg0) {
        return localizer.localize(localizableGENERATOR_INTERNAL_ERROR_SHOULD_NOT_HAPPEN(arg0));
    }

    public static Localizable localizableGENERATOR_INDENTINGWRITER_CHARSET_CANTENCODE(Object arg0) {
        return messageFactory.getMessage("generator.indentingwriter.charset.cantencode", arg0);
    }

    /**
     * WSDL has some characters which native java encoder can''t encode: "{0}"
     *
     */
    public static String GENERATOR_INDENTINGWRITER_CHARSET_CANTENCODE(Object arg0) {
        return localizer.localize(localizableGENERATOR_INDENTINGWRITER_CHARSET_CANTENCODE(arg0));
    }

    public static Localizable localizableGENERATOR_CANNOT_CREATE_DIR(Object arg0) {
        return messageFactory.getMessage("generator.cannot.create.dir", arg0);
    }

    /**
     * can''t create directory: {0}
     *
     */
    public static String GENERATOR_CANNOT_CREATE_DIR(Object arg0) {
        return localizer.localize(localizableGENERATOR_CANNOT_CREATE_DIR(arg0));
    }

    public static Localizable localizableGENERATOR_CANT_WRITE(Object arg0) {
        return messageFactory.getMessage("generator.cant.write", arg0);
    }

    /**
     * can''t write file: {0}
     *
     */
    public static String GENERATOR_CANT_WRITE(Object arg0) {
        return localizer.localize(localizableGENERATOR_CANT_WRITE(arg0));
    }

}
