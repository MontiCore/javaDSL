/*
 * Copyright (c) 1999, 2009, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jndi.ldap;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;

final class VersionHelper12 extends VersionHelper {

    // System property to control whether classes may be loaded from an
    // arbitrary URL code base.
    private static final String TRUST_URL_CODEBASE_PROPERTY =
        "com.sun.jndi.ldap.object.trustURLCodebase";

    // Determine whether classes may be loaded from an arbitrary URL code base.
    private static final String trustURLCodebase =
        AccessController.doPrivileged(
            new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(TRUST_URL_CODEBASE_PROPERTY,
                            "false");
                }
            }
        );

    VersionHelper12() {} // Disallow external from creating one of these.

    ClassLoader getURLClassLoader(String[] url)
        throws MalformedURLException {
            ClassLoader parent = getContextClassLoader();
            /*
             * Classes may only be loaded from an arbitrary URL code base when
             * the system property com.sun.jndi.ldap.object.trustURLCodebase
             * has been set to "true".
             */
            if (url != null && "true".equalsIgnoreCase(trustURLCodebase)) {
                return URLClassLoader.newInstance(getUrlArray(url), parent);
            } else {
                return parent;
            }
    }

    Class loadClass(String className) throws ClassNotFoundException {
        ClassLoader cl = getContextClassLoader();
        return Class.forName(className, true, cl);
    }

    private ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            }
        );
    }

    Thread createThread(final Runnable r) {
        return (Thread) AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return new Thread(r);
                }
            }
        );
    }
}
