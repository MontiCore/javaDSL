/*
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * This class generates a AES key.
 *
 * @author Valerie Peng
 *
 */

public final class AESKeyGenerator extends KeyGeneratorSpi {

    private SecureRandom random = null;
    private int keySize = 16; // default keysize (in number of bytes)

    /**
     * Empty constructor.
     */
    public AESKeyGenerator() {
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
        throws InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException
                ("AES key generation does not take any parameters");
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
        if (((keysize % 8) != 0) ||
            (!AESCrypt.isKeySizeValid(keysize/8))) {
            throw new InvalidParameterException
                ("Wrong keysize: must be equal to 128, 192 or 256");
        }
        this.keySize = keysize/8;
        this.engineInit(random);
    }

    /**
     * Generates the AES key.
     *
     * @return the new AES key
     */
    protected SecretKey engineGenerateKey() {
        SecretKeySpec aesKey = null;

        if (this.random == null) {
            this.random = SunJCE.RANDOM;
        }

        byte[] keyBytes = new byte[keySize];
        this.random.nextBytes(keyBytes);
        aesKey = new SecretKeySpec(keyBytes, "AES");
        return aesKey;
    }
}
