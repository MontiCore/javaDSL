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

import java.math.BigInteger;

/**
 * The interface to a Diffie-Hellman public key.
 *
 * @author Jan Luehe
 *
 * @see DHKey
 * @see DHPrivateKey
 * @since 1.4
 */
public interface DHPublicKey extends DHKey, java.security.PublicKey {

    /**
     * The class fingerprint that is set to indicate serialization
     * compatibility since J2SE 1.4.
     */
    static final long serialVersionUID = -6628103563352519193L;

    /**
     * Returns the public value, <code>y</code>.
     *
     * @return the public value, <code>y</code>
     */
    BigInteger getY();
}
