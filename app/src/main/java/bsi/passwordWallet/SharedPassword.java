package bsi.passwordWallet;

public class SharedPassword {
    public static final String SHARED_PASSWORD_ID = "shared_password_id";
    public static final String PASSWORD_ID = "password_id";
    public static final String PART_OWNER_ID = "part_owner_id";
    public static final String PASSWORD = "password";
    public static final String IV = "iv";
    public static final String NEEDS_UPDATE = "needs_update";

    private long id;
    private long passwordId;
    private long getPartOwnerId;
    private String password;
    private String iv;
    private int needsUpdate;


    public SharedPassword(long id, long passwordId, long getPartOwnerId, String password, String iv, int needsUpdate) {
        this.id = id;
        this.passwordId = passwordId;
        this.getPartOwnerId = getPartOwnerId;
        this.password = password;
        this.iv = iv;
        this.needsUpdate = needsUpdate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPasswordId() {
        return passwordId;
    }

    public void setPasswordId(long password_id) {
        this.passwordId = password_id;
    }

    public long getGetPartOwnerId() {
        return getPartOwnerId;
    }

    public void setGetPartOwnerId(long getPartOwnerId) {
        this.getPartOwnerId = getPartOwnerId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public int getNeedsUpdate() {
        return needsUpdate;
    }

    public void setNeedsUpdate(int needsUpdate) {
        this.needsUpdate = needsUpdate;
    }
}