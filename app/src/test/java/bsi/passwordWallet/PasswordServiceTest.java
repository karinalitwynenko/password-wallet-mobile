package bsi.passwordWallet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class PasswordServiceTest {

    @Mock
    Validation validationMock;
    @Mock
    Encryption encryptionMock;
    @Mock
    DataAccess dataAccessMock;

    @InjectMocks
    @Spy
    PasswordService passwordService = new PasswordService(encryptionMock);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        encryptionMock.setCipher(new Encryption.CipherWrapper());
        passwordService.setDataAccess(dataAccessMock);
    }

//    @Test
//    public void addPassword_ReturnsNewPassword_IfCreated() {
//        User testUser = new User(1L, "userLogin", Encryption.SHA512, "pass", "testSalt");
//        when(
//                dataAccessMock.createPassword(
//                        anyLong(),
//                        anyString(),
//                        any(),
//                        anyString(),
//                        anyString(),
//                        anyString()
//                )
//        ).thenReturn(new Password(1, 1, "testLogin", "testPassword", "testIV", "exampleWebsite", "test"));
//
//        //doReturn("").when(passwordService).validate(anyString(), anyString(), anyString());
//
//        when(encryptionMock.randomIV()).thenReturn(new byte[]{1, 1, 1, 1});
//
//        HashMap<String, String> passwordParams = new HashMap<>();
//        passwordParams.put(DataAccess.LOGIN, "testLogin");
//        passwordParams.put(DataAccess.PASSWORD, "testPasswd");
//        passwordParams.put(DataAccess.WEBSITE, "testWebsite");
//        passwordParams.put(DataAccess.DESCRIPTION, "testDesc");
//
//        Password password = null;
//        try {
//            password = passwordService.addPassword(testUser, new byte[]{2, 3, 4}, passwordParams);
//        } catch (PasswordService.PasswordCreationException e) {
//            e.printStackTrace();
//        }
//
//        assertNotNull(password);
//    }

    @Test
    public void addPassword_ThrowsException_IfValidationFailed() throws PasswordService.PasswordCreationException {
        thrown.expect(PasswordService.PasswordCreationException.class);
        when(validationMock.validateLogin(anyString())).thenReturn("test validation error");

        HashMap<String, String> passwordParams = new HashMap<>();
        passwordParams.put(DataAccess.LOGIN, "testLogin");
        passwordParams.put(DataAccess.PASSWORD, "testPasswd");
        passwordParams.put(DataAccess.WEBSITE, "testWebsite");
        passwordParams.put(DataAccess.DESCRIPTION, "testDesc");

        passwordService.addPassword(new User(), new byte[]{2, 3, 4}, passwordParams);
    }

    @Test
    public void addPassword_ThrowsException_IfPasswordCreationFailed() throws PasswordService.PasswordCreationException {
        User testUser = new User(1L, "userLogin", Encryption.SHA512, "pass", "testSalt");
//        when(
//                dataAccessMock.createPassword(
//                        anyLong(),
//                        anyString(),
//                        anyString(),
//                        anyString(),
//                        anyString(),
//                        anyString()
//                )
//        ).thenReturn(null);

        thrown.expect(PasswordService.PasswordCreationException.class);
        thrown.expectMessage(PasswordService.COULD_NOT_CREATE);

        doReturn("").when(passwordService).validate(anyString(), anyString(), anyString());
        when(encryptionMock.randomIV()).thenReturn(new byte[]{1, 1, 1, 1});

        HashMap<String, String> passwordParams = new HashMap<>();
        passwordParams.put(DataAccess.LOGIN, "testLogin");
        passwordParams.put(DataAccess.PASSWORD, "testPasswd");
        passwordParams.put(DataAccess.WEBSITE, "testWebsite");
        passwordParams.put(DataAccess.DESCRIPTION, "testDesc");

        Password p = passwordService.addPassword(testUser, new byte[]{2, 3, 4}, passwordParams);
        assertNull(p);
    }

    @Test
    public void validate_ReturnsErrorMessage_IfDataInvalid() {
        when(validationMock.validatePassword(anyString())).thenReturn("test validation error");
        String result = passwordService.validate("testLogin", "testPasswd", "testWebsite");
        assertEquals("test validation error", result);
    }

    @Test
    public void validate_ReturnsEmptyMessage_IfDataValid() {
        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validateWebsite(anyString())).thenReturn("");

        String result = passwordService.validate("testLogin", "testPasswd", "testWebsite");
        assertEquals("", result);
    }

    @Test
    public void updatePasswords_ReturnsTrue_IfPasswordsUpdated() {
        ArrayList<Password> passwords = new ArrayList<>();
        passwords.add(new Password(1, 1, "log", "pass", "ivtest", "website", "testDesc"));
        passwords.add(new Password(2, 1, "log2", "pass2", "IVtest", "12website", ""));
        when(encryptionMock.randomIV()).thenReturn(new byte[]{1, 1, 1, 1});
        when(dataAccessMock.updatePasswords(passwords)).thenReturn(true);
        when(encryptionMock.decryptAES128(anyString(), any(SecretKeySpec.class), any(IvParameterSpec.class))).thenReturn("decrypted test");
        boolean result = passwordService.updatePasswords(passwords, new byte[]{1, 2}, new byte[]{2, 3});

        verify(encryptionMock, times(2)).decryptAES128(anyString(), any(SecretKeySpec.class), any(IvParameterSpec.class));
        verify(encryptionMock, times(2)).encryptAES128(anyString(), any(SecretKeySpec.class), any(IvParameterSpec.class));

        assertTrue(result);
    }

}