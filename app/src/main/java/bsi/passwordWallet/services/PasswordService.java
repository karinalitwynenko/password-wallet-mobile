package bsi.passwordWallet.services;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.Encryption;
import bsi.passwordWallet.Password;
import bsi.passwordWallet.SharedPassword;
import bsi.passwordWallet.User;
import bsi.passwordWallet.Validation;

public class PasswordService {
    public static final String NEW_PASSWORD_ADDED = "New password has been added";
    public static final String COULD_NOT_CREATE = "Could not add new password";
    private DataAccess dataAccess = DataAccess.getInstance();
    private Validation validation = new Validation();
    private Encryption encryption = new Encryption();

    public static class PasswordCreationException extends Exception {
        public PasswordCreationException(String message) {
            super(message);
        }
    }

    public Password addPassword(User user, byte[] masterPassword, HashMap<String, String> passwordParams) throws PasswordCreationException {
        ArrayList<String> validationResults = new ArrayList<>();

        validationResults.add(validation.validatePassword(passwordParams.get(Password.PASSWORD)));
        validationResults.add(validation.validateLogin(passwordParams.get(Password.LOGIN)));
        validationResults.add(validation.validateWebsite(passwordParams.get(Password.WEBSITE)));

        int validationMessageIndex = -1;
        for(int i = 0; i < validationResults.size(); i++)
            if(validationResults.get(i) != null && !validationResults.get(i).isEmpty())
                validationMessageIndex = i;

        // check if any validation error occurred
        if(validationMessageIndex != -1)
            throw new PasswordCreationException(validationResults.get(validationMessageIndex));

        byte[] randomIV = encryption.randomIV();

        Password newPassword = dataAccess.createPassword(
                user.getId(),
                passwordParams.get(Password.LOGIN),
                encryption.encryptAES128(passwordParams.get(Password.PASSWORD), masterPassword, randomIV),
                Base64.getEncoder().encodeToString(randomIV),
                passwordParams.get(Password.WEBSITE),
                passwordParams.get(Password.DESCRIPTION)
        );

        if(newPassword != null)
            return newPassword;
        else
            throw new PasswordCreationException(COULD_NOT_CREATE);
    }

    public boolean updatePassword(Password password, byte[] masterPassword) throws PasswordCreationException {
        ArrayList<String> validationResults = new ArrayList<>();
        String plaintextPassword = password.getPassword();
        validationResults.add(validation.validatePassword(password.getPassword()));
        validationResults.add(validation.validateLogin(password.getLogin()));
        validationResults.add(validation.validateWebsite(password.getLogin()));

        int validationMessageIndex = -1;
        for(int i = 0; i < validationResults.size(); i++)
            if(validationResults.get(i) != null && !validationResults.get(i).isEmpty())
                validationMessageIndex = i;

        // check if any validation error occurred
        if(validationMessageIndex != -1)
            throw new PasswordCreationException(validationResults.get(validationMessageIndex));

        byte[] randomIV = encryption.randomIV();

        password.setPassword(encryption.encryptAES128(plaintextPassword, masterPassword, randomIV));
        password.setIV(Base64.getEncoder().encodeToString(randomIV));

        if(!dataAccess.updatePassword(password)) {
            throw new PasswordCreationException("Could not update password details.");
        }

        if(!updateSharedPasswords(password, plaintextPassword)) {
            throw new PasswordCreationException("Could not update shared passwords");
        }
        else {
            return true;
        }
    }

    public boolean updatePasswordHashes(ArrayList<Password> passwords, byte[] masterPassword, byte[] newMasterPassword) {
        String decryptedPassword;
        byte[] randomIV;

        for (Password p : passwords) {
            // decrypt the password using previous user's key
            decryptedPassword = encryption.decryptAES128(
                    p.getPassword(), masterPassword, Base64.getDecoder().decode(p.getIV())
            );
            // generate new initialization vector
            randomIV = encryption.randomIV();
            // encrypt the password using new user's key
            p.setPassword(encryption.encryptAES128(decryptedPassword, newMasterPassword, randomIV));
            p.setIV(Base64.getEncoder().encodeToString(randomIV));
        }

        return dataAccess.updatePasswordHashes(passwords);
    }

    public String sharePassword(Password password, String partOwnerLogin, byte[] masterPassword) {
        User partOwner = dataAccess.getUserByLogin(partOwnerLogin);
        if(partOwner == null)
            return "User does not exist";
        else if(partOwner.getId() == password.getUserId())
            return "Can't share the password with yourself.";

        // check if the password is already shared with the user
        for(Password p : dataAccess.getPasswordsSharedWithUser(partOwner.getId())) {
            if(p.getId() == password.getId())
                return "This user already has the access to the password.";
        }

        String decryptedPassword = encryption.decryptAES128(
                password.getPassword(), masterPassword, Base64.getDecoder().decode(password.getIV())
        );
        // generate new initialization vector
        byte[] randomIV = encryption.randomIV();

        /**
         *  Temporarily encrypt the password using the second user's password hash.
         *  Use MD5 to match key size for AES128.
          */
        String tempEncryptedPassword = encryption.encryptAES128(
                decryptedPassword,
                encryption.calculateMD5(partOwner.getPassword()),
                randomIV
        );
        SharedPassword sharedPassword = new SharedPassword(
                -1,
                password.getId(),
                partOwner.getId(),
                tempEncryptedPassword,
                Base64.getEncoder().encodeToString(randomIV),
                1
        );

        if(dataAccess.addSharedPassword(sharedPassword))
            return "Password has been shared";
        else
            return "Could not share the password";
    }

    public ArrayList<Password> getPasswords(long partOwnerId) {
        ArrayList<Password> passwords = dataAccess.getPasswords(partOwnerId);
        passwords.addAll(dataAccess.getPasswordsSharedWithUser(partOwnerId));

        return passwords;
    }

    public boolean updateSharedPasswords(Password password, String plaintextPassword) {
        byte[] randomIV;
        ArrayList<SharedPassword> passwords = dataAccess.getSharedPasswords(password.getId());
        User user;
        boolean result = true;
        for(SharedPassword sharedPassword : passwords) {
            user = dataAccess.getUserById(sharedPassword.getGetPartOwnerId());
            randomIV = encryption.randomIV();
            // temporarily encrypt the password using part owner's password hash
            String encryptedPassword = encryption.encryptAES128(
                    plaintextPassword,
                    encryption.calculateMD5(user.getPassword()),
                    randomIV
            );
            sharedPassword.setPassword(encryptedPassword);
            sharedPassword.setIv(Base64.getEncoder().encodeToString(randomIV));
            sharedPassword.setNeedsUpdate(1);

            result = dataAccess.updateSharedPassword(sharedPassword);
        }

        return result;
    }

    public void checkForSharedPasswordUpdates(User user, byte[] masterPassword) {
        ArrayList<SharedPassword> passwords = dataAccess.getSharedPasswordsForUpdate(user.getId());
        String decryptedPassword, encryptedPassword;
        byte[] randomIV;

        for(SharedPassword sharedPassword : passwords) {
            decryptedPassword = encryption.decryptAES128(
                    sharedPassword.getPassword(),
                    encryption.calculateMD5(user.getPassword()),
                    Base64.getDecoder().decode(sharedPassword.getIv())
            );

            randomIV = encryption.randomIV();
            encryptedPassword = encryption.encryptAES128(decryptedPassword, masterPassword, randomIV);

            sharedPassword.setPassword(encryptedPassword);
            sharedPassword.setIv(Base64.getEncoder().encodeToString(randomIV));
            sharedPassword.setNeedsUpdate(0);

            dataAccess.updateSharedPassword(sharedPassword);
        }
    }

    public boolean deletePassword(long passwordId) {
        return dataAccess.markPasswordAsDeleted(passwordId);
    }

    public boolean deletePasswordPermanently(long passwordId) {
        if(dataAccess.deletePassword(passwordId)) {
            return dataAccess.deleteSharedPassword(passwordId);
        }
        else return false;
    }

}
