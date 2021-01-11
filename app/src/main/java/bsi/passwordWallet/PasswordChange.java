package bsi.passwordWallet;

public class PasswordChange {
    public static final String PASSWORD_CHANGE_ID = "password_change_id";
    public static final String PASSWORD_ID = "password_id";
    public static final String TIME = "time";
    public static final String ACTION_TYPE = "action_type";
    public static final String PREVIOUS_VALUE = "previous_value";
    public static final String CURRENT_VALUE = "current_value";

    private long passwordChangeId;
    private long passwordId;
    private long time;
    private String actionType;
//    private String previousValue;
//    private String currentValue;
    private Password previousValue;
    private Password currentValue;

    public PasswordChange(long passwordChangeId,
                          long passwordId,
                          long time,
                          String actionType,
                          Password previousValue,
                          Password newValue) {
        this.passwordChangeId = passwordChangeId;
        this.passwordId = passwordId;
        this.time = time;
        this.actionType = actionType;
        this.previousValue = previousValue;
        this.currentValue = newValue;
    }

    public long getPasswordChangeId() {
        return passwordChangeId;
    }

    public void setPasswordChangeId(long passwordChangeId) {
        this.passwordChangeId = passwordChangeId;
    }

    public long getPasswordId() {
        return passwordId;
    }

    public void setPasswordId(long passwordId) {
        this.passwordId = passwordId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Password getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(Password previousValue) {
        this.previousValue = previousValue;
    }

    public Password getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Password currentValue) {
        this.currentValue = currentValue;
    }
}
