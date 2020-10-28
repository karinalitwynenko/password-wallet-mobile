package bsi.passwordWallet;

class Password {
    private long passwordID;
    private long userID;
    private String login;
    private String password;
    private String iv;
    private String website;
    private String description;

    public void setPassword(String password) {
        this.password = password;
    }

    public void setIV(String iv) {
        this.iv = iv;
    }

    public long getPasswordID() {
        return passwordID;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getWebsite() {
        return website;
    }

    public String getDescription() {
        return description;
    }

    public String getIV() {
        return this.iv;
    }

    public Password(long passwordID, long userID, String login, String password, String iv, String website, String description) {
        this.userID = userID;
        this.passwordID = passwordID;
        this.login = login;
        this.password = password;
        this.iv = iv;
        this.website = website;
        this.description = description;
    }
}
