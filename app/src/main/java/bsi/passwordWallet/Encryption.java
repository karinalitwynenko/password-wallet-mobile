package bsi.passwordWallet;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
    public static String SHA512 = "SHA-512";
    public static String HMAC_SHA512 = "HmacSHA512";
    public static String PEPPER = "lnkhvfuuebcdjcuegjsnioahqdprjgjskqclabqmhhlneburfoenupityxlrcjgj";

    public String calculateSHA512(String input, String salt) {
        byte[] hash = {};
        try {
            // get an instance of SHA-256
            MessageDigest md = MessageDigest.getInstance(SHA512);
            // add salt
            md.update(salt.getBytes());
            // add pepper
            md.update(PEPPER.getBytes());
            // calculate message digest of the input string
            hash = md.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return Base64.getEncoder().encodeToString(hash);
    }

    public String calculateHMAC(String input, String salt) {
        byte[] hash = {};
        // get bytes from salt string
        final byte[] byteKey = salt.getBytes(StandardCharsets.UTF_8);

        // create a secret key
        SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);

        try {
            // get an instance of HmacSHA512
            Mac mac = Mac.getInstance(HMAC_SHA512);
            mac.init(keySpec);
            // add salt
            mac.update(salt.getBytes());
            // add pepper
            mac.update(PEPPER.getBytes());
            // calculate message digest of the input string
            hash = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return Base64.getEncoder().encodeToString(hash);
    }

    public String encryptAES128(String input, byte[] key, byte[] iv) {
        byte[] cipherBytes = {};
        try {
            // initialization vector
            IvParameterSpec ivParam = new IvParameterSpec(iv);
            // create secret key
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            // get an instance of AES
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParam);
            // encrypt input string
            cipherBytes = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            e.printStackTrace();
        }

        // convert to string using Base64 encoding scheme
        return Base64.getEncoder().encodeToString(cipherBytes);
    }

    public String decryptAES128(String input, byte[] key, byte[] iv) {
        byte[] decodedInput = {};
        try {
            // decode a Base64 encoded string into byte array
            byte[] inputBytes = Base64.getDecoder().decode(input);
            // initialization vector
            IvParameterSpec ivParam = new IvParameterSpec(iv);
            // create secret key
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            // get an instance of AES
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParam);
            // decrypt input string
            decodedInput = cipher.doFinal(inputBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String(decodedInput);
    }

    /**
     * calculates MD5 hash
     * @param input string to digest
     * @return hash in a byte array
     */
    public byte[] calculateMD5(String input) {
        byte[] hash = {};
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hash;
    }

    /**
     * generates 64 chars long random salt
     * @return UTF-8 representation of generated salt
     */
    public String generateSalt64() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[64];
        random.nextBytes(salt);
        return new String(salt, StandardCharsets.UTF_8);
    }

    /**
     * generates random initialization vector of 16 bytes
     * @return iv as a byte array
     */
    public byte[] randomIV(){
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

}