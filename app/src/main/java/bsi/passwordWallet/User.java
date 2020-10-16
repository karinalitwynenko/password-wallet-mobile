package bsi.passwordWallet;

import android.os.Parcel;
import android.os.Parcelable;


class User implements Parcelable {
    private long userID;
    private String login;
    private String encryptionMethod;
    private String password;
    private String salt;

    public User(long userID, String login, String encryptionType, String password, String salt) {
        this.userID = userID;
        this.login = login;
        this.encryptionMethod = encryptionType;
        this.password = password;
        this.salt = salt;
    }

    protected User(Parcel in) {
        userID = in.readLong();
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

    public long getUserID() {
        return userID;
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
        dest.writeLong(userID);
        dest.writeString(login);
        dest.writeString(encryptionMethod);
        dest.writeString(password);
        dest.writeString(salt);
    }
}
