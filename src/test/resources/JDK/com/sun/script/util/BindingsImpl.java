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
import java.util.*;
import javax.script.Bindings;

/*
 * Abstract super class for Bindings implementations. Handles
 * global and local scopes.
 *
 * @author Mike Grogan
 * @since 1.6
 */
public abstract class BindingsImpl extends BindingsBase {

    //get method delegates to global if key is not defined in
    //base class or local scope
    protected Bindings global = null;

    //get delegates to local scope
    protected Bindings local = null;

    public void setGlobal(Bindings n) {
        global = n;
    }

    public void setLocal(Bindings n) {
        local = n;
    }

    public  Set<Map.Entry<String, Object>> entrySet() {
        return new BindingsEntrySet(this);
    }

    public Object get(Object key) {
        checkKey(key);

        Object ret  = null;
        if ((local != null) && (null != (ret = local.get(key)))) {
            return ret;
        }

        ret = getImpl((String)key);

        if (ret != null) {
            return ret;
        } else if (global != null) {
            return global.get(key);
        } else {
            return null;
        }
    }

    public Object remove(Object key) {
        checkKey(key);
        Object ret = get(key);
        if (ret != null) {
            removeImpl((String)key);
        }
        return ret;
    }
}
