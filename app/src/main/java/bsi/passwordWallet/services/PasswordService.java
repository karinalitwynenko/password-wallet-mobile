package bsi.passwordWallet.services;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.Encryption;
import bsi.passwordWallet.Password;
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

    public boolean updatePasswords(ArrayList<Password> passwords, byte[] masterPassword, byte[] newMasterPassword) {
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

        return dataAccess.updatePasswords(passwords);
    }

    public String sharePassword(Password password, String partOwnerLogin) {
        User partOwner = dataAccess.getUser(partOwnerLogin);
        if(partOwner == null)
            return "User does not exist";
        else if(partOwner.getId() == password.getUserId())
            return "There is no need to share the password with yourself.";

        // check if the password is already shared with the user
        for(Password p : dataAccess.getSharedPasswords(partOwner.getId())) {
            if(p.getId() == password.getId())
                return "This user already has the access to the password.";
        }

        if(dataAccess.addSharedPassword(password.getId(), partOwner.getId()))
            return "Password has been shared";
        else
            return "Could not share the password";
    }

    public ArrayList<Password> getPasswords(long userId) {
        ArrayList<Password> passwords = dataAccess.getPasswords(userId);
        passwords.addAll(dataAccess.getSharedPasswords(userId));

        return passwords;
    }

}
