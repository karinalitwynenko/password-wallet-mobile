package bsi.passwordWallet.services;

import java.util.ArrayList;
import java.util.Date;

import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.LoginLog;
import bsi.passwordWallet.User;

public class LogService {
    static final String ACCOUNT_BLOCKED_FOR_5S = "Your account has been blocked for 5 seconds.";
    static final String ACCOUNT_BLOCKED_FOR_10S = "Your account has been blocked for 10 seconds.";
    static final String ACCOUNT_BLOCKED_FOR_2M = "Your account has been blocked for 2 minutes.";
    static final String IP_BLOCKED_FOR_5S = "Your IP address has been blocked for 5 seconds.";
    static final String IP_BLOCKED_FOR_10S = "Your IP address has been blocked for 10 seconds.";
    static final String IP_BLOCKED_PERMANENTLY = "Your IP address has been blocked permanently.";

    public final static String LOGIN_SUCCESS = "SUCCESS";
    public final static String LOGIN_FAIL = "FAIL";
    public final static int FAILS_FROM_IP_LIMIT = 4;
    private DataAccess dataAccess = DataAccess.getInstance();

    /**
     *
     * @param user
     * @param ipAddress
     * @param loginDate
     * @return if next login fail should ban the ip (when exceed allowed limit)
     * @throws UserService.UserAccountException
     */
    boolean checkUserIP(User user, String ipAddress, Date loginDate) throws UserService.UserAccountException {
        // check if ips is permanently blocked
        ArrayList<String> blockedIPs = dataAccess.getBlockedIPs(user.getId());
        if(blockedIPs.contains(ipAddress))
            throw new UserService.UserAccountException(IP_BLOCKED_PERMANENTLY);

        ArrayList<LoginLog> logs =  dataAccess.getLoginLogs(user.getId(), ipAddress, 4);

        int failSequenceLength = 0;
        for(int i = 0; i < logs.size(); i++) {
            if(logs.get(i).getLoginResult().equals(LOGIN_SUCCESS) || logs.get(i).getIgnoreFail() == 1)
                break;
            else
                failSequenceLength++;
        }

        if(failSequenceLength > 1) {
            long timeDiff = loginDate.getTime() - logs.get(0).getLoginTime();
            long timeDiffInSeconds = (timeDiff / 1000);

            if(failSequenceLength == 3 && timeDiffInSeconds < 10)
                throw new UserService.UserAccountException(IP_BLOCKED_FOR_10S);
            else if(failSequenceLength == 2 && timeDiffInSeconds < 5)
                throw new UserService.UserAccountException(IP_BLOCKED_FOR_5S);
        }

        // check if ip should be banned on next fail
        if(failSequenceLength >= FAILS_FROM_IP_LIMIT - 1)
            return true;
        else
            return false;
    }

    void checkUserAccount(User user, Date loginDate) throws UserService.UserAccountException {
        ArrayList<LoginLog> logs =  dataAccess.getLoginLogs(user.getId(), null, 4);

        int failSequenceLength = 0;
        for(int i = 0; i < logs.size(); i++) {
            if(logs.get(i).getLoginResult().equals(LOGIN_SUCCESS))
                break;
            else
                failSequenceLength++;
        }

        if(failSequenceLength > 1) {
            long timeDiff = loginDate.getTime() - logs.get(0).getLoginTime();
            long timeDiffInSeconds = (timeDiff / 1000);

            if(failSequenceLength >= 4 && timeDiffInSeconds < 120)
                throw new UserService.UserAccountException(ACCOUNT_BLOCKED_FOR_2M);
            else if(failSequenceLength == 3 && timeDiffInSeconds < 10)
                throw new UserService.UserAccountException(ACCOUNT_BLOCKED_FOR_10S);
            else if(failSequenceLength == 2 && timeDiffInSeconds < 5)
                throw new UserService.UserAccountException(ACCOUNT_BLOCKED_FOR_5S);
        }
    }
}