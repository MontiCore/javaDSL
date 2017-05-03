/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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

package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

/**
 * Specifies the set of parameters required by a {@link
 * javax.crypto.Cipher} using the Galois/Counter Mode (GCM) mode.
 * <p>
 * Simple block cipher modes (such as CBC) generally require only an
 * initialization vector (such as {@code IvParameterSpec}),
 * but GCM needs these parameters:
 * <ul>
 * <li>{@code IV}: Initialization Vector (IV) </li>
 * <li>{@code tLen}: length (in bits) of authentication tag T</li>
 * </ul>
 * <p>
 * In addition to the parameters described here, other GCM inputs/output
 * (Additional Authenticated Data (AAD), Keys, block ciphers,
 * plain/ciphertext and authentication tags) are handled in the {@code
 * Cipher} class.
  <p>
 * Please see <a href="http://www.ietf.org/rfc/rfc5116.txt"> RFC 5116
 * </a> for more information on the Authenticated Encryption with
 * Associated Data (AEAD) algorithm, and <a href=
 * "http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">
 * NIST Special Publication 800-38D</a>, "NIST Recommendation for Block
 * Cipher Modes of Operation:  Galois/Counter Mode (GCM) and GMAC."
 * <p>
 * The GCM specification states that {@code tLen} may only have the
 * values {128, 120, 112, 104, 96}, or {64, 32} for certain
 * applications.  Other values can be specified for this class, but not
 * all CSP implementations will support them.
 *
 * @see javax.crypto.Cipher
 *
 * @since 1.7
 */
public class GCMParameterSpec implements AlgorithmParameterSpec {

    // Initialization Vector.  Could use IvParameterSpec, but that
    // would add extra copies.
    private byte[] iv;

    // Required Tag length (in bits).
    private int tLen;

    /**
     * Constructs a GCMParameterSpec using the specified authentication
     * tag bit-length and IV buffer.
     *
     * @param tLen the authentication tag length (in bits)
     * @param src the IV source buffer.  The contents of the buffer are
     * copied to protect against subsequent modification.
     *
     * @throws IllegalArgumentException if {@code tLen} is negative,
     * or {@code src} is null.
     */
    public GCMParameterSpec(int tLen, byte[] src) {
        if (src == null) {
            throw new IllegalArgumentException("src array is null");
        }

        init(tLen, src, 0, src.length);
    }

    /**
     * Constructs a GCMParameterSpec object using the specified
     * authentication tag bit-length and a subset of the specified
     * buffer as the IV.
     *
     * @param tLen the authentication tag length (in bits)
     * @param src the IV source buffer.  The contents of the
     * buffer are copied to protect against subsequent modification.
     * @param offset the offset in {@code src} where the IV starts
     * @param len the number of IV bytes
     *
     * @throws IllegalArgumentException if {@code tLen} is negative,
     * {@code src} is null, {@code len} or {@code offset} is negative,
     * or the sum of {@code offset} and {@code len} is greater than the
     * length of the {@code src} byte array.
     */
    public GCMParameterSpec(int tLen, byte[] src, int offset, int len) {
        init(tLen, src, offset, len);
    }

    /*
     * Check input parameters.
     */
    private void init(int tLen, byte[] src, int offset, int len) {
        if (tLen < 0) {
            throw new IllegalArgumentException(
                "Length argument is negative");
        }
        this.tLen = tLen;

        // Input sanity check
        if ((src == null) ||(len < 0) || (offset < 0)
                || ((len + offset) > src.length)) {
            throw new IllegalArgumentException("Invalid buffer arguments");
        }

        iv = new byte[len];
        System.arraycopy(src, offset, iv, 0, len);
    }

    /**
     * Returns the authentication tag length.
     *
     * @return the authentication tag length (in bits)
     */
    public int getTLen() {
        return tLen;
    }

    /**
     * Returns the Initialization Vector (IV).
     *
     * @return the IV.  Creates a new array each time this method
     * is called.
     */
    public byte[] getIV() {
        return iv.clone();
    }
}
