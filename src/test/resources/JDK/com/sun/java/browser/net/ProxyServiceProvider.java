/*
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.java.browser.net;

import java.net.URL;

/**
 *
 * @author  Zhengyu Gu
 */
public interface ProxyServiceProvider {
    public ProxyInfo[] getProxyInfo(URL url);
}
