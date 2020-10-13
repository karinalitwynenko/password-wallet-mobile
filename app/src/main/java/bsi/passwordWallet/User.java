package bsi.passwordWallet;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;


class User implements Parcelable {
    private long userID;
    private String login;
    private String encryptionType;
    private String passwordHash;
    private String salt;

    public User(long userID, String login, String encryptionType, String passwordHash, String salt) {
        this.userID = userID;
        this.login = login;
        this.encryptionType = encryptionType;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    protected User(Parcel in) {
        userID = in.readLong();
        login = in.readString();
        encryptionType = in.readString();
        passwordHash = in.readString();
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

    public long getUserID() {
        return userID;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    static boolean loginUser(User user, String password) {
        String sha256Cipher = Encryption.encryptSHA265(password, user.getSalt(), null);
        if(sha256Cipher.equals(user.getPasswordHash()))
            return true;
        else
            return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(userID);
        dest.writeString(login);
        dest.writeString(encryptionType);
        dest.writeString(passwordHash);
        dest.writeString(salt);
    }
}
