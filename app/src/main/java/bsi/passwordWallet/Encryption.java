package bsi.passwordWallet;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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

    static String encryptAES128(String input, String key) {
        return "hash";
    }

    static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt.toString();
    }
}
