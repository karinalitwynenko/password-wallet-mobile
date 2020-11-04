package bsi.passwordWallet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    Validation validationMock;
    @Mock
    Encryption encryptionMock;
    @Mock
    DataAccess dataAccessMock;

    @InjectMocks
    UserService userService = new UserService();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {

    }

    @Test
    public void updatePassword_ReturnsNewPassword_IfPasswordUpdated() {
        User user = new User(1, "userLogin", Encryption.SHA512, "testpasswordhash", "testsalt");
        String oldPassword = "testOldPassword";
        String newPassword = "testNewPassword";
        String masterPassword = "testMasterPassword";

        when(validationMock.validatePassword(oldPassword)).thenReturn("");
        when(validationMock.validatePassword(newPassword)).thenReturn("");
    }
}