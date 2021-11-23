package de.neozo.jblockchain.common;


import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Date;

import static java.time.temporal.ChronoUnit.YEARS;
import static org.bouncycastle.asn1.x509.Extension.basicConstraints;

public final class Signatures {

    /**
     * The keyFactory defines which algorithms are used to generate the private/public keys.
     */
    private static final KeyFactory keyFactory;

    static {
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate a random key pair.
     *
     * @return KeyPair containing private and public key
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());

        return generator.generateKeyPair();

    }

    public static Certificate selfSign(KeyPair keyPair, X500Name dnName) throws OperatorCreationException, CertificateException, IOException {
        var start = Instant.now();
        var end = start.plus(1, YEARS);

        var signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .build(keyPair.getPrivate());

        var holder = new JcaX509v3CertificateBuilder(
                dnName,
                BigInteger.valueOf(start.toEpochMilli()),
                Date.from(start),
                Date.from(end),
                dnName,
                keyPair.getPublic()
        ).addExtension(
                basicConstraints,
                true,
                new BasicConstraints(true)
        ).build(signer);

        return new JcaX509CertificateConverter()
                .setProvider(new BouncyCastleProvider())
                .getCertificate(holder);
    }

    /**
     * Verify if the given signature is valid regarding the data and publicKey.
     *
     * @param data      raw data which was signed
     * @param signature to proof the validity of the sender
     * @param publicKey key to verify the data was signed by owner of corresponding private key
     * @return true if the signature verification succeeds.
     */
    public static boolean verify(byte[] data, byte[] signature, byte[] publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        // construct a public key from raw bytes
        var publicKeyObj = keyFactory.generatePublic(
                new X509EncodedKeySpec(publicKey)
        );

        // do the verification
        var sig = getSignatureObj();
        sig.initVerify(publicKeyObj);
        sig.update(data);
        return sig.verify(signature);
    }

    /**
     * Sign given data with a private key
     *
     * @param data       raw data to sign
     * @param privateKey to use for the signage process
     * @return signature of data which can be verified with corresponding public key
     */
    public static byte[] sign(byte[] data, byte[] privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        // construct a PrivateKey-object from raw bytes
        var privateKeyObj = keyFactory.generatePrivate(
                new PKCS8EncodedKeySpec(privateKey)
        );

        // do the signage
        var sig = getSignatureObj();
        sig.initSign(privateKeyObj);
        sig.update(data);
        return sig.sign();
    }

    private static Signature getSignatureObj() throws NoSuchAlgorithmException {
        return Signature.getInstance("SHA1withRSA");
    }

}
