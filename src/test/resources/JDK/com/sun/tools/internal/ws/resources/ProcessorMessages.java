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
public final class ProcessorMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.tools.internal.ws.resources.processor");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizablePROCESSOR_MISSING_MODEL() {
        return messageFactory.getMessage("processor.missing.model");
    }

    /**
     * model is missing
     *
     */
    public static String PROCESSOR_MISSING_MODEL() {
        return localizer.localize(localizablePROCESSOR_MISSING_MODEL());
    }

}
