/*
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
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

package javax.crypto.interfaces;

import javax.crypto.spec.DHParameterSpec;

/**
 * The interface to a Diffie-Hellman key.
 *
 * @author Jan Luehe
 *
 * @see javax.crypto.spec.DHParameterSpec
 * @see DHPublicKey
 * @see DHPrivateKey
 * @since 1.4
 */
public interface DHKey {

    /**
     * Returns the key parameters.
     *
     * @return the key parameters
     */
    DHParameterSpec getParams();
}
