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

package com.sun.tools.internal.ws.wsdl.parser;

import com.sun.tools.internal.ws.api.wsdl.TWSDLExtensible;
import com.sun.tools.internal.ws.api.wsdl.TWSDLParserContext;
import com.sun.tools.internal.ws.wscompile.ErrorReceiver;
import com.sun.xml.internal.ws.api.addressing.AddressingVersion;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * @author Arun Gupta
 */
public class MemberSubmissionAddressingExtensionHandler extends W3CAddressingExtensionHandler {
    public MemberSubmissionAddressingExtensionHandler(Map<String, AbstractExtensionHandler> extensionHandlerMap) {
        super(extensionHandlerMap);
    }

    public MemberSubmissionAddressingExtensionHandler(Map<String, AbstractExtensionHandler> extensionHandlerMap, ErrorReceiver env) {
        super(extensionHandlerMap, env);
    }

    @Override
    public String getNamespaceURI() {
        return AddressingVersion.MEMBER.wsdlNsUri;
    }

    protected QName getWSDLExtensionQName() {
        return AddressingVersion.MEMBER.wsdlExtensionTag;
    }

    @Override
    public boolean handlePortExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        // ignore any extension elements
        return false;
    }

}
