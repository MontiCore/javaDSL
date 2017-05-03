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

package com.sun.xml.internal.ws.client;

import com.sun.xml.internal.ws.api.server.Container;
import com.sun.xml.internal.ws.model.wsdl.WSDLServiceImpl;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import com.sun.org.glassfish.external.amx.AMXGlassfish;
import com.sun.org.glassfish.gmbal.AMXMetadata;
import com.sun.org.glassfish.gmbal.Description;
import com.sun.org.glassfish.gmbal.InheritedAttribute;
import com.sun.org.glassfish.gmbal.InheritedAttributes;
import com.sun.org.glassfish.gmbal.ManagedAttribute;
import com.sun.org.glassfish.gmbal.ManagedData;
import com.sun.org.glassfish.gmbal.ManagedObject;
import com.sun.org.glassfish.gmbal.ManagedObjectManager;
import com.sun.org.glassfish.gmbal.ManagedObjectManagerFactory;
import java.net.URL;

/**
 * @author Harold Carr
 */
@ManagedObject
@Description("Metro Web Service client")
@AMXMetadata(type="WSClient")
public final class MonitorRootClient extends com.sun.xml.internal.ws.server.MonitorBase {

    private final Stub stub;

    MonitorRootClient(final Stub stub) {
        this.stub = stub;
    }

    /*
    private static final Logger logger = Logger.getLogger(
        com.sun.xml.internal.ws.util.Constants.LoggingDomain + ".client.stub");
    */

    //
    // From WSServiceDelegate
    //

    @ManagedAttribute
    private Container getContainer() { return stub.owner.getContainer(); }

    @ManagedAttribute
    private Map<QName, PortInfo> qnameToPortInfoMap() { return stub.owner.getQNameToPortInfoMap(); }

    @ManagedAttribute
    private QName serviceName() { return stub.owner.getServiceName(); }

    @ManagedAttribute
    private Class serviceClass() { return stub.owner.getServiceClass(); }

    @ManagedAttribute
    private URL wsdlDocumentLocation() { return stub.owner.getWSDLDocumentLocation(); }

    @ManagedAttribute
    private WSDLServiceImpl wsdlService() { return stub.owner.getWsdlService(); }



}
