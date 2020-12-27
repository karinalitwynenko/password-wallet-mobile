package bsi.passwordWallet;

public class PasswordChange {
    public static final String PASSWORD_CHANGE_ID = "password_change_id";
    public static final String PASSWORD_ID = "password_id";
    public static final String TIME = "time";
    public static final String RECORD_NAME = "record_name";
    public static final String ACTION_TYPE = "action_type";
    public static final String PREVIOUS_VALUE = "previous_value";
    public static final String NEW_VALUE = "new_value";

    private long passwordChangeId;
    private long passwordId;
    private long time;
    private String record_name;
    private String actionType;
    private String previousValue;
    private String newValue;

    public PasswordChange(long passwordChangeId,
                          long passwordId,
                          long time,
                          String record_name,
                          String actionType,
                          String previousValue,
                          String newValue) {
        this.passwordChangeId = passwordChangeId;
        this.passwordId = passwordId;
        this.time = time;
        this.record_name = record_name;
        this.actionType = actionType;
        this.previousValue = previousValue;
        this.newValue = newValue;
    }
}
