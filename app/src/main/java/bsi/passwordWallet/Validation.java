package bsi.passwordWallet;

class Validation {
    static final int LOGIN_MAX_LENGTH = 30;
    static final int PASSWORD_MAX_LENGTH = 30;
    static final int WEBSITE_MAX_LENGTH = 300;

    static public String validatePassword(String password) {
        if(password.isEmpty())
            return "Password can't be empty";
        else if(password.length() > PASSWORD_MAX_LENGTH)
            return "Password can't be longer than " + PASSWORD_MAX_LENGTH + " characters";
        else return "";
    }

    static public String validateLogin(String login) {
        if(login.isEmpty())
            return "Login can't be empty";
        else if(login.length() > LOGIN_MAX_LENGTH)
            return "Login can't be longer than " + LOGIN_MAX_LENGTH + " characters";
        else return "";
    }

    static public String validateWebsite(String website) {
        if(website.isEmpty())
            return "Website can't be empty";
        else if(website.length() > WEBSITE_MAX_LENGTH)
            return "Website can't be longer than " + WEBSITE_MAX_LENGTH + " characters";
        else return "";
    }
}
