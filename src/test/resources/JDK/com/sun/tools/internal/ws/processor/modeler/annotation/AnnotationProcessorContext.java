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

package com.sun.tools.internal.ws.processor.modeler.annotation;

import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.tools.internal.ws.processor.model.Model;
import com.sun.tools.internal.ws.processor.model.Operation;
import com.sun.tools.internal.ws.processor.model.Port;
import com.sun.tools.internal.ws.processor.model.Service;
import com.sun.tools.internal.ws.wsdl.document.soap.SOAPUse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author  dkohlert
 */
public class AnnotationProcessorContext {

    private Map<String, SEIContext> seiContextMap;
    private int round = 1;
    private boolean modelCompleted = false;

    /** Creates a new instance of AnnotationProcessorContext */
    public AnnotationProcessorContext() {
        seiContextMap = new HashMap<String, SEIContext>();
    }

    public void addSEIContext(String seiName, SEIContext seiContext) {
        seiContextMap.put(seiName, seiContext);
    }

    public SEIContext getSEIContext(String seiName) {
        SEIContext context =  seiContextMap.get(seiName);
        if (context == null) {
            context = new SEIContext(seiName);
            addSEIContext(seiName, context);
        }
        return context;
    }

    public SEIContext getSEIContext(TypeDeclaration d) {
        SEIContext context = getSEIContext(d.getQualifiedName());
        return context;
    }

    public Collection<SEIContext> getSEIContexts() {
        return seiContextMap.values();
    }

    public int getRound() {
        return round;
    }

    public void incrementRound() {
        round++;
    }

    public static boolean isEncoded(Model model) {
        if (model == null)
            return false;
        for (Service service : model.getServices()) {
            for (Port port : service.getPorts()) {
                for (Operation operation : port.getOperations()) {
                    if (operation.getUse() != null && operation.getUse().equals(SOAPUse.LITERAL))
                        return false;
                }
            }
        }
        return true;
    }

    public void setModelCompleted(boolean modelCompleted) {
        this.modelCompleted = modelCompleted;
    }

    public boolean isModelCompleted() {
        return modelCompleted;
    }

    public static class SEIContext {
        private Map<String, WrapperInfo> reqOperationWrapperMap;
        private Map<String, WrapperInfo> resOperationWrapperMap;
        private Map<String, FaultInfo> exceptionBeanMap;

        private String seiName;
        private String seiImplName;
        private boolean implementsSEI = false;
        private String namespaceURI = null;

        public SEIContext(String seiName) {
            reqOperationWrapperMap = new HashMap<String, WrapperInfo>();
            resOperationWrapperMap = new HashMap<String, WrapperInfo>();
            exceptionBeanMap = new HashMap<String,FaultInfo>();
            this.seiName = seiName;
        }

        public void setImplementsSEI(boolean implementsSEI) {
            this.implementsSEI = implementsSEI;
        }

        public boolean getImplementsSEI() {
            return implementsSEI;
        }

        public void setNamespaceURI(String namespaceURI) {
            this.namespaceURI = namespaceURI;
        }

        public String getNamespaceURI() {
            return namespaceURI;
        }

        public String getSEIImplName() {
            return seiImplName;
        }

        public void setSEIImplName(String implName) {
            seiImplName = implName;
        }

        public void setReqWrapperOperation(MethodDeclaration method, WrapperInfo wrapperInfo) {
            reqOperationWrapperMap.put(methodToString(method), wrapperInfo);
        }

        public WrapperInfo getReqOperationWrapper(MethodDeclaration method) {
            return reqOperationWrapperMap.get(methodToString(method));
        }

        public void setResWrapperOperation(MethodDeclaration method, WrapperInfo wrapperInfo) {
            resOperationWrapperMap.put(methodToString(method), wrapperInfo);
        }

        public WrapperInfo getResOperationWrapper(MethodDeclaration method) {
            return resOperationWrapperMap.get(methodToString(method));
        }

        public String methodToString(MethodDeclaration method) {
            StringBuffer buf = new StringBuffer(method.getSimpleName());
            for (ParameterDeclaration param : method.getParameters())
                buf.append(";"+param.getType().toString());
            return buf.toString();
        }

        public void clearExceptionMap() {
            exceptionBeanMap.clear();
        }

        public void addExceptionBeanEntry(String exception, FaultInfo faultInfo, ModelBuilder builder) {
            exceptionBeanMap.put(exception,faultInfo);
        }

        public FaultInfo getExceptionBeanName(String exception) {
            return exceptionBeanMap.get(exception);
        }
    }
}
