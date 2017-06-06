/*
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.org.apache.xalan.internal;

import com.sun.org.apache.xerces.internal.impl.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Commonly used constants.
 *
 * @author Huizhe Wang, Oracle
 *
 * @version $Id: Constants.java,v 1.14 2011-06-07 04:39:40 joehw Exp $
 */
public final class XalanConstants {

    //
    // Constants
    //
    // Oracle Feature:
        /**
         * <p>Use Service Mechanism</p>
         *
         * <ul>
         *   <li>
         *     <code>true</code> instructs the implementation to use service mechanism to find implementation.
         *     This is the default behavior.
         *   </li>
         *   <li>
         *     <code>false</code> instructs the implementation to skip service mechanism and use the default implementation.
         *   </li>
         * </ul>
         */
    public static final String ORACLE_FEATURE_SERVICE_MECHANISM = "http://www.oracle.com/feature/use-service-mechanism";

} // class Constants
