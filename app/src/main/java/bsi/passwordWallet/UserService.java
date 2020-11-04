package bsi.passwordWallet;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class UserService {
    public final static String USER_DOES_NOT_EXIST = "User doesn't exist";
    public final static String INCORRECT_PASSWORD = "Incorrect password";
    public final static String PASSWORDS_DO_NOT_MATCH = "Passwords don't match";
    public final static String LOGIN_EXISTS = "Login already exists";
    public final static String COULD_NOT_CREATE = "Couldn't create user's account";
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

        if(dataAccess.updateUserMasterPassword(user.getId(), newPasswordHash, newSalt))
            return newPassword;
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

    User signIn(String login, String password) throws UserAccountException {
        String validationResult = validation.validateLogin(login);
        // check if the login has invalid format
        if(!validationResult.isEmpty())
            throw new UserAccountException(validationResult);


        validationResult = validation.validatePassword(password);
        // check if the password has invalid format
        if(!validationResult.isEmpty())
            throw new UserAccountException(validationResult);


         // try to get user with provided login
        User user = dataAccess.getUser(login);
        // check if the user was found
        if(user == null)
            throw new UserAccountException(USER_DOES_NOT_EXIST);

        String hash;
        String encryptionMethod;
        if(user.getEncryptionMethod().equals(Encryption.SHA512))
            encryptionMethod = Encryption.SHA512;
        else
            encryptionMethod = Encryption.HMAC_SHA512;

        hash = new UserService().calculateHash(encryptionMethod, password, user.getSalt());

        // check if provided password is valid
        if(!hash.equals(user.getPassword()))
            throw new UserAccountException(INCORRECT_PASSWORD);
        else
            return user;
    }

    User signUp(String login, String password, String confirmPassword, String encryptionMethod) throws UserAccountException {
        ArrayList<String> validationResults = new ArrayList<>();
        validationResults.add(validation.validatePassword(confirmPassword));
        validationResults.add(validation.validatePassword(password));
        validationResults.add(validation.validateLogin(login));

        int validationMessageIndex = -1;
        for(int i = 0; i < validationResults.size(); i++) {
            if(!validationResults.get(i).isEmpty())
                validationMessageIndex = i;
        }

        if(validationMessageIndex != -1)
            throw new UserAccountException(validationResults.get(validationMessageIndex));

        if(!password.equals(confirmPassword))
            throw new UserAccountException(PASSWORDS_DO_NOT_MATCH);

        User user = dataAccess.getUser(login);
        // check if provided login is already in use
        if(user != null)
            throw new UserAccountException(LOGIN_EXISTS);

        // generate random salt
        String salt = encryption.generateSalt64();
        String hash;

        // generate hash for chosen encryption method
        hash = calculateHash(encryptionMethod, password, salt);

        // create a user
        user = dataAccess.createUser(login, encryptionMethod, hash, salt);

        // check if the user was properly created
        if(user == null)
            throw new UserAccountException(COULD_NOT_CREATE);
        else
            return user;
    }
}
