package bsi.passwordWallet;

import android.os.Parcel;
import android.os.Parcelable;


public class User implements Parcelable {
    public static final String USER_ID = "user_id";
    public static final String LOGIN = "login";
    public static final String ENCRYPTION_TYPE = "encryption_type";
    public static final String PASSWORD_HASH = "password_hash";
    public static final String SALT = "salt";

    private long id;
    private String login;
    private String encryptionMethod;
    private String password;
    private String salt;

    public User() {}

    public User(long id, String login, String encryptionMethod, String password, String salt) {
        this.id = id;
        this.login = login;
        this.encryptionMethod = encryptionMethod;
        this.password = password;
        this.salt = salt;
    }

    protected User(Parcel in) {
        id = in.readLong();
        login = in.readString();
        encryptionMethod = in.readString();
        password = in.readString();
        salt = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getSalt() {
        return salt;
    }

    public String getEncryptionMethod() {
        return encryptionMethod;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(login);
        dest.writeString(encryptionMethod);
        dest.writeString(password);
        dest.writeString(salt);
    }
}
