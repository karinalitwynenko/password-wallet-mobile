package bsi.passwordWallet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import bsi.passwordWallet.services.UserService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
    @Mock
    DataAccess dataAccessMock;
    @Mock
    Validation validationMock;
    @Mock
    Encryption encryptionMock;

    @InjectMocks
    UserService userService = new UserService();

    @Test
    public void updatePassword_ReturnsNewSHA512Password_IfPasswordUpdated() {
        User user = new User(
                1,
                "userLogin",
                Encryption.SHA512,
                "testpasswordhash", "testsalt"
        );

        String oldPassword = "testMasterPassword";
        String newPassword = "testNewMasterPassword";
        String currentMasterPassword = "testMasterPassword";

        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(encryptionMock.generateSalt64()).thenReturn("testSalt");
        when(encryptionMock.calculateSHA512(anyString(), anyString())).thenReturn("testSha512Hash");
        when(
                dataAccessMock.updateUserMasterPassword(
                        user.getId(), "testSha512Hash", "testSalt"
                )
        ).thenReturn(true);

        String resultPassword = "";

        try {
            resultPassword = userService.updatePassword(user, oldPassword, newPassword, currentMasterPassword);
        } catch (UserService.UserAccountException e) {
            e.printStackTrace();
        }

        verify(validationMock).validatePassword(oldPassword);
        verify(validationMock).validatePassword(newPassword);
        verify(encryptionMock).generateSalt64();
        verify(encryptionMock).calculateSHA512(newPassword, "testSalt");
        verify(dataAccessMock).updateUserMasterPassword(user.getId(), "testSha512Hash", "testSalt");

        assertEquals(newPassword, resultPassword);
    }

    @Test
    public void updatePassword_ThrowsUserAccountException_IfPasswordCouldNotBeUpdated() {
        User user = new User(
                1,
                "userLogin",
                Encryption.SHA512,
                "testpasswordhash", "testsalt"
        );

        String oldPassword = "testMasterPassword";
        String newPassword = "testNewMasterPassword";
        String currentMasterPassword = "testMasterPassword";

        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(encryptionMock.generateSalt64()).thenReturn("testSalt");
        when(encryptionMock.calculateSHA512(anyString(), anyString())).thenReturn("testSha512Hash");

        when(
                dataAccessMock.updateUserMasterPassword(
                        user.getId(), "testSha512Hash", "testSalt"
                )
        ).thenReturn(false);

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.updatePassword(user, oldPassword, newPassword, currentMasterPassword)
        );

        verify(validationMock).validatePassword(oldPassword);
        verify(validationMock).validatePassword(newPassword);
        verify(encryptionMock).generateSalt64();
        verify(encryptionMock).calculateSHA512(newPassword, "testSalt");
        verify(dataAccessMock).updateUserMasterPassword(user.getId(), "testSha512Hash", "testSalt");

        assertEquals("Could not change user's password", thrown.getMessage());
    }

    @Test
    public void updatePassword_ReturnsNewHmacPassword_IfPasswordUpdated() {
        User user = new User(
                1,
                "userLogin",
                Encryption.HMAC_SHA512,
                "testpasswordhash", "testsalt"
        );

        String oldPassword = "testMasterPassword";
        String newPassword = "testNewMasterPassword";
        String currentMasterPassword = "testMasterPassword";

        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(encryptionMock.generateSalt64()).thenReturn("testSalt");
        when(encryptionMock.calculateHMAC(anyString(), anyString())).thenReturn("testHmacHash");
        when(
                dataAccessMock.updateUserMasterPassword(
                        user.getId(), "testHmacHash", "testSalt"
                )
        ).thenReturn(true);

        String resultPassword = "";

        try {
            resultPassword = userService.updatePassword(user, oldPassword, newPassword, currentMasterPassword);
        } catch (UserService.UserAccountException e) {
            e.printStackTrace();
        }

        verify(validationMock).validatePassword(oldPassword);
        verify(validationMock).validatePassword(newPassword);
        verify(encryptionMock).generateSalt64();
        verify(encryptionMock).calculateHMAC(newPassword, "testSalt");
        verify(dataAccessMock).updateUserMasterPassword(user.getId(), "testHmacHash", "testSalt");

        assertEquals(newPassword, resultPassword);
    }

    @Test
    public void updatePassword_ThrowsUserAccountException_IfOldPasswordValidationFailed() {
        when(validationMock.validatePassword(anyString())).thenReturn(Validation.PASSWORD_CANT_BE_EMPTY);

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.updatePassword(new User(), "", "testp1", "testp2")
        );

        verify(validationMock).validatePassword("");
        verifyNoMoreInteractions(validationMock);
        assertEquals(Validation.PASSWORD_CANT_BE_EMPTY, thrown.getMessage());
    }

    @Test
    public void updatePassword_ThrowsUserAccountException_IfNewPasswordValidationFailed() {
        when(
                validationMock.validatePassword(anyString())
        ).thenReturn("").thenReturn(Validation.PASSWORD_CANT_BE_LONGER_THAN);

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.updatePassword(new User(), "testp1", "invalidTestPass", "")
        );

        verify(validationMock).validatePassword("testp1");
        verify(validationMock).validatePassword("invalidTestPass");
        verifyNoMoreInteractions(validationMock);
        assertEquals(Validation.PASSWORD_CANT_BE_LONGER_THAN, thrown.getMessage());
    }

    @Test
    public void updatePassword_ThrowsUserAccountException_IfPasswordsAreTheSame() {
        when(validationMock.validatePassword(anyString())).thenReturn("");

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.updatePassword(
                        new User(),
                        "testpassword",
                        "testpassword",
                        ""
                )
        );

        verify(validationMock, times(2)).validatePassword("testpassword");
        verifyNoMoreInteractions(validationMock);
        assertEquals("New password is the same as the current one.", thrown.getMessage());
    }

    @Test
    public void updatePassword_ThrowsUserAccountException_IfUserPasswordIncorrect() {
        when(validationMock.validatePassword(anyString())).thenReturn("");

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.updatePassword(
                        new User(),
                        "testpassword",
                        "testnewpassword",
                        "masterpassw"
                )
        );

        verify(validationMock).validatePassword("testpassword");
        verify(validationMock).validatePassword("testnewpassword");
        assertEquals("Incorrect user password", thrown.getMessage());
    }

    @Test
    public void signIn_ThrowsUserAccountException_IfLoginValidationFailed() {
        when(validationMock.validateLogin(anyString())).thenReturn("invalid login");

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.signIn(
                        "testlogin",
                        "testpasswd"
                )
        );

        verify(validationMock).validateLogin("testlogin");
        verifyNoMoreInteractions(validationMock);
        assertEquals("invalid login", thrown.getMessage());
    }

    @Test
    public void signIn_ThrowsUserAccountException_IfPasswordValidationFailed() {
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validatePassword(anyString())).thenReturn("invalid password");

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.signIn(
                        "testlogin",
                        "testpasswd"
                )
        );

        verify(validationMock).validateLogin("testlogin");
        verify(validationMock).validatePassword("testpasswd");

        verifyNoMoreInteractions(validationMock);
        assertEquals("invalid password", thrown.getMessage());
    }

    @Test
    public void signIn_ThrowsUserAccountException_IfUserNotExist() {
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(dataAccessMock.getUser(anyString())).thenReturn(null);

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.signIn(
                        "testlogin",
                        "testpasswd"
                )
        );

        verify(validationMock).validateLogin("testlogin");
        verify(validationMock).validatePassword("testpasswd");
        verify(dataAccessMock).getUser("testlogin");

        assertEquals(UserService.USER_DOES_NOT_EXIST, thrown.getMessage());
    }

    @Test
    public void signIn_ReturnsUserWithSHA512Hash_IfUserExists() {
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(dataAccessMock.getUser(anyString())).thenReturn(
                new User(
                        1L,
                        "testlogin",
                        Encryption.SHA512,
                        "testpasswordhash",
                        "testsalt"
                )
        );

        when(encryptionMock.calculateSHA512(anyString(), anyString())).thenReturn("testpasswordhash");

        User user = null;
        try {
            user = userService.signIn("testlogin", "testpass");
        } catch (UserService.UserAccountException e) {
            fail("Unexpected exception");
        }

        verify(validationMock).validateLogin("testlogin");
        verify(validationMock).validatePassword("testpass");
        verify(dataAccessMock).getUser("testlogin");
        verify(encryptionMock).calculateSHA512("testpass", "testsalt");
        assertNotNull(user);
    }

    @Test
    public void signIn_ReturnsUserWithHmacHash_IfUserExists() {
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(dataAccessMock.getUser(anyString())).thenReturn(
                new User(
                        1L,
                        "testlogin",
                        Encryption.HMAC_SHA512,
                        "testpasswordhash",
                        "testsalt"
                )
        );

        when(encryptionMock.calculateHMAC(anyString(), anyString())).thenReturn("testpasswordhash");

        User user = null;
        try {
            user = userService.signIn("testlogin", "testpass");
        } catch (UserService.UserAccountException e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        verify(validationMock).validateLogin("testlogin");
        verify(validationMock).validatePassword("testpass");
        verify(dataAccessMock).getUser("testlogin");
        verify(encryptionMock).calculateHMAC("testpass", "testsalt");
        assertNotNull(user);
    }

    @Test
    public void signIn_ThrowsUserAccountException_IfUserPasswordIncorrect() {
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(dataAccessMock.getUser(anyString())).thenReturn(
                new User(
                        1L,
                        "testlogin",
                        Encryption.HMAC_SHA512,
                        "testpasswordhash123",
                        "testsalt"
                )
        );

        when(encryptionMock.calculateHMAC(anyString(), anyString())).thenReturn("testpasswordhash");

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.signIn(
                        "testlogin",
                        "testpass"
                )
        );

        verify(validationMock).validateLogin("testlogin");
        verify(validationMock).validatePassword("testpass");
        verify(dataAccessMock).getUser("testlogin");
        verify(encryptionMock).calculateHMAC("testpass", "testsalt");
        assertEquals(UserService.INCORRECT_PASSWORD, thrown.getMessage());
    }

    @Test
    public void signUp_ThrowsUserAccountException_IfLoginValidationFailed() {
        when(validationMock.validateLogin(anyString())).thenReturn("invalid login");
        when(validationMock.validatePassword(anyString())).thenReturn("");

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.signUp(
                        "testlogin",
                        "testpasswd",
                        "testpasswd",
                        Encryption.SHA512
                )
        );

        verify(validationMock).validateLogin("testlogin");
        verify(validationMock, times(2)).validatePassword("testpasswd");

        assertEquals("invalid login", thrown.getMessage());
    }

    @Test
    public void signUp_ThrowsUserAccountException_IfPasswordValidationFailed() {
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validatePassword(anyString())).thenReturn("").thenReturn("invalid password");

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.signUp(
                        "testlogin",
                        "testpasswd",
                        "testpasswd",
                        Encryption.SHA512
                )
        );

        verify(validationMock).validateLogin("testlogin");
        verify(validationMock, times(2)).validatePassword("testpasswd");

        assertEquals("invalid password", thrown.getMessage());
    }

    @Test
    public void signUp_ThrowsUserAccountException_IfConfirmPasswordValidationFailed() {
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validatePassword(anyString())).thenReturn("invalid password").thenReturn("");

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.signUp(
                        "testlogin",
                        "testpasswd",
                        "testpasswd",
                        Encryption.SHA512
                )
        );

        verify(validationMock).validateLogin("testlogin");
        verify(validationMock, times(2)).validatePassword("testpasswd");

        assertEquals("invalid password", thrown.getMessage());
    }

    @Test
    public void signUp_ThrowsUserAccountException_IfPasswordsDoNotMatch() {
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validatePassword(anyString())).thenReturn("");

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.signUp(
                        "testlogin",
                        "testpasswd123",
                        "testpasswd456",
                        Encryption.SHA512
                )
        );

        verify(validationMock).validateLogin("testlogin");
        verify(validationMock).validatePassword("testpasswd123");
        verify(validationMock).validatePassword("testpasswd456");

        assertEquals(UserService.PASSWORDS_DO_NOT_MATCH, thrown.getMessage());
    }

    @Test
    public void signUp_ThrowsUserAccountException_IfLoginAlreadyInUse() {
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(dataAccessMock.getUser(anyString())).thenReturn(new User());

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.signUp(
                        "testlogin",
                        "testpasswd",
                        "testpasswd",
                        Encryption.SHA512
                )
        );

        verify(validationMock).validateLogin("testlogin");
        verify(validationMock, times(2)).validatePassword("testpasswd");
        verify(dataAccessMock).getUser("testlogin");
        assertEquals(UserService.LOGIN_EXISTS, thrown.getMessage());
    }

    @Test
    public void signUp_ReturnsNewUserWithSHA512Hash_IfUserSuccessfullyCreated() {
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(dataAccessMock.getUser(anyString())).thenReturn(null);
        when(dataAccessMock.createUser(anyString(), anyString(), anyString(), anyString())).thenReturn(new User());

        when(encryptionMock.generateSalt64()).thenReturn("testsalt");
        when(encryptionMock.calculateSHA512(anyString(), anyString())).thenReturn("testSha512Hash");

        User user = null;
        try {
            user = userService.signUp(
                    "testlogin",
                    "testpasswd",
                    "testpasswd",
                    Encryption.SHA512
            );
        } catch (UserService.UserAccountException e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        verify(validationMock).validateLogin("testlogin");
        verify(validationMock, times(2)).validatePassword("testpasswd");
        verify(dataAccessMock).getUser("testlogin");
        verify(encryptionMock).generateSalt64();
        verify(encryptionMock).calculateSHA512("testpasswd", "testsalt");
        verify(dataAccessMock).createUser(
                "testlogin", Encryption.SHA512, "testSha512Hash", "testsalt"
        );
        assertNotNull(user);
    }

    @Test
    public void signUp_ReturnsNewUserWithHmacHash_IfUserSuccessfullyCreated() {
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(dataAccessMock.getUser(anyString())).thenReturn(null);
        when(dataAccessMock.createUser(anyString(), anyString(), anyString(), anyString())).thenReturn(new User());

        when(encryptionMock.generateSalt64()).thenReturn("testsalt");
        when(encryptionMock.calculateHMAC(anyString(), anyString())).thenReturn("testHmac2Hash");

        User user = null;
        try {
            user = userService.signUp(
                    "testlogin",
                    "testpasswd",
                    "testpasswd",
                    Encryption.HMAC_SHA512
            );
        } catch (UserService.UserAccountException e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        verify(validationMock).validateLogin("testlogin");
        verify(validationMock, times(2)).validatePassword("testpasswd");
        verify(dataAccessMock).getUser("testlogin");
        verify(encryptionMock).generateSalt64();
        verify(encryptionMock).calculateHMAC("testpasswd", "testsalt");
        verify(dataAccessMock).createUser(
                "testlogin", Encryption.HMAC_SHA512, "testHmac2Hash", "testsalt"
        );
        assertNotNull(user);
    }

    @Test
    public void signUp_ThrowsUserAccountException_IfUserCouldNotBeCreated() {
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(dataAccessMock.getUser(anyString())).thenReturn(null);
        when(dataAccessMock.createUser(anyString(), anyString(), anyString(), anyString())).thenReturn(null);

        when(encryptionMock.generateSalt64()).thenReturn("testsalt");
        when(encryptionMock.calculateHMAC(anyString(), anyString())).thenReturn("testHmac2Hash");

        UserService.UserAccountException thrown = assertThrows(
                UserService.UserAccountException.class,
                () -> userService.signUp(
                        "testlogin",
                        "testpasswd",
                        "testpasswd",
                        Encryption.HMAC_SHA512
                )
        );

        verify(validationMock).validateLogin("testlogin");
        verify(validationMock, times(2)).validatePassword("testpasswd");
        verify(dataAccessMock).getUser("testlogin");
        verify(encryptionMock).generateSalt64();
        verify(encryptionMock).calculateHMAC("testpasswd", "testsalt");
        verify(dataAccessMock).createUser(
                "testlogin", Encryption.HMAC_SHA512, "testHmac2Hash", "testsalt"
        );
        assertEquals(UserService.COULD_NOT_CREATE, thrown.getMessage());
    }

}