package bsi.passwordWallet;

public class ActivityLog {
    public static final String ACTIVITY_ID = "activity_id";
    public static final String USER_ID = "user_id";
    public static final String PASSWORD_ID = "password_id";
    public static final String TIME = "time";
    public static final String FUNCTION = "function";

    public static final String CREATE = "create";
    public static final String VIEW = "view";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String SHARE = "share";

    private long activityId;
    private long userId;
    private long passwordId;
    private long time;
    private String function;

    public ActivityLog(long userId, long passwordId, long time, String function) {
        this.activityId = activityId;
        this.userId = userId;
        this.passwordId = passwordId;
        this.time = time;
        this.function = function;
    }

    public ActivityLog(long activityId, long userId, long passwordId, long time, String function) {
        this.activityId = activityId;
        this.userId = userId;
        this.passwordId = passwordId;
        this.time = time;
        this.function = function;
    }

    public long getActivityId() {
        return activityId;
    }

    public void setActivityId(long activityId) {
        this.activityId = activityId;
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

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
