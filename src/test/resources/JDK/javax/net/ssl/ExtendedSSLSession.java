/*
 * Copyright (c) 2010, 2012, Oracle and/or its affiliates. All rights reserved.
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

package javax.net.ssl;

/**
 * Extends the <code>SSLSession</code> interface to support additional
 * session attributes.
 *
 * @since 1.7
 */
public abstract class ExtendedSSLSession implements SSLSession {
    /**
     * Obtains an array of supported signature algorithms that the local side
     * is willing to use.
     * <p>
     * Note: this method is used to indicate to the peer which signature
     * algorithms may be used for digital signatures in TLS 1.2. It is
     * not meaningful for TLS versions prior to 1.2.
     * <p>
     * The signature algorithm name must be a standard Java Security
     * name (such as "SHA1withRSA", "SHA256withECDSA", and so on).
     * See Appendix A in the <a href=
     * "{@docRoot}/../technotes/guides/security/crypto/CryptoSpec.html#AppA">
     * Java Cryptography Architecture API Specification &amp; Reference </a>
     * for information about standard algorithm names.
     * <p>
     * Note: the local supported signature algorithms should conform to
     * the algorithm constraints specified by
     * {@link SSLParameters#getAlgorithmConstraints getAlgorithmConstraints()}
     * method in <code>SSLParameters</code>.
     *
     * @return An array of supported signature algorithms, in descending
     *     order of preference.  The return value is an empty array if
     *     no signature algorithm is supported.
     *
     * @see SSLParameters#getAlgorithmConstraints
     */
    public abstract String[] getLocalSupportedSignatureAlgorithms();

    /**
     * Obtains an array of supported signature algorithms that the peer is
     * able to use.
     * <p>
     * Note: this method is used to indicate to the local side which signature
     * algorithms may be used for digital signatures in TLS 1.2. It is
     * not meaningful for TLS versions prior to 1.2.
     * <p>
     * The signature algorithm name must be a standard Java Security
     * name (such as "SHA1withRSA", "SHA256withECDSA", and so on).
     * See Appendix A in the <a href=
     * "{@docRoot}/../technotes/guides/security/crypto/CryptoSpec.html#AppA">
     * Java Cryptography Architecture API Specification &amp; Reference </a>
     * for information about standard algorithm names.
     *
     * @return An array of supported signature algorithms, in descending
     *     order of preference.  The return value is an empty array if
     *     the peer has not sent the supported signature algorithms.
     *
     * @see X509KeyManager
     * @see X509ExtendedKeyManager
     */
    public abstract String[] getPeerSupportedSignatureAlgorithms();
}
