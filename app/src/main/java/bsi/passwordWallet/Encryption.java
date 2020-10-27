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

class Encryption {
    final static String SHA512 = "SHA-512";
    final static String HMAC = "HMAC";
    final static String PEPPER = "lnkhvfuuebcdjcuegjsnioahqdprjgjskqclabqmhhlneburfoenupityxlrcjgj";

    static String calculate512(String input, String salt, String pepper) {
        String hash = "";
        try {
            // get an instance of SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // add salt
            md.update(salt.getBytes());
            // add pepper
            md.update(pepper.getBytes());

            // calculate message digest of the input string 
            hash = new String(md.digest(input.getBytes()), StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return hash;
    }

    static String calculateHMAC(String input, String salt, String pepper) {
        String hash = "";
        try {
            final byte[] byteKey = salt.getBytes(StandardCharsets.UTF_8);
            Mac sha512Hmac = Mac.getInstance("HMAC_SHA512");

            SecretKeySpec keySpec = new SecretKeySpec(byteKey, "HMAC_SHA512");
            sha512Hmac.init(keySpec);

            // add salt
            sha512Hmac.update(salt.getBytes());
            // add pepper
            sha512Hmac.update(pepper.getBytes());

            hash = new String(
                    sha512Hmac.doFinal(input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8
            );

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hash;
    }

    static String encryptAES128(String input, byte[] key, byte[] iv) {
        byte[] cipherBytes = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
            IvParameterSpec ivParam = new IvParameterSpec(iv);

            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParam);

            cipherBytes = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Base64.getEncoder().encodeToString(cipherBytes);
    }

    static String decryptAES128(String input, byte[] key, byte[] iv) {
        byte[] decodedInput = new byte[0];
        try {
            byte[] inputBytes = Base64.getDecoder().decode(input);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
            IvParameterSpec ivParam = new IvParameterSpec(iv);

            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParam);
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
    static byte[] calculateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * generates 64 bytes long random salt
     * @return UTF-8 representation of generated salt
     */
    static String generateSalt64() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[64];
        random.nextBytes(salt);
        return new String(salt, StandardCharsets.UTF_8);
    }

    /**
     * generates random initialization vector of 16 bytes
     * @return iv as a byte array
     */
    public static byte[] randomIV(){
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
