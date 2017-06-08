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

package com.sun.crypto.provider;

import java.security.KeyRep;
import java.security.InvalidKeyException;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESedeKeySpec;

/**
 * This class represents a DES-EDE key.
 *
 * @author Jan Luehe
 *
 */

final class DESedeKey implements SecretKey {

    static final long serialVersionUID = 2463986565756745178L;

    private byte[] key;

    /**
     * Creates a DES-EDE key from a given key.
     *
     * @param key the given key
     *
     * @exception InvalidKeyException if the given key has a wrong size
     */
    DESedeKey(byte[] key) throws InvalidKeyException {
        this(key, 0);
    }

    /**
     * Uses the first 24 bytes in <code>key</code>, beginning at
     * <code>offset</code>, as the DES-EDE key
     *
     * @param key the buffer with the DES-EDE key
     * @param offset the offset in <code>key</code>, where the DES-EDE key
     * starts
     *
     * @exception InvalidKeyException if the given key has a wrong size
     */
    DESedeKey(byte[] key, int offset) throws InvalidKeyException {

        if (key==null || ((key.length-offset)<DESedeKeySpec.DES_EDE_KEY_LEN)) {
            throw new InvalidKeyException("Wrong key size");
        }
        this.key = new byte[DESedeKeySpec.DES_EDE_KEY_LEN];
        System.arraycopy(key, offset, this.key, 0,
                         DESedeKeySpec.DES_EDE_KEY_LEN);
        DESKeyGenerator.setParityBit(this.key, 0);
        DESKeyGenerator.setParityBit(this.key, 8);
        DESKeyGenerator.setParityBit(this.key, 16);
    }

    public byte[] getEncoded() {
        return (byte[])this.key.clone();
    }

    public String getAlgorithm() {
        return "DESede";
    }

    public String getFormat() {
        return "RAW";
    }

    /**
     * Calculates a hash code value for the object.
     * Objects that are equal will also have the same hashcode.
     */
    public int hashCode() {
        int retval = 0;
        for (int i = 1; i < this.key.length; i++) {
            retval += this.key[i] * i;
        }
        return(retval ^= "desede".hashCode());
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof SecretKey))
            return false;

        String thatAlg = ((SecretKey)obj).getAlgorithm();
        if (!(thatAlg.equalsIgnoreCase("DESede"))
            && !(thatAlg.equalsIgnoreCase("TripleDES")))
            return false;

        byte[] thatKey = ((SecretKey)obj).getEncoded();
        boolean ret = java.util.Arrays.equals(this.key, thatKey);
        java.util.Arrays.fill(thatKey, (byte)0x00);
        return ret;
    }

    /**
     * readObject is called to restore the state of this key from
     * a stream.
     */
    private void readObject(java.io.ObjectInputStream s)
         throws java.io.IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        key = (byte[])key.clone();
    }

    /**
     * Replace the DESede key to be serialized.
     *
     * @return the standard KeyRep object to be serialized
     *
     * @throws java.io.ObjectStreamException if a new object representing
     * this DESede key could not be created
     */
    private Object writeReplace() throws java.io.ObjectStreamException {
        return new KeyRep(KeyRep.Type.SECRET,
                        getAlgorithm(),
                        getFormat(),
                        getEncoded());
    }

    /**
     * Ensures that the bytes of this key are
     * set to zero when there are no more references to it.
     */
    protected void finalize() throws Throwable {
        try {
            if (this.key != null) {
                java.util.Arrays.fill(this.key, (byte)0x00);
                this.key = null;
            }
        } finally {
            super.finalize();
        }
    }
}
