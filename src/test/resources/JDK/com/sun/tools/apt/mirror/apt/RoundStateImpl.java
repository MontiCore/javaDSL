/*
 * Copyright (c) 2004, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.apt.mirror.apt;

import com.sun.mirror.apt.RoundState;
import java.util.Map;

@SuppressWarnings("deprecation")
public class RoundStateImpl implements RoundState {
    private final boolean finalRound;
    private final boolean errorRaised;
    private final boolean sourceFilesCreated;
    private final boolean classFilesCreated;

    public RoundStateImpl(boolean errorRaised,
                          boolean sourceFilesCreated,
                          boolean classFilesCreated,
                          Map<String,String> options) {
        /*
         * In the default mode of operation, this round is the final
         * round if an error was raised OR there were no new source
         * files generated.  If classes are being treated as
         * declarations, this is the final round if an error was
         * raised OR neither new source files nor new class files were
         * generated.
         */
        this.finalRound =
            errorRaised ||
            (!sourceFilesCreated &&
            !(classFilesCreated && options.keySet().contains("-XclassesAsDecls")) );
        this.errorRaised = errorRaised;
        this.sourceFilesCreated = sourceFilesCreated;
        this.classFilesCreated = classFilesCreated;
    }

    public boolean finalRound() {
        return finalRound;
    }

    public boolean errorRaised() {
        return errorRaised;
    }

    public boolean sourceFilesCreated() {
        return sourceFilesCreated;
    }

    public boolean classFilesCreated() {
        return classFilesCreated;
    }

    public String toString() {
        return
            "[final round: " +  finalRound +
            ", error raised: " +  errorRaised +
            ", source files created: " + sourceFilesCreated +
            ", class files created: " + classFilesCreated + "]";
    }
}
