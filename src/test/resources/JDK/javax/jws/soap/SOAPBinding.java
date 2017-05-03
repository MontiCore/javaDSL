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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Specifies the mapping of the Web Service onto the SOAP message protocol.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD})
public @interface SOAPBinding {

    /**
     * The SOAP binding style
     */
    public enum Style {
      DOCUMENT,
      RPC
    };

    /**
     * The SOAP binding use
     */
    public enum Use {
      LITERAL,
      ENCODED
    };

    /**
     * The style of mapping parameters onto SOAP messages
     */
    public enum ParameterStyle {
      BARE,
      WRAPPED
    }

    /**
     * Defines the encoding style for messages send to and from the Web Service.
     */
    Style style() default Style.DOCUMENT;

    /**
     * Defines the formatting style for messages sent to and from the Web Service.
     */
    Use use() default Use.LITERAL;

    /**
     * Determines whether method parameters represent the entire message body, or whether the parameters are elements
     * wrapped inside a top-level element named after the operation
     */
    ParameterStyle parameterStyle() default ParameterStyle.WRAPPED;
}
