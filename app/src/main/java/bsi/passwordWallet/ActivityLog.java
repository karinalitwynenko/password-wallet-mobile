package bsi.passwordWallet;

public class ActivityLog {
    public static final String ACTIVITY_ID = "activity_id";
    public static final String USER_ID = "user_id";
    public static final String PASSWORD_ID = "password_id";
    public static final String TIME = "time";
    public static final String ACTION_TYPE = "action_type";
    public static final String PREVIOUS_VALUE = "previous_value";
    public static final String CURRENT_VALUE = "current_value";

    public static final String CREATE = "create";
    public static final String VIEW = "view";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String SHARE = "share";
    public static final String RECOVER = "recover";

    private long id;
    private long userId;
    private long passwordId;
    private long time;
    private String actionType;

    private Password previousValue;
    private Password currentValue;

    public ActivityLog(long userId,
                       long passwordId,
                       long time, String actionType,
                       Password previousValue,
                       Password newValue) {
        this.userId = userId;
        this.passwordId = passwordId;
        this.time = time;
        this.actionType = actionType;
        this.previousValue = previousValue;
        this.currentValue = newValue;
    }

    public ActivityLog(
            long id,
            long userId,
            long passwordId,
            long time,
            String actionType,
            Password previousValue,
            Password newValue) {
        this.id = id;
        this.userId = userId;
        this.passwordId = passwordId;
        this.time = time;
        this.actionType = actionType;
        this.previousValue = previousValue;
        this.currentValue = newValue;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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
