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
public final class UtilMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.tools.internal.ws.resources.util");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableSAX_2_DOM_NOTSUPPORTED_CREATEELEMENT(Object arg0) {
        return messageFactory.getMessage("sax2dom.notsupported.createelement", arg0);
    }

    /**
     * SAX2DOMEx.DomImplDoesntSupportCreateElementNs: {0}
     *
     */
    public static String SAX_2_DOM_NOTSUPPORTED_CREATEELEMENT(Object arg0) {
        return localizer.localize(localizableSAX_2_DOM_NOTSUPPORTED_CREATEELEMENT(arg0));
    }

    public static Localizable localizableNULL_NAMESPACE_FOUND(Object arg0) {
        return messageFactory.getMessage("null.namespace.found", arg0);
    }

    /**
     * Encountered error in wsdl. Check namespace of element <{0}>
     *
     */
    public static String NULL_NAMESPACE_FOUND(Object arg0) {
        return localizer.localize(localizableNULL_NAMESPACE_FOUND(arg0));
    }

    public static Localizable localizableHOLDER_VALUEFIELD_NOT_FOUND(Object arg0) {
        return messageFactory.getMessage("holder.valuefield.not.found", arg0);
    }

    /**
     * Could not find the field in the Holder that contains the Holder''s value: {0}
     *
     */
    public static String HOLDER_VALUEFIELD_NOT_FOUND(Object arg0) {
        return localizer.localize(localizableHOLDER_VALUEFIELD_NOT_FOUND(arg0));
    }

}
