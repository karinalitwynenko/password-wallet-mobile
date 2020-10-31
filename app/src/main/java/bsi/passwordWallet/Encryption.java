package bsi.passwordWallet;

import java.nio.charset.StandardCharsets;
import java.security.BasicPermission;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class Encryption {
    final static String SHA512 = "SHA-512";
    final static String PEPPER = "lnkhvfuuebcdjcuegjsnioahqdprjgjskqclabqmhhlneburfoenupityxlrcjgj";
    final static String HMAC_SHA512 = "HmacSHA512";
    MessageDigest messageDigest;
    MacWrapper mac;
    CipherWrapper cipher;

    static class MacWrapper {
        Mac mac;

        public MacWrapper() { }

        public MacWrapper(Mac mac) {
            this.mac = mac;
        }

        public void init(SecretKeySpec keySpec) throws InvalidKeyException {
            mac.init(keySpec);
        }

        public void update(byte[] input) {
            mac.update(input);
        }

        public byte[] doFinal(byte[] input) {
            return mac.doFinal(input);
        }
    }

    static class CipherWrapper {
        Cipher cipher;

        public CipherWrapper() { }

        public CipherWrapper(Cipher cipher) {
            this.cipher = cipher;
        }

        public void init(int mode, SecretKeySpec secretKeySpec, IvParameterSpec ivParam) throws InvalidAlgorithmParameterException, InvalidKeyException {
            cipher.init(mode, secretKeySpec, ivParam);
        }

        public byte[] doFinal(byte[] input) throws BadPaddingException, IllegalBlockSizeException {
            return cipher.doFinal(input);
        }
    }

    void setMessageDigest(MessageDigest messageDigest) {
        this.messageDigest = messageDigest;
    }

    void setMac(MacWrapper macWrapper) {
        this.mac = macWrapper;
    }

    void setCipher(CipherWrapper cipherWrapper) {
        this.cipher = cipherWrapper;
    }

    String calculateSHA512(String input, String salt, String pepper) {
        // add salt
        messageDigest.update(salt.getBytes());
        // add pepper
        messageDigest.update(pepper.getBytes());

        // calculate message digest of the input string
        String hash = new String(messageDigest.digest(input.getBytes()), StandardCharsets.UTF_8);

        return hash;
    }

    String calculateHMAC(String input, SecretKeySpec keySpec, String pepper) {
        String hash = "";
        try {
            mac.init(keySpec);

            // add pepper
            mac.update(pepper.getBytes());

            // calculate message digest of the input string
            hash = new String(
                    mac.doFinal(input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8
            );

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return hash;
    }

    String encryptAES128(String input, SecretKeySpec secretKeySpec, IvParameterSpec ivParam) {
        byte[] cipherBytes = new byte[0];
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParam);
            // encrypt input string
            cipherBytes = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            e.printStackTrace();
        }

        // convert to string using Base64 encoding scheme
        return Base64.getEncoder().encodeToString(cipherBytes);
    }

    String decryptAES128(String input, SecretKeySpec secretKeySpec, IvParameterSpec ivParam) {
        byte[] decodedInput = new byte[0];
        try {
            // decode a Base64 encoded string into byte array
            byte[] inputBytes = Base64.getDecoder().decode(input);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParam);
            // decrypt input string
            decodedInput = cipher.doFinal(inputBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String(decodedInput);
    }

    /**
     * calculate MD5 hash
     * @param input string to digest
     * @return hash in a byte array
     */
    byte[] calculateMD5(String input) {
        return messageDigest.digest(input.getBytes());
    }

    /**
     * generate 64 bytes long random salt
     * @return UTF-8 representation of generated salt
     */
    static String generateSalt64() {
        byte[] salt = new byte[64];
        new SecureRandom().nextBytes(salt);
        return new String(salt, StandardCharsets.UTF_8);
    }

    /**
     * generate random initialization vector of 16 bytes
     * @return iv as a byte array
     */
    public static byte[] randomIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

}
