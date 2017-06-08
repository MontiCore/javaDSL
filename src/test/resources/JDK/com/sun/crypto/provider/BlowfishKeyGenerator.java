/*
 * Copyright (c) 1998, 2009, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.crypto.provider;

import java.security.SecureRandom;
import java.security.InvalidParameterException;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class generates a secret key for use with the Blowfish algorithm.
 *
 * @author Jan Luehe
 *
 */

public final class BlowfishKeyGenerator extends KeyGeneratorSpi {

    private SecureRandom random = null;
    private int keysize = 16; // default keysize (in number of bytes)

    /**
     * Empty constructor
     */
    public BlowfishKeyGenerator() {
    }

    /**
     * Initializes this key generator.
     *
     * @param random the source of randomness for this generator
     */
    protected void engineInit(SecureRandom random) {
        this.random = random;
    }

    /**
     * Initializes this key generator with the specified parameter
     * set and a user-provided source of randomness.
     *
     * @param params the key generation parameters
     * @param random the source of randomness for this key generator
     *
     * @exception InvalidAlgorithmParameterException if <code>params</code> is
     * inappropriate for this key generator
     */
    protected void engineInit(AlgorithmParameterSpec params,
                              SecureRandom random)
        throws InvalidAlgorithmParameterException
    {
        throw new InvalidAlgorithmParameterException
            ("Blowfish key generation does not take any parameters");
    }

    /**
     * Initializes this key generator for a certain keysize, using the given
     * source of randomness.
     *
     * @param keysize the keysize. This is an algorithm-specific
     * metric specified in number of bits.
     * @param random the source of randomness for this key generator
     */
    protected void engineInit(int keysize, SecureRandom random) {
        if (((keysize % 8) != 0) || (keysize < 32) || (keysize > 448)) {
            throw new InvalidParameterException("Keysize must be "
                                                + "multiple of 8, and can "
                                                + "only range from 32 to 448 "
                                                + "(inclusive)");
        }
        this.keysize = keysize / 8;
        this.engineInit(random);
    }

    /**
     * Generates a Blowfish key.
     *
     * @return the new Blowfish key
     */
    protected SecretKey engineGenerateKey() {
        if (this.random == null) {
            this.random = SunJCE.RANDOM;
        }

        byte[] keyBytes = new byte[this.keysize];
        this.random.nextBytes(keyBytes);

        return new SecretKeySpec(keyBytes, "Blowfish");
    }
}
