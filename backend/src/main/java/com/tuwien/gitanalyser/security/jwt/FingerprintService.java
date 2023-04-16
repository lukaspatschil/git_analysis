package com.tuwien.gitanalyser.security.jwt;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Service
public class FingerprintService {

    private static final int SIZE_OF_BYTES_ARRAY = 32;

    private static final int LEFT_LIMIT = 97;
    private static final int RIGHT_LIMIT = 122;
    private static final int MIN_LENGTH = 64;
    private static final int HEX_RADIX = 16;

    public static String toHexString(final byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(HEX_RADIX));

        // Pad with leading zeros
        while (hexString.length() < MIN_LENGTH) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public FingerprintPair createFingerprint() {
        String fingerPrint = createRandomsBytesString();

        return new FingerprintPair(fingerPrint, sha256(fingerPrint));
    }

    private String createRandomsBytesString() {
        Random random = new Random();

        return random.ints(LEFT_LIMIT, RIGHT_LIMIT + 1).limit(SIZE_OF_BYTES_ARRAY)
                     .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    public String sha256(final String fingerprint) {

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] hash = md.digest(fingerprint.getBytes(StandardCharsets.UTF_8));
        return toHexString(hash);
    }
}
