package bsi.passwordWallet.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;

import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.Encryption;
import bsi.passwordWallet.LoginLog;
import bsi.passwordWallet.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LogServiceTest {
    @Mock
    DataAccess dataAccessMock;

    @InjectMocks
    LogService logService = new LogService();

    /**
     * TDD
     */
    @Test
    public void checkUserIP_ThrowsUserAccountException_IfUserIpBlocked() {
        User user = new User(
                1,
                "userLogin",
                Encryption.SHA512,
                "testpasswordhash", "testsalt"
        );

        ArrayList<String> blockedIPs = new ArrayList<>();
        blockedIPs.add("10.2.10.5");
        blockedIPs.add("10.0.10.10");
        blockedIPs.add("10.0.10.2");
        when(dataAccessMock.getBlockedIPs(user.getId())).thenReturn(blockedIPs);

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> logService.checkUserIP(user, "10.0.10.10", new Date())
        );

        assertEquals(LogService.IP_BLOCKED_PERMANENTLY, thrown.getMessage());
    }

    @Test
    public void checkUserIP_ThrowsUserAccountException_If2FailsInRowAnd5SecondsBlockadeDidNotPass() {
        User user = new User(
                1,
                "userLogin",
                Encryption.SHA512,
                "testpasswordhash", "testsalt"
        );

        ArrayList<LoginLog> logs = new ArrayList<>();
        logs.add(new LoginLog(1, 1, "10.0.10.10", 10000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(2, 1, "10.0.10.10", 9000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(3, 1, "10.0.10.10", 8000, LogService.LOGIN_SUCCESS, 0));
        logs.add(new LoginLog(4, 1, "10.0.10.10", 200, LogService.LOGIN_FAIL, 0));

        when(dataAccessMock.getBlockedIPs(anyLong())).thenReturn(new ArrayList<>());
        when(dataAccessMock.getLoginLogs(anyLong(), anyString(), anyInt())).thenReturn(logs);

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> logService.checkUserIP(user, "10.0.10.10", new Date(14999))
        );

        assertEquals(LogService.IP_BLOCKED_FOR_5S, thrown.getMessage());
    }

    @Test
    public void checkUserIP_ThrowsUserAccountException_If3FailsInRowAnd10SecondsBlockadeDidNotPass() {
        User user = new User(
                1,
                "userLogin",
                Encryption.SHA512,
                "testpasswordhash", "testsalt"
        );

        ArrayList<LoginLog> logs = new ArrayList<>();
        logs.add(new LoginLog(1, 1, "10.0.10.10", 10000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(2, 1, "10.0.10.10", 9000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(4, 1, "10.0.10.10", 6000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(3, 1, "10.0.10.10", 5262, LogService.LOGIN_SUCCESS, 0));
        logs.add(new LoginLog(4, 1, "10.0.10.10", 2262, LogService.LOGIN_FAIL, 0));

        when(dataAccessMock.getBlockedIPs(anyLong())).thenReturn(new ArrayList<>());
        when(dataAccessMock.getLoginLogs(anyLong(), anyString(), anyInt())).thenReturn(logs);

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> logService.checkUserIP(user, "10.0.10.10", new Date(19999))
        );

        assertEquals(LogService.IP_BLOCKED_FOR_10S, thrown.getMessage());
    }

    @Test
    public void checkUserIP_ReturnsTrue_IfFailSequenceLengthGreaterEqual3AndTimeBlockadePassed() {
        User user = new User(
                1,
                "userLogin",
                Encryption.SHA512,
                "testpasswordhash", "testsalt"
        );

        ArrayList<LoginLog> logs = new ArrayList<>();
        logs.add(new LoginLog(1, 1, "10.0.10.10", 10000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(2, 1, "10.0.10.10", 9000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(4, 1, "10.0.10.10", 6000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(3, 1, "10.0.10.10", 5262, LogService.LOGIN_SUCCESS, 0));
        logs.add(new LoginLog(4, 1, "10.0.10.10", 2262, LogService.LOGIN_FAIL, 0));

        when(dataAccessMock.getBlockedIPs(anyLong())).thenReturn(new ArrayList<>());
        when(dataAccessMock.getLoginLogs(anyLong(), anyString(), anyInt())).thenReturn(logs);

        try {
            assertTrue(logService.checkUserIP(user, "10.0.10.10", new Date(20000)));
        } catch (UserService.UserAccountException e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void checkUserIP_ReturnsFalse_IfFailSequenceLengthLessThan3AndTimeBlockadePassed() {
        User user = new User(
                1,
                "userLogin",
                Encryption.SHA512,
                "testpasswordhash", "testsalt"
        );

        ArrayList<LoginLog> logs = new ArrayList<>();
        logs.add(new LoginLog(1, 1, "10.0.10.10", 10000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(2, 1, "10.0.10.10", 9000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(4, 1, "10.0.10.10", 6000, LogService.LOGIN_SUCCESS, 0));
        logs.add(new LoginLog(3, 1, "10.0.10.10", 5262, LogService.LOGIN_SUCCESS, 0));
        logs.add(new LoginLog(4, 1, "10.0.10.10", 2262, LogService.LOGIN_FAIL, 0));

        when(dataAccessMock.getBlockedIPs(anyLong())).thenReturn(new ArrayList<>());
        when(dataAccessMock.getLoginLogs(anyLong(), anyString(), anyInt())).thenReturn(logs);

        try {
            assertFalse(logService.checkUserIP(user, "10.0.10.10", new Date(20000)));
        } catch (UserService.UserAccountException e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void checkUserIP_ReturnsFalse_IfFailSequenceSetToIgnored() {
        User user = new User(
                1,
                "userLogin",
                Encryption.SHA512,
                "testpasswordhash", "testsalt"
        );

        ArrayList<LoginLog> logs = new ArrayList<>();
        logs.add(new LoginLog(1, 1, "10.0.10.10", 10000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(2, 1, "10.0.10.10", 9000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(4, 1, "10.0.10.10", 6000, LogService.LOGIN_FAIL, 1));
        logs.add(new LoginLog(3, 1, "10.0.10.10", 5262, LogService.LOGIN_FAIL, 1));

        when(dataAccessMock.getBlockedIPs(anyLong())).thenReturn(new ArrayList<>());
        when(dataAccessMock.getLoginLogs(anyLong(), anyString(), anyInt())).thenReturn(logs);

        try {
            assertFalse(logService.checkUserIP(user, "10.0.10.10", new Date(20000)));
        } catch (UserService.UserAccountException e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void checkUserAccount_ThrowsUserAccountException_IfFourOrMoreFailsInRowAnd2MinutesBlockadeDidNotPass() {
        User user = new User(
                1,
                "userLogin",
                Encryption.SHA512,
                "testpasswordhash", "testsalt"
        );

        ArrayList<LoginLog> logs = new ArrayList<>();
        logs.add(new LoginLog(1, 1, "10.0.2.10", 10000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(2, 1, "10.0.10.3", 9000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(4, 1, "10.0.4.4", 6000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(3, 1, "10.0.5.5", 5262, LogService.LOGIN_FAIL, 0));

        when(dataAccessMock.getLoginLogs(anyLong(), any(), anyInt())).thenReturn(logs);

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> logService.checkUserAccount(user, new Date(129999))
        );

        assertEquals(LogService.ACCOUNT_BLOCKED_FOR_2M, thrown.getMessage());
    }

    @Test
    public void checkUserAccount_ThrowsUserAccountException_IfThreeFailsInRowAnd10SecondsBlockadeDidNotPass() {
        User user = new User(
                1,
                "userLogin",
                Encryption.SHA512,
                "testpasswordhash", "testsalt"
        );

        ArrayList<LoginLog> logs = new ArrayList<>();
        logs.add(new LoginLog(1, 1, "10.0.2.10", 10000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(2, 1, "10.0.10.3", 9000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(4, 1, "10.0.4.4", 6000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(3, 1, "10.0.5.5", 5262, LogService.LOGIN_SUCCESS, 0));

        when(dataAccessMock.getLoginLogs(anyLong(), any(), anyInt())).thenReturn(logs);

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> logService.checkUserAccount(user, new Date(19999))
        );

        assertEquals(LogService.ACCOUNT_BLOCKED_FOR_10S, thrown.getMessage());
    }

    @Test
    public void checkUserAccount_ThrowsUserAccountException_IfTwoFailsInRowAnd5SecondsBlockadeDidNotPass() {
        User user = new User(
                1,
                "userLogin",
                Encryption.SHA512,
                "testpasswordhash", "testsalt"
        );

        ArrayList<LoginLog> logs = new ArrayList<>();
        logs.add(new LoginLog(1, 1, "10.0.2.10", 10000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(2, 1, "10.0.10.3", 9000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(4, 1, "10.0.4.4", 6000, LogService.LOGIN_SUCCESS, 0));
        logs.add(new LoginLog(3, 1, "10.0.5.5", 5262, LogService.LOGIN_FAIL, 0));

        when(dataAccessMock.getLoginLogs(anyLong(), any(), anyInt())).thenReturn(logs);

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> logService.checkUserAccount(user, new Date(14999))
        );

        assertEquals(LogService.ACCOUNT_BLOCKED_FOR_5S, thrown.getMessage());
    }

    @Test
    public void checkUserAccount_DoesNotThrowAnyException_IfFailSequenceLessThan2() {
        User user = new User(
                1,
                "userLogin",
                Encryption.SHA512,
                "testpasswordhash", "testsalt"
        );

        ArrayList<LoginLog> logs = new ArrayList<>();
        logs.add(new LoginLog(1, 1, "10.0.2.10", 10000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(2, 1, "10.0.10.3", 9000, LogService.LOGIN_SUCCESS, 0));
        logs.add(new LoginLog(4, 1, "10.0.4.4", 6000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(3, 1, "10.0.5.5", 5262, LogService.LOGIN_FAIL, 0));

        when(dataAccessMock.getLoginLogs(anyLong(), any(), anyInt())).thenReturn(logs);

        assertDoesNotThrow(() -> logService.checkUserAccount(user, new Date(11000)));
    }

    @Test
    public void checkUserAccount_DoesNotThrowAnyException_IfTimeBlockadePassed() {
        User user = new User(
                1,
                "userLogin",
                Encryption.SHA512,
                "testpasswordhash", "testsalt"
        );

        ArrayList<LoginLog> logs = new ArrayList<>();
        logs.add(new LoginLog(1, 1, "10.0.2.10", 10000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(2, 1, "10.0.10.3", 9000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(4, 1, "10.0.4.4", 6000, LogService.LOGIN_FAIL, 0));
        logs.add(new LoginLog(3, 1, "10.0.5.5", 5262, LogService.LOGIN_SUCCESS, 0));

        when(dataAccessMock.getLoginLogs(anyLong(), any(), anyInt())).thenReturn(logs);

        assertDoesNotThrow(() -> logService.checkUserAccount(user, new Date(20000)));
    }

}