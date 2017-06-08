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

package com.sun.tools.internal.ws.wsdl.document.http;

import javax.xml.namespace.QName;

/**
 * Interface defining HTTP-extension-related constants.
 *
 * @author WS Development Team
 */
public interface HTTPConstants {

    // namespace URIs
    public static String NS_WSDL_HTTP = "http://schemas.xmlsoap.org/wsdl/http/";

    // QNames
    public static QName QNAME_ADDRESS = new QName(NS_WSDL_HTTP, "address");
    public static QName QNAME_BINDING = new QName(NS_WSDL_HTTP, "binding");
    public static QName QNAME_OPERATION = new QName(NS_WSDL_HTTP, "operation");
    public static QName QNAME_URL_ENCODED =
        new QName(NS_WSDL_HTTP, "urlEncoded");
    public static QName QNAME_URL_REPLACEMENT =
        new QName(NS_WSDL_HTTP, "urlReplacement");
}
