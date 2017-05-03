/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.script.util;

import javax.script.Bindings;
import java.util.Map;
import java.util.AbstractMap;

/**
 * Abstract super class for Bindings implementations
 *
 * @author Mike Grogan
 * @since 1.6
 */
public abstract class BindingsBase extends AbstractMap<String, Object>
        implements Bindings {

    //AbstractMap methods
    public Object get(Object name) {
        checkKey(name);
        return getImpl((String)name);
    }

    public Object remove(Object key) {
        checkKey(key);
        return removeImpl((String)key);
    }

    public Object put(String key, Object value) {
        checkKey(key);
        return putImpl(key, value);
    }

    public void putAll(Map<? extends String, ? extends Object> toMerge) {
        for (Map.Entry<? extends String, ? extends Object> entry : toMerge.entrySet()) {
            String key = entry.getKey();
            checkKey(key);
            putImpl(entry.getKey(), entry.getValue());
        }
    }

    //BindingsBase methods
    public abstract Object putImpl(String name, Object value);
    public abstract Object getImpl(String name);
    public abstract Object removeImpl(String name);
    public abstract String[] getNames();

    protected void checkKey(Object key) {
        if (key == null) {
            throw new NullPointerException("key can not be null");
        }
        if (!(key instanceof String)) {
            throw new ClassCastException("key should be String");
        }
        if (key.equals("")) {
            throw new IllegalArgumentException("key can not be empty");
        }
    }
}
