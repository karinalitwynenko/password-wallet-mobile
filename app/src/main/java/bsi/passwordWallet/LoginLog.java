package bsi.passwordWallet;

public class LoginLog {
    public static final String LOG_ID = "log_id";
    public static final String USER_ID = "user_id";
    public static final String IP_ADDRESS = "ip_address";
    public static final String LOGIN_TIME = "login_time";
    public static final String LOGIN_RESULT = "login_result";
    public static final String IGNORE_FAIL = "ignore_fail";

    private long id;
    private final long userId;
    private final String ipAddress;
    private final long loginTime;
    private final String loginResult;
    private final int ignoreFail;

    public LoginLog(long id, long userId, String ipAddress, long loginTime, String loginResult, int ignoreFail) {
        this.id = id;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.loginTime = loginTime;
        this.loginResult = loginResult;
        this.ignoreFail = ignoreFail;
    }

    public int getIgnoreFail() {
        return ignoreFail;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public String getLoginResult() {
        return loginResult;
    }

    public String getIpAddress() {
        return ipAddress;
    }

}
