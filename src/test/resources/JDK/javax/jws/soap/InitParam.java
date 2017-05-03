/*
 * Copyright (c) 2004, 2010, Oracle and/or its affiliates. All rights reserved.
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

package javax.jws.soap;

/**
 * An initialization parameter
 *
 * @deprecated  As of JSR-181 2.0 with no replacement.
 */
@Deprecated public @interface InitParam {

    /**
     * Name of the initialization parameter
     */
    String name();

    /**
     * Value of the initialization parameter
     */
    String value();
};
