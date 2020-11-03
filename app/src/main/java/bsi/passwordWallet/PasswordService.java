package bsi.passwordWallet;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class PasswordService {
    static final String NEW_PASSWORD_ADDED = "New password has been added";
    static final String COULD_NOT_CREATE = "Could not add new password";
    private DataAccess dataAccess = new DataAccess();
    private Validation validation = new Validation();
    private Encryption encryption;


    static public class PasswordCreationException extends Exception {
        public PasswordCreationException(String message) {
            super(message);
        }
    }

    public PasswordService() {
        encryption = new Encryption();
        try {
            encryption.setCipher(new Encryption.CipherWrapper(Cipher.getInstance("AES/CBC/PKCS7PADDING")));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public PasswordService(Encryption encryption) {
        this.encryption = encryption;
    }

    void setDataAccess(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public Password addPassword(User user, byte[] masterPassword, HashMap<String, String> passwordParams) throws PasswordCreationException {
        String validationResult = validate(
                passwordParams.get(DataAccess.LOGIN),
                passwordParams.get(DataAccess.PASSWORD),
                passwordParams.get(DataAccess.WEBSITE)
        );

        if(!validationResult.equals(""))
            throw new PasswordCreationException(validationResult);

        byte[] randomIV = encryption.randomIV();

        Password newPassword = dataAccess.createPassword(
                user.getId(),
                passwordParams.get(DataAccess.LOGIN),
                encryption.encryptAES128(passwordParams.get(DataAccess.PASSWORD), new SecretKeySpec(masterPassword, "AES"), new IvParameterSpec(randomIV)),
                Base64.getEncoder().encodeToString(randomIV),
                passwordParams.get(DataAccess.WEBSITE),
                passwordParams.get(DataAccess.DESCRIPTION)
        );

        if(newPassword != null)
            return newPassword;
        else
            throw new PasswordCreationException(COULD_NOT_CREATE);
    }

    public String validate(String newLogin, String newPassword, String newWebsite) {
        ArrayList<String> validationResults = new ArrayList<>();

        validationResults.add(validation.validatePassword(newPassword));
        validationResults.add(validation.validateLogin(newLogin));
        validationResults.add(validation.validateWebsite(newWebsite));

        int validationMessageIndex = -1;
        for(int i = 0; i < validationResults.size(); i++)
            if(validationResults.get(i) != null && !validationResults.get(i).isEmpty())
                validationMessageIndex = i;

        // check if any validation error occurred
        if(validationMessageIndex != -1)
            return validationResults.get(validationMessageIndex);
        else
            return "";
    }

    boolean updatePasswords(ArrayList<Password> passwords, byte[] masterPassword, byte[] newMasterPassword) {
        String decryptedPassword;
        byte[] randomIV;

        for (Password p : passwords) {
            // decrypt the password using previous user's key
            decryptedPassword = encryption.decryptAES128(
                    p.getPassword(), new SecretKeySpec(masterPassword, "AES"),  new IvParameterSpec(Base64.getDecoder().decode(p.getIV()))
            );
            // generate new initialization vector
            randomIV = encryption.randomIV();
            // encrypt the password using new user's key
            p.setPassword(encryption.encryptAES128(decryptedPassword, new SecretKeySpec(newMasterPassword, "AES"), new IvParameterSpec(randomIV)));
            p.setIV(Base64.getEncoder().encodeToString(randomIV));
        }

        return dataAccess.updatePasswords(passwords);
    }

}
