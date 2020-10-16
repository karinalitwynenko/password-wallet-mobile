package bsi.passwordWallet;

import android.os.Build;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import androidx.annotation.RequiresApi;

class Encryption {
    final static String SHA256 = "SHA-256";
    final static String HMAC = "HMAC";
    private static String PEPPER = "Piperaceae";

    static String calculateSHA265(String input, String salt, String pepper) {
        String hash = "";
        try {
            // get an instance of SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // add salt
            md.update(salt.getBytes());
            // calculate message digest of the input string - returns byte array
            hash = new String(md.digest(input.getBytes()), StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return hash;
    }

    static String calculateHMAC(String input, String key, String pepper) {
        String hash = "";
        try {
            final byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
            Mac sha512Hmac = Mac.getInstance("HMAC_SHA512");
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, "HMAC_SHA512");
            sha512Hmac.init(keySpec);
            hash = sha512Hmac.doFinal(input.getBytes(StandardCharsets.UTF_8)).toString();
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

            cipherBytes = cipher.doFinal(input.getBytes("UTF-8"));

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

    /** calculates MD5 hash */
    static byte[] encryptMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt.toString();
    }

    /** returns random initialization vector of 16 bytes */
    public static byte[] randomIV(){
        byte[] iv = new byte[16];
        new Random().nextBytes(iv);
        return iv;
    }
}
