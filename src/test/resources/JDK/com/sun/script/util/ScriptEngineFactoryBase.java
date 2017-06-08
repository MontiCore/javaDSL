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
import javax.script.*;

/*
 * Abstract super class for factory implementations.
 *
 * @author Mike Grogan
 * @since 1.6
 */
public abstract class ScriptEngineFactoryBase implements ScriptEngineFactory {

    public String getName() {
        return (String)getParameter(ScriptEngine.NAME);
    }

    public String getEngineName() {
        return (String)getParameter(ScriptEngine.ENGINE);
    }

    public String getEngineVersion() {
        return (String)getParameter(ScriptEngine.ENGINE_VERSION);
    }

    public String getLanguageName() {
        return (String)getParameter(ScriptEngine.LANGUAGE);
    }

    public String getLanguageVersion() {
        return (String)getParameter(ScriptEngine.LANGUAGE_VERSION);
    }
}
