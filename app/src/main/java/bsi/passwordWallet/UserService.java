package bsi.passwordWallet;

import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class UserService {
    private Validation validation = new Validation();
    private Encryption encryption = new Encryption();
    private DataAccess dataAccess = new DataAccess();

    static class UserAccountException extends Exception {
        UserAccountException(String message){
            super(message);
        }
    }

    public String updatePassword(User user, String oldPassword, String newPassword, String masterPassword) throws UserAccountException {
        // validate passwords format
        String validationResult = validation.validatePassword(oldPassword);
        if(!validationResult.equals(""))
            throw new UserAccountException(validationResult);

        validationResult = validation.validatePassword(newPassword);
        if(!validationResult.equals(""))
            throw new UserAccountException(validationResult);

        // check if current and new password are the same
        if(oldPassword.equals(newPassword))
            throw new UserAccountException("New password is the same as the current one.");

        // check if master password is valid
        if(!oldPassword.equals(masterPassword))
            throw new UserAccountException("Incorrect user password");

        String newPasswordHash, encryptionMethod;
        String newSalt = encryption.generateSalt64();

        if(user.getEncryptionMethod().equals(Encryption.SHA512))
            encryptionMethod = Encryption.SHA512;
        else
            encryptionMethod = Encryption.HMAC_SHA512;

        newPasswordHash = calculateHash(encryptionMethod, newPassword, newSalt);

        if(dataAccess.updateUserMasterPassword(user.getId(), newPasswordHash, newSalt)) {
            return newPassword;
        }
        else // if any error occurred
            // notify the user
            throw new UserAccountException("Could not change user's password");
    }

    String calculateHash(String encryptionMethod, String input, String salt) {
        String hash;
        if(encryptionMethod.equals(Encryption.SHA512)) {
            try {
                encryption.setMessageDigest(MessageDigest.getInstance(Encryption.SHA512));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            hash = encryption.calculateSHA512(input, salt, Encryption.PEPPER);

        }
        else {
            try {
                encryption.setMac(new Encryption.MacWrapper(Mac.getInstance(Encryption.HMAC_SHA512)));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            hash = encryption.calculateHMAC(
                    input,
                    new SecretKeySpec(salt.getBytes(StandardCharsets.UTF_8), Encryption.HMAC_SHA512),
                    Encryption.PEPPER
            );
        }

        return hash;
    }
}
