package com.mastercard.developer.encryption;

import com.jayway.jsonpath.JsonPath;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import static com.mastercard.developer.utils.EncodingUtils.encodeBytes;
import static com.mastercard.developer.utils.StringUtils.isNullOrEmpty;

abstract class EncryptionConfigBuilder {

    protected Certificate encryptionCertificate;
    protected String encryptionKeyFingerprint;
    protected PrivateKey decryptionKey;
    protected Map<String, String> encryptionPaths = new HashMap<>();
    protected Map<String, String> decryptionPaths = new HashMap<>();
    protected String encryptedValueFieldName;

    void computeEncryptionKeyFingerprintWhenNeeded() throws EncryptionException {
        try {
            if (encryptionCertificate == null || !isNullOrEmpty(encryptionKeyFingerprint)) {
                // No encryption certificate set or key fingerprint already provided
                return;
            }
            byte[] keyFingerprintBytes = sha256digestBytes(encryptionCertificate.getPublicKey().getEncoded());
            encryptionKeyFingerprint = encodeBytes(keyFingerprintBytes, FieldLevelEncryptionConfig.FieldValueEncoding.HEX);
        } catch (Exception e) {
            throw new EncryptionException("Failed to compute encryption key fingerprint!", e);
        }
    }

    static byte[] sha256digestBytes(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(bytes);
        return messageDigest.digest();
    }

    void checkJsonPathParameterValues() {
        for (Map.Entry<String, String> entry : decryptionPaths.entrySet()) {
            if (!JsonPath.isPathDefinite(entry.getKey()) || !JsonPath.isPathDefinite(entry.getValue())) {
                throw new IllegalArgumentException("JSON paths for decryption must point to a single item!");
            }
        }

        for (Map.Entry<String, String> entry : encryptionPaths.entrySet()) {
            if (!JsonPath.isPathDefinite(entry.getKey()) || !JsonPath.isPathDefinite(entry.getValue())) {
                throw new IllegalArgumentException("JSON paths for encryption must point to a single item!");
            }
        }
    }
}
