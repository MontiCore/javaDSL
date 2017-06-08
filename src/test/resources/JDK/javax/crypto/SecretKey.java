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

package javax.crypto;

/**
 * A secret (symmetric) key.
 *
 * <p>This interface contains no methods or constants.
 * Its only purpose is to group (and provide type safety for) secret keys.
 *
 * <p>Provider implementations of this interface must overwrite the
 * <code>equals</code> and <code>hashCode</code> methods inherited from
 * <code>java.lang.Object</code>, so that secret keys are compared based on
 * their underlying key material and not based on reference.
 *
 * <p>Keys that implement this interface return the string <code>RAW</code>
 * as their encoding format (see <code>getFormat</code>), and return the
 * raw key bytes as the result of a <code>getEncoded</code> method call. (The
 * <code>getFormat</code> and <code>getEncoded</code> methods are inherited
 * from the <code>java.security.Key</code> parent interface.)
 *
 * @author Jan Luehe
 *
 * @see SecretKeyFactory
 * @see Cipher
 * @since 1.4
 */

public interface SecretKey extends java.security.Key {
    /**
     * The class fingerprint that is set to indicate serialization
     * compatibility since J2SE 1.4.
     */
    static final long serialVersionUID = -4795878709595146952L;
}
