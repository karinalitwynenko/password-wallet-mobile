package bsi.passwordWallet;

import android.os.Build;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
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
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import androidx.annotation.RequiresApi;

class Encryption {
    final static String SHA256 = "SHA-256";
    final static String HMAC = "HMAC";
    private static String PEPPER = "Piperaceae";

    static String encryptSHA265(String input, String salt, String pepper) {
        try {
            // get an instance of SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // add salt
            md.update(salt.getBytes());

            // calculate message digest of the input string - returns byte array
            byte[] messageDigest = md.digest(input.getBytes());

            // convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // convert message digest into hex value
            StringBuilder hashtext = new StringBuilder(no.toString(16));

            // add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }

            // return the HashText
            return hashtext.toString();
        }
        // if wrong message digest algorithm was specified
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    static String encryptHMAC() {
        return "";
    }


    static String decryptHMAC() {
        return "";
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
