package bsi.passwordWallet;

import android.annotation.SuppressLint;

public class Validation {
    static final int LOGIN_MAX_LENGTH = 30;
    static final int PASSWORD_MAX_LENGTH = 30;
    static final int WEBSITE_MAX_LENGTH = 300;

    public static final String PASSWORD_CANT_BE_EMPTY = "Password can't be empty";
    public static final String PASSWORD_CANT_BE_LONGER_THAN = "Password can't be longer than %d characters";
    public static final String LOGIN_CANT_BE_EMPTY = "Login can't be empty";
    public static final String LOGIN_CANT_BE_LONGER_THAN = "Login can't be longer than %d characters";
    public static final String WEBSITE_CANT_BE_EMPTY = "Website can't be empty";
    public static final String WEBSITE_CANT_BE_LONGER_THAN = "Website can't be longer than %d characters";

    @SuppressLint("DefaultLocale")
    public String validatePassword(String password) {
        if(password.isEmpty())
            return PASSWORD_CANT_BE_EMPTY;
        else if(password.length() > PASSWORD_MAX_LENGTH)
            return String.format(PASSWORD_CANT_BE_LONGER_THAN, PASSWORD_MAX_LENGTH);
        else return "";
    }

    @SuppressLint("DefaultLocale")
    public String validateLogin(String login) {
        if(login.isEmpty())
            return LOGIN_CANT_BE_EMPTY;
        else if(login.length() > LOGIN_MAX_LENGTH)
            return String.format(LOGIN_CANT_BE_LONGER_THAN, LOGIN_MAX_LENGTH);

        else return "";
    }

    @SuppressLint("DefaultLocale")
    public String validateWebsite(String website) {
        if(website.isEmpty())
            return WEBSITE_CANT_BE_EMPTY;
        else if(website.length() > WEBSITE_MAX_LENGTH)
            return String.format(WEBSITE_CANT_BE_LONGER_THAN, WEBSITE_MAX_LENGTH);
        else return "";
    }

}
