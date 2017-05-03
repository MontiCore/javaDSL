/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.javadoc;

import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.util.*;

/**
 *  Javadoc's own todo queue doesn't queue its inputs, as javadoc
 *  doesn't perform attribution of method bodies or semantic checking.
 *  @author Neal Gafter
 */
public class JavadocTodo extends Todo {
    public static void preRegister(Context context) {
        context.put(todoKey, new Context.Factory<Todo>() {
               public Todo make(Context c) {
                   return new JavadocTodo(c);
               }
        });
    }

    protected JavadocTodo(Context context) {
        super(context);
    }

    @Override
    public void append(Env<AttrContext> e) {
        // do nothing; Javadoc doesn't perform attribution.
    }

    @Override
    public boolean offer(Env<AttrContext> e) {
        return false;
    }
}
