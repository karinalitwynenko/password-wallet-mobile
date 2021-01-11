package bsi.passwordWallet.services;

import java.util.ArrayList;
import java.util.Date;

import bsi.passwordWallet.ActivityLog;
import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.Encryption;
import bsi.passwordWallet.User;
import bsi.passwordWallet.Validation;

public class UserService {
    /**
     * user actions
     */
    public static final String VIEW = "view";
    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String SHARE = "share";
    public static final String RESTORE = "recover";

    public static final String USER_DOES_NOT_EXIST = "User doesn't exist";
    public static final String INCORRECT_PASSWORD = "Incorrect password";
    public static final String PASSWORDS_DO_NOT_MATCH = "Passwords don't match";
    public static final String LOGIN_EXISTS = "Login already exists";
    public static final String COULD_NOT_CREATE = "Couldn't create user's account";

    static final String IP_ADDRESS_CANT_BE_EMPTY = "IP address can't be empty";

    private DataAccess dataAccess = DataAccess.getInstance();
    private Validation validation = new Validation();
    private Encryption encryption = new Encryption();
    private LogService logService = new LogService();

    private DateTime dateTime = new DateTime() {
        @Override
        public Date getDate() {
            return new Date();
        }
    };

    public static class UserAccountException extends Exception {
        UserAccountException(String message){
            super(message);
        }
    }

    interface DateTime {
        Date getDate();
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

        String newPasswordHash;
        String newSalt = encryption.generateSalt64();

        if(user.getEncryptionMethod().equals(Encryption.SHA512))
            newPasswordHash = encryption.calculateSHA512(newPassword, newSalt);
        else
            newPasswordHash = encryption.calculateHMAC(newPassword, newSalt);

        if(dataAccess.updateUserMasterPassword(user.getId(), newPasswordHash, newSalt))
            return newPassword;
        else // if any error occurred
            throw new UserAccountException("Could not change user's password");
    }

    public User signIn(String login, String password, String ipAddress) throws UserAccountException {
        String validationResult = validation.validateLogin(login);
        // check if the login has invalid format
        if(!validationResult.isEmpty())
            throw new UserAccountException(validationResult);

        validationResult = validation.validatePassword(password);
        // check if the password has invalid format
        if(!validationResult.isEmpty())
            throw new UserAccountException(validationResult);

        if(ipAddress.isEmpty())
            throw new UserAccountException(IP_ADDRESS_CANT_BE_EMPTY);

        // try to get user with provided login
        User user = dataAccess.getUserByLogin(login);
        // check if the user was found
        if(user == null)
            throw new UserAccountException(USER_DOES_NOT_EXIST);

        Date currentLoginDate = dateTime.getDate();

        /*
        ***********************
            user account check
        ***********************/
        logService.checkUserAccount(user, currentLoginDate);

        /*
        ***********************
            ip check
        ***********************/
        boolean ipShouldBeBanned = logService.checkUserIP(user, ipAddress, currentLoginDate);

        String hash;
        if(user.getEncryptionMethod().equals(Encryption.SHA512))
            hash = encryption.calculateSHA512(password, user.getSalt());
        else
            hash = encryption.calculateHMAC(password, user.getSalt());

        // check if provided password is valid
        if(!hash.equals(user.getPassword())) {
            dataAccess.createLoginLog(user.getId(), ipAddress, currentLoginDate.getTime(), LogService.LOGIN_FAIL);

            if(ipShouldBeBanned)
                dataAccess.createBlockedIP(user.getId(), ipAddress); // block the address

            throw new UserAccountException(INCORRECT_PASSWORD);
        }
        else {
            dataAccess.createLoginLog(user.getId(), ipAddress, currentLoginDate.getTime(), LogService.LOGIN_SUCCESS);
            return user;
        }
    }

    public User signUp(String login, String password, String confirmPassword, String encryptionMethod) throws UserAccountException {
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

        User user = dataAccess.getUserByLogin(login);
        // check if provided login is already in use
        if(user != null)
            throw new UserAccountException(LOGIN_EXISTS);

        String hash;
        // generate random salt
        String salt = encryption.generateSalt64();

        // generate hash for chosen encryption method
        if(encryptionMethod.equals(Encryption.SHA512))
            hash = encryption.calculateSHA512(password, salt);
        else
            hash = encryption.calculateHMAC(password, salt);

        // create a user
        user = dataAccess.createUser(login, encryptionMethod, hash, salt);

        // check if the user was properly created
        if(user == null)
            throw new UserAccountException(COULD_NOT_CREATE);
        else
            return user;
    }

}
