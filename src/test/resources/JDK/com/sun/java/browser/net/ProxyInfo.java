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

/**
 *
 * @author  Zhengyu Gu
 */
public interface ProxyInfo {
    public String   getHost();
    public int      getPort();
    public boolean  isSocks();
}
