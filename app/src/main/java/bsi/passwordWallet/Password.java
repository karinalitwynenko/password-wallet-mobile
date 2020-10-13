package bsi.passwordWallet;

class Password {
    long passwordID;
    String login;
    String password;
    String encryptedPassword;
    String website;
    String description;

    public Password(long passwordID, String login, String encryptedPassword, String website, String description) {
        this.passwordID = passwordID;
        this.login = login;
        this.encryptedPassword = encryptedPassword;
        this.website = website;
        this.description = description;
    }

    Password(String website) {
        this.website = website;
    }
}
