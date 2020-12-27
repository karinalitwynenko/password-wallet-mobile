package bsi.passwordWallet;

import java.io.Serializable;

public class Password implements Serializable {
    public static final String PASSWORD_ID = "password_id";
    public static final String USER_ID = "user_id";
    public static final String LOGIN = "login";
    public static final String WEBSITE = "website";
    public static final String DESCRIPTION = "description";
    public static final String PASSWORD = "password";
    public static final String IV = "iv";
    public static final String DELETED = "deleted";

    private long id;
    private long userId;
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

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
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

    public void setId(long id) {
        this.id = id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Password() {}

    public Password(
            long passwordId,
            long userId,
            String login,
            String password,
            String iv, String website, String description) {
        this.id = passwordId;
        this.userId = userId;
        this.login = login;
        this.password = password;
        this.iv = iv;
        this.website = website;
        this.description = description;
    }
}
