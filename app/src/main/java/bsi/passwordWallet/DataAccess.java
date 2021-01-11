package bsi.passwordWallet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DataAccess {
    private static SQLiteDatabase database;
    private static final DataAccess instance = new DataAccess();
    /** database table names */
    static final String USER_TABLE = "users";
    static final String PASSWORD_TABLE = "passwords";
    static final String LOGIN_LOG_TABLE = "login_log";
    static final String BLOCKED_IPS_TABLE = "blocked_ips";
    static final String SHARED_PASSWORDS_TABLE = "shared_passwords";
    static final String ACTIVITY_LOGS_TABLE = "activity_logs";
    static final String PASSWORD_CHANGES_TABLE = "password_changes";

    private ContentValues contentValues = new ContentValues();

    public static DataAccess getInstance() {
        return instance;
    }

    public static void initialize(SQLiteDatabase database) {
        DataAccess.database = database;
    }

    public void setContentValues(ContentValues contentValues) {
        this.contentValues = contentValues;
    }

    /**
     * Retrieves the user with specified id
     * @param id user id
     * @return instance of User if exists, null otherwise
     */
    public User getUserById(long id) {
        Cursor cursor = database.rawQuery("select * from " + USER_TABLE +
                " where user_id = ?", new String[] {id + ""});
        User user = null;

        if(cursor.moveToFirst()) {
            user = new User(
                    cursor.getLong(cursor.getColumnIndex(User.USER_ID)),
                    cursor.getString(cursor.getColumnIndex(User.LOGIN)),
                    cursor.getString(cursor.getColumnIndex(User.ENCRYPTION_TYPE)),
                    cursor.getString(cursor.getColumnIndex(User.PASSWORD_HASH)),
                    cursor.getString(cursor.getColumnIndex(User.SALT))
            );
        }

        cursor.close();
        return user;
    }

    /**
     * Retrieves the user with specified login
     * @param login user's login
     * @return instance of User if exists, null otherwise
     */
    public User getUserByLogin(String login) {
        Cursor cursor = database.rawQuery("select * from " + USER_TABLE + " where login = ?", new String[] {login});
        User user = null;

        if(cursor.moveToFirst()) {
            user = new User(
                    cursor.getLong(cursor.getColumnIndex(User.USER_ID)),
                    cursor.getString(cursor.getColumnIndex(User.LOGIN)),
                    cursor.getString(cursor.getColumnIndex(User.ENCRYPTION_TYPE)),
                    cursor.getString(cursor.getColumnIndex(User.PASSWORD_HASH)),
                    cursor.getString(cursor.getColumnIndex(User.SALT))
            );
        }

        cursor.close();
        return user;
    }

    /**
     * Creates new user
     * @param login unique user login
     * @param encryptionMethod method for hash calculation
     * @param passwordHash password hash
     * @param salt salt used to calculate password hash
     * @return instance of User if created successfully, null otherwise
     */
    public User createUser(String login, String encryptionMethod, String passwordHash, String salt) {
        contentValues.clear();
        contentValues.put(User.LOGIN, login);
        contentValues.put(User.ENCRYPTION_TYPE, encryptionMethod);
        contentValues.put(User.PASSWORD_HASH, passwordHash);
        contentValues.put(User.SALT, salt);

        long userID = database.insert(USER_TABLE, null, contentValues);

        // if the row was successfully inserted
        if(userID != -1)
            return new User(userID, login, encryptionMethod, passwordHash, salt);
        else
            return null;
    }

    /**
     * Updates password hash and salt for user's account
     * @param userId id of user for whom account's password should be updated
     * @param newPassword hash of the new password
     * @param newSalt salt used to calculate password hash
     * @return true if updated successfully
     */
    public boolean updateUserMasterPassword(long userId, String newPassword, String newSalt) {
        contentValues.clear();
        contentValues.put(User.PASSWORD_HASH, newPassword);
        contentValues.put(User.SALT, newSalt);

        database.beginTransaction();
        if(database.update(USER_TABLE, contentValues, "user_id = ?", new String[] {userId + ""}) > 0)
            return true;
        else {
            database.endTransaction(); // rollback
            return false;
        }
    }

    /**
     * @param userId id of user for whom passwords should be retrieved
     * @return ArrayList of Password objects
     */
    @NonNull
    public ArrayList<Password> getPasswordsByUserId(long userId) {
        Cursor cursor = database.rawQuery(
                "select * from " + PASSWORD_TABLE + " where user_id = ? and deleted = ?",
                new String[] {userId + "", 0 + ""}
                );

        ArrayList<Password> passwords = new ArrayList<>();

        // loop through cursor
        while(cursor.moveToNext()) {
            passwords.add(mapToPassword(cursor));
        }
        cursor.close();

        return passwords;
    }

    public Password getPasswordById(long passwordId) {
        Cursor cursor = database.rawQuery(
                "select * from " + PASSWORD_TABLE + " where password_id = ?",
                new String[] {passwordId + ""}
        );

        Password password = null;

        // loop through cursor
        if(cursor.moveToNext()) {
            password = mapToPassword(cursor);
        }

        cursor.close();

        return password;
    }

    @NonNull
    private Password mapToPassword(Cursor cursor) {
        return new Password(
                cursor.getLong(cursor.getColumnIndex(Password.PASSWORD_ID)),
                cursor.getLong(cursor.getColumnIndex(Password.USER_ID)),
                cursor.getString(cursor.getColumnIndex(Password.LOGIN)),
                cursor.getString(cursor.getColumnIndex(Password.PASSWORD)),
                cursor.getString(cursor.getColumnIndex(Password.IV)),
                cursor.getString(cursor.getColumnIndex(Password.WEBSITE)),
                cursor.getString(cursor.getColumnIndex(Password.DESCRIPTION))
        );
    }

    /**
     * @param userID id of user for whom password should be created
     * @param login login to the website
     * @param password encrypted password
     * @param iv initialization vector for password encryption/decryption
     * @param website website name
     * @param description item description
     * @return new Password instance if created successfully, null if failed
     */
    public Password createPassword(long userID, String login, String password, String iv, String website, String description) {
        contentValues.clear();
        contentValues.put(Password.USER_ID, userID);
        contentValues.put(Password.LOGIN, login);
        contentValues.put(Password.PASSWORD, password);
        contentValues.put(Password.IV, iv);
        contentValues.put(Password.WEBSITE, website);
        contentValues.put(Password.DESCRIPTION, description);

        long passwordID = database.insert(PASSWORD_TABLE, null, contentValues);
        // check if the row was successfully inserted
        if(passwordID != -1)
            return new Password(passwordID, userID, login, password, iv, website, description);
        else
            return null;
    }

    public boolean updatePassword(Password password) {
        contentValues.clear();
        contentValues.put(Password.USER_ID, password.getUserId());
        contentValues.put(Password.LOGIN, password.getLogin());
        contentValues.put(Password.PASSWORD, password.getPassword());
        contentValues.put(Password.IV, password.getIV());
        contentValues.put(Password.WEBSITE, password.getWebsite());
        contentValues.put(Password.DESCRIPTION, password.getDescription());

        long passwordID = database.update(
                PASSWORD_TABLE,
                contentValues,
                "password_id=?",
                new String[] {password.getId() + ""}
                );

        return passwordID != -1;
    };

    public boolean markPasswordAsDeleted(long passwordId) {
        contentValues.clear();
        contentValues.put(Password.DELETED, 1);

        long id = database.update(
                PASSWORD_TABLE,
                contentValues,
                "password_id=?",
                new String[] {passwordId + ""}
        );

        return id != -1;
    }

    /**
     * @param passwordId id of password to delete
     * @return true if password deleted successfully
     */
    public boolean deletePassword(long passwordId) {
        return database.delete(
                PASSWORD_TABLE, "password_id = ?", new String[] {passwordId + ""}
                ) > 0;
    }

    public boolean deleteSharedPassword(long passwordId) {
        return database.delete(
                SHARED_PASSWORDS_TABLE, "password_id = ?", new String[] {passwordId + ""}
        ) > 0;
    }

    /**
     * Updates encrypted password.
     * Begin transaction before calling this method.
     * @param passwords ArrayList of Password objects
     * @return true if passwords updated successfully
     */
    public boolean updatePasswordHashes(ArrayList<Password> passwords) {
        for(Password p : passwords) {
            contentValues.clear();
            contentValues.put(Password.PASSWORD, p.getPassword());
            contentValues.put(Password.IV, p.getIV());
            if(database.update(PASSWORD_TABLE, contentValues, "password_id = ?",
                    new String[] {p.getId() + ""}) == 0) {

                // rollback
                database.endTransaction();
                return false;
            }
        }

        // transaction successful -> commit
        database.setTransactionSuccessful();
        database.endTransaction();
        return true;
    }

    public boolean createLoginLog(long userID, String ipAddress, long loginTime, String loginResult) {
        contentValues.clear();
        contentValues.put(LoginLog.USER_ID, userID);
        contentValues.put(LoginLog.IP_ADDRESS, ipAddress);
        contentValues.put(LoginLog.LOGIN_TIME, loginTime);
        contentValues.put(LoginLog.LOGIN_RESULT, loginResult);

        long logID = database.insert(LOGIN_LOG_TABLE, null, contentValues);

        if(logID != -1)
            return true;
        else
            return false;
    }

    public ArrayList<LoginLog> getLoginLogs(long userID, @Nullable String ipAddress, int limit) {
        Cursor cursor;
        String queryLimit;
        if(limit != -1)
            queryLimit = " limit " + limit;
        else
            queryLimit = "";

        if(ipAddress != null) {
                cursor = database.rawQuery(
                "select * from " + LOGIN_LOG_TABLE
                        + " where " + LoginLog.USER_ID + "=? and " + LoginLog.IP_ADDRESS + "=? "
                        + " order by " + LoginLog.LOG_ID + " desc " + queryLimit,
                    new String[] {userID + "", ipAddress}
                );
        }
        else {
            cursor = database.rawQuery(
                    "select * from " + LOGIN_LOG_TABLE
                            + " where " + LoginLog.USER_ID + "=?"
                            + " order by " + LoginLog.LOG_ID + " desc " + queryLimit,
                    new String[] {userID + ""}
            );
        }

        ArrayList<LoginLog> logs = new ArrayList<>();

        while(cursor.moveToNext()) {
            logs.add(
                    new LoginLog(
                            cursor.getLong(cursor.getColumnIndex(LoginLog.LOG_ID)),
                            cursor.getLong(cursor.getColumnIndex(LoginLog.USER_ID)),
                            cursor.getString(cursor.getColumnIndex(LoginLog.IP_ADDRESS)),
                            cursor.getLong(cursor.getColumnIndex(LoginLog.LOGIN_TIME)),
                            cursor.getString(cursor.getColumnIndex(LoginLog.LOGIN_RESULT)),
                            cursor.getInt(cursor.getColumnIndex(LoginLog.IGNORE_FAIL))
                    )
            );
        }
        cursor.close();

        return logs;
    }

    public boolean createBlockedIP(long userID, String ipAddress) {
        contentValues.clear();
        contentValues.put(LoginLog.USER_ID, userID);
        contentValues.put(LoginLog.IP_ADDRESS, ipAddress);

        long id = database.insert(BLOCKED_IPS_TABLE, null, contentValues);

        if(id != -1)
            return true;
        else
            return false;
    }

    public ArrayList<String> getBlockedIPs(long userID) {
        Cursor cursor = database.rawQuery(
                "select ip_address from " + BLOCKED_IPS_TABLE
                        + " where user_id = ?",
                new String[] {userID + ""}
        );

        ArrayList<String> ips = new ArrayList<>();
        while(cursor.moveToNext()) {
            ips.add(cursor.getString(0));
        }
        cursor.close();

        return ips;
    }

    public boolean deleteBlockedIP(String ipAddress) {
        database.beginTransaction();
        if(database.delete(BLOCKED_IPS_TABLE, "ip_address = ?", new String[] {ipAddress}) > 0) {
            contentValues.clear();
            contentValues.put(LoginLog.IGNORE_FAIL, 1);

            if(database.update(LOGIN_LOG_TABLE, contentValues, "ip_address = ?", new String[] {ipAddress}) > 0) {
                database.setTransactionSuccessful();
                database.endTransaction();
                return true;
            }
        }

        database.endTransaction();
        return false;
    }

    /** Closes the database connection */
    public static void close() {
        database.close();
    }

    public boolean addSharedPassword(SharedPassword sharedPassword) {
        contentValues.clear();
        contentValues.put(SharedPassword.PASSWORD_ID, sharedPassword.getPasswordId());
        contentValues.put(SharedPassword.PART_OWNER_ID, sharedPassword.getGetPartOwnerId());
        contentValues.put(SharedPassword.PASSWORD, sharedPassword.getPassword());
        contentValues.put(SharedPassword.IV, sharedPassword.getIv());
        contentValues.put(SharedPassword.NEEDS_UPDATE, sharedPassword.getNeedsUpdate());

        long sharedPasswordId = database.insert(SHARED_PASSWORDS_TABLE, null, contentValues);

        return sharedPasswordId != -1;
    }

    /**
     * Gets passwords that have been shared with user with specified id.
     * Omits passwords deleted by the owner.
     * @param userId
     * @return list of passwords shared with the user - empty if no passwords found
     */
    @NonNull
    public ArrayList<Password> getPasswordsSharedWithUser(long userId) {
        ArrayList<Password> passwords = new ArrayList<>();

        String sql = "select " +
                "p.password_id, p.user_id, p.login, sp.password as password, sp.iv as iv, p.website, p.description" +
                " from " + PASSWORD_TABLE + " as p inner join " + SHARED_PASSWORDS_TABLE +
                " as sp on sp.password_id = p.password_id " +
                "where sp.part_owner_id = " + userId + " and p.deleted = 0";

        Cursor cursor = database.rawQuery(sql, null);

        while(cursor.moveToNext()) {
            passwords.add(
                    new Password(
                            cursor.getLong(cursor.getColumnIndex(Password.PASSWORD_ID)),
                            cursor.getLong(cursor.getColumnIndex(Password.USER_ID)),
                            cursor.getString(cursor.getColumnIndex(Password.LOGIN)),
                            cursor.getString(cursor.getColumnIndex(Password.PASSWORD)),
                            cursor.getString(cursor.getColumnIndex(Password.IV)),
                            cursor.getString(cursor.getColumnIndex(Password.WEBSITE)),
                            cursor.getString(cursor.getColumnIndex(Password.DESCRIPTION))
                    )
            );
        }

        cursor.close();

        return passwords;
    }

    /**
     * Gets shared passwords records corresponding to a password in owners table.
     * @param passwordId id of password from the base table
     * @return list of shared passwords
     */
    @NonNull
    public ArrayList<SharedPassword> getSharedPasswords(long passwordId) {
        ArrayList<SharedPassword> passwords = new ArrayList<>();

        String sql = "select * from " + SHARED_PASSWORDS_TABLE
                + " where password_id = " + passwordId;

        Cursor cursor = database.rawQuery(sql, null);

        while(cursor.moveToNext()) {
            passwords.add(
                    new SharedPassword(
                            cursor.getLong(cursor.getColumnIndex(SharedPassword.SHARED_PASSWORD_ID)),
                            cursor.getLong(cursor.getColumnIndex(SharedPassword.PASSWORD_ID)),
                            cursor.getLong(cursor.getColumnIndex(SharedPassword.PART_OWNER_ID)),
                            cursor.getString(cursor.getColumnIndex(SharedPassword.PASSWORD)),
                            cursor.getString(cursor.getColumnIndex(SharedPassword.IV)),
                            cursor.getInt(cursor.getColumnIndex(SharedPassword.NEEDS_UPDATE))
                            )
            );
        }

        cursor.close();

        return passwords;
    }

    /**
     * Gets passwords that have been shared with the user and need the encryption update.
     * The update may be required when the owner changes the password or the password
     * has been shared recently and needs to be encrypted with a master password.
     * @param userId
     * @return list of shared passwords that should be updated  - empty if no passwords found
     */
    @NonNull
    public ArrayList<SharedPassword> getSharedPasswordsForUpdate(long userId) {
        ArrayList<SharedPassword> passwords = new ArrayList<>();

        String sql = "select sp.* from " + PASSWORD_TABLE + " p inner join " + SHARED_PASSWORDS_TABLE +
                " sp on sp.password_id = p.password_id " +
                "where sp.part_owner_id = " + userId + " and needs_update = 1";

        Cursor cursor = database.rawQuery(sql, null);

        while(cursor.moveToNext()) {
            passwords.add(
                    new SharedPassword(
                            cursor.getLong(cursor.getColumnIndex(SharedPassword.SHARED_PASSWORD_ID)),
                            cursor.getLong(cursor.getColumnIndex(SharedPassword.PASSWORD_ID)),
                            cursor.getLong(cursor.getColumnIndex(SharedPassword.PART_OWNER_ID)),
                            cursor.getString(cursor.getColumnIndex(SharedPassword.PASSWORD)),
                            cursor.getString(cursor.getColumnIndex(SharedPassword.IV)),
                            cursor.getInt(cursor.getColumnIndex(SharedPassword.NEEDS_UPDATE))
                    )
            );
        }

        cursor.close();

        return passwords;
    }

    public boolean updateSharedPassword(SharedPassword password) {
        contentValues.clear();
        contentValues.put(SharedPassword.PASSWORD, password.getPassword());
        contentValues.put(SharedPassword.IV, password.getIv());
        contentValues.put(SharedPassword.NEEDS_UPDATE, password.getNeedsUpdate());

        return database.update(
                SHARED_PASSWORDS_TABLE,
                contentValues,
                "shared_password_id = ?",
                new String[] {password.getId() + ""}
                ) > 0;
    }

    @NonNull
    public ArrayList<String> getPartOwners(long passwordId) {
        ArrayList<String> userLogins = new ArrayList<>();

        String sql = "select u.login from " + SHARED_PASSWORDS_TABLE + " sp inner join " + PASSWORD_TABLE
                + " p on sp.password_id = p.password_id inner join " + USER_TABLE +
                " u on u.user_id = sp.part_owner_id where p.password_id = ?";

        Cursor cursor = database.rawQuery(sql, new String[] {passwordId + ""});

        if(cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                userLogins.add(cursor.getString(0));
            }
        }
        cursor.close();

        return userLogins;
    }

    @NonNull
    public String getPasswordOwner(long passwordId) {
        String resultLogin = "";

        String sql = "select u.login from " + PASSWORD_TABLE
                + " p inner join " + USER_TABLE +
                " u on u.user_id = p.user_id where p.password_id = ?";

        Cursor cursor = database.rawQuery(sql, new String[] {passwordId + ""});

        if(cursor.getCount() > 0) {
            cursor.moveToNext();
            resultLogin = cursor.getString(0);
        }
        cursor.close();

        return resultLogin;
    }

    @NonNull
    public ArrayList<ActivityLog> getActivityLogs(long userId) {
        ArrayList<ActivityLog> logs = new ArrayList<>();

        Cursor cursor = database.rawQuery(
                "select * from " + ACTIVITY_LOGS_TABLE + " where user_id = ? order by time desc",
                new String[] {userId + ""}
                );

        while(cursor.moveToNext()) {
            logs.add(mapToActivityLog(cursor));
        }
        cursor.close();

        return logs;
    }

    public boolean addActivityLog(ActivityLog activityLog) {
        contentValues.clear();
        contentValues.put(ActivityLog.USER_ID, activityLog.getUserId());
        contentValues.put(ActivityLog.PASSWORD_ID, activityLog.getPasswordId());
        contentValues.put(ActivityLog.TIME, activityLog.getTime());
        contentValues.put(ActivityLog.ACTION_TYPE, activityLog.getActionType());
        contentValues.put(ActivityLog.PREVIOUS_VALUE, passwordToBytes(activityLog.getPreviousValue()));
        contentValues.put(ActivityLog.CURRENT_VALUE, passwordToBytes(activityLog.getCurrentValue()));

        return database.insert(ACTIVITY_LOGS_TABLE, null, contentValues) != -1;
    }

    public ArrayList<ActivityLog> getExtendedActivityLogs(long passwordId) {
        ArrayList<ActivityLog> logs = new ArrayList<>();

        Cursor cursor = database.rawQuery(
                "select * from " + ACTIVITY_LOGS_TABLE + " where password_id = ? " +
                     "and action_type in (\"update\", \"create\", \"delete\") order by time desc",
                    new String[] {passwordId + ""}
        );

        while(cursor.moveToNext()) {
            logs.add(mapToActivityLog(cursor));
        }
        cursor.close();

        return logs;
    }

    private ActivityLog mapToActivityLog(Cursor cursor) {
        return new ActivityLog(
                cursor.getLong(cursor.getColumnIndex(ActivityLog.ACTIVITY_ID)),
                cursor.getLong(cursor.getColumnIndex(ActivityLog.USER_ID)),
                cursor.getLong(cursor.getColumnIndex(ActivityLog.PASSWORD_ID)),
                cursor.getLong(cursor.getColumnIndex(ActivityLog.TIME)),
                cursor.getString(cursor.getColumnIndex(ActivityLog.ACTION_TYPE)),
                readPasswordFromBytes(cursor.getBlob(cursor.getColumnIndex(PasswordChange.PREVIOUS_VALUE))),
                readPasswordFromBytes(cursor.getBlob(cursor.getColumnIndex(PasswordChange.CURRENT_VALUE)))
        );
    }

//    public ArrayList<PasswordChange> getPasswordChanges(long passwordId) {
//        ArrayList<PasswordChange> passwordChanges = new ArrayList<>();
//
//        String sql = "select * from " + PASSWORD_CHANGES_TABLE + " where password_id = ? order by time desc";
//
//        Cursor cursor = database.rawQuery(sql, new String[] {passwordId + ""});
//
//        while(cursor.moveToNext()) {
//            passwordChanges.add(
//                    new PasswordChange(
//                            cursor.getLong(cursor.getColumnIndex(PasswordChange.PASSWORD_CHANGE_ID)),
//                            cursor.getLong(cursor.getColumnIndex(PasswordChange.PASSWORD_ID)),
//                            cursor.getLong(cursor.getColumnIndex(PasswordChange.TIME)),
//                            cursor.getString(cursor.getColumnIndex(PasswordChange.ACTION_TYPE)),
//                            readPasswordFromBytes(cursor.getBlob(cursor.getColumnIndex(PasswordChange.PREVIOUS_VALUE))),
//                            readPasswordFromBytes(cursor.getBlob(cursor.getColumnIndex(PasswordChange.CURRENT_VALUE)))
//                    )
//            );
//        }
//        cursor.close();
//
//        return passwordChanges;
//    }
//
//    public boolean createPasswordChange(PasswordChange passwordChange) {
//        contentValues.clear();
//        contentValues.put(PasswordChange.ACTION_TYPE, passwordChange.getActionType());
//        contentValues.put(PasswordChange.PASSWORD_ID, passwordChange.getPasswordId());
//        contentValues.put(PasswordChange.TIME, passwordChange.getTime());
//        contentValues.put(PasswordChange.PREVIOUS_VALUE, passwordToBytes(passwordChange.getPreviousValue()));
//        contentValues.put(PasswordChange.CURRENT_VALUE, passwordToBytes(passwordChange.getCurrentValue()));
//
//        long id = database.insert(PASSWORD_CHANGES_TABLE, null, contentValues);
//
//        return id != -1;
//    }

    public byte[] passwordToBytes(Password data) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Password readPasswordFromBytes(byte[] data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Password password = (Password)ois.readObject();
            return password ;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
