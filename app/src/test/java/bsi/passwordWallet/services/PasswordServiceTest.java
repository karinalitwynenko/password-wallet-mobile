package bsi.passwordWallet.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;

import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.Encryption;
import bsi.passwordWallet.Password;
import bsi.passwordWallet.User;
import bsi.passwordWallet.Validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PasswordServiceTest {
    @Mock
    Validation validationMock;
    @Mock
    Encryption encryptionMock;
    @Mock
    DataAccess dataAccessMock;

    @InjectMocks
    PasswordService passwordService = new PasswordService();

    @Test
    public void addPassword_ReturnsNewPassword_IfCreated() {
        User testUser = new User(
                1L,
                "testUserLogin",
                Encryption.SHA512,
                "testUserPassword",
                "testSalt"
        );

        when(
                dataAccessMock.createPassword(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString())
        ).thenReturn(new Password());

        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validateWebsite(anyString())).thenReturn("");
        when(encryptionMock.randomIV()).thenReturn("testIV".getBytes());
        when(
                encryptionMock.encryptAES128(anyString(), any(byte[].class), any(byte[].class))
        ).thenReturn("testEncrypted");

        HashMap<String, String> passwordParams = new HashMap<>();
        passwordParams.put(Password.LOGIN, "testLogin");
        passwordParams.put(Password.PASSWORD, "testPassword");
        passwordParams.put(Password.WEBSITE, "testWebsite");
        passwordParams.put(Password.DESCRIPTION, "testDescription");

        Password password = null;
        try {
            password = passwordService.addPassword(testUser, "testMasterPass".getBytes(), passwordParams);
        } catch (PasswordService.PasswordCreationException e) {
            e.printStackTrace();
        }

        verify(
                validationMock, times(1)
        ).validatePassword(passwordParams.get(Password.PASSWORD));

        verify(
                validationMock, times(1)
        ).validateLogin(passwordParams.get(Password.LOGIN));

        verify(
                validationMock, times(1)
        ).validateWebsite(passwordParams.get(Password.WEBSITE));

        verify(encryptionMock, times(1)).randomIV();

        verify(
                encryptionMock, times(1)
        ).encryptAES128("testPassword", "testMasterPass".getBytes(), "testIV".getBytes());

        verify(
                dataAccessMock, times(1)
        ).createPassword(
                testUser.getId(),
                passwordParams.get(Password.LOGIN),
                "testEncrypted",
                "dGVzdElW",
                passwordParams.get(Password.WEBSITE),
                passwordParams.get(Password.DESCRIPTION)
        );

        assertNotNull(password);
    }

    @Test
    public void addPassword_ThrowsPasswordCreationException_IfLoginValidationFailed() {
        HashMap<String, String> passwordParams = new HashMap<>();
        passwordParams.put(Password.LOGIN, "");
        passwordParams.put(Password.PASSWORD, "testPassword");
        passwordParams.put(Password.WEBSITE, "testWebsite");
        passwordParams.put(Password.DESCRIPTION, "testDescription");

        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(validationMock.validateLogin(anyString())).thenReturn("invalid login");
        when(validationMock.validateWebsite(anyString())).thenReturn("");

        PasswordService.PasswordCreationException thrown = assertThrows(
                PasswordService.PasswordCreationException.class,
                () ->  passwordService.addPassword(new User(), "testMasterPass".getBytes(), passwordParams)
        );

        verify(
                validationMock, times(1)
        ).validatePassword(passwordParams.get(Password.PASSWORD));

        verify(
                validationMock, times(1)
        ).validateLogin(passwordParams.get(Password.LOGIN));

        verify(
                validationMock, times(1)
        ).validateWebsite(passwordParams.get(Password.WEBSITE));

        assertEquals("invalid login", thrown.getMessage());
    }

    @Test
    public void addPassword_ThrowsPasswordCreationException_IfPasswordValidationFailed() {
        HashMap<String, String> passwordParams = new HashMap<>();
        passwordParams.put(Password.LOGIN, "");
        passwordParams.put(Password.PASSWORD, "testPassword");
        passwordParams.put(Password.WEBSITE, "testWebsite");
        passwordParams.put(Password.DESCRIPTION, "testDescription");

        when(validationMock.validatePassword(anyString())).thenReturn("invalid password");
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validateWebsite(anyString())).thenReturn("");

        PasswordService.PasswordCreationException thrown = assertThrows(
                PasswordService.PasswordCreationException.class,
                () ->  passwordService.addPassword(new User(), "testMasterPass".getBytes(), passwordParams)
        );

        verify(
                validationMock, times(1)
        ).validatePassword(passwordParams.get(Password.PASSWORD));

        verify(
                validationMock, times(1)
        ).validateLogin(passwordParams.get(Password.LOGIN));

        verify(
                validationMock, times(1)
        ).validateWebsite(passwordParams.get(Password.WEBSITE));

        assertEquals("invalid password", thrown.getMessage());
    }

    @Test
    public void addPassword_ThrowsPasswordCreationException_IfWebsiteValidationFailed() {
        HashMap<String, String> passwordParams = new HashMap<>();
        passwordParams.put(Password.LOGIN, "");
        passwordParams.put(Password.PASSWORD, "testPassword");
        passwordParams.put(Password.WEBSITE, "testWebsite");
        passwordParams.put(Password.DESCRIPTION, "testDescription");

        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validateWebsite(anyString())).thenReturn("invalid website");

        PasswordService.PasswordCreationException thrown = assertThrows(
                PasswordService.PasswordCreationException.class,
                () ->  passwordService.addPassword(new User(), "testMasterPass".getBytes(), passwordParams)
        );

        verify(
                validationMock, times(1)
        ).validatePassword(passwordParams.get(Password.PASSWORD));

        verify(
                validationMock, times(1)
        ).validateLogin(passwordParams.get(Password.LOGIN));

        verify(
                validationMock, times(1)
        ).validateWebsite(passwordParams.get(Password.WEBSITE));

        assertEquals("invalid website", thrown.getMessage());
    }

    @Test
    public void addPassword_ThrowsPasswordCreationException_IfPasswordCreationFailed() {
        User testUser = new User(
                1L,
                "testUserLogin",
                Encryption.SHA512,
                "testUserPassword",
                "testSalt"
        );

        HashMap<String, String> passwordParams = new HashMap<>();
        passwordParams.put(Password.LOGIN, "testLogin");
        passwordParams.put(Password.PASSWORD, "testPassword");
        passwordParams.put(Password.WEBSITE, "testWebsite");
        passwordParams.put(Password.WEBSITE, "testDescription");

        when(validationMock.validateLogin(anyString())).thenReturn("");
        when(validationMock.validatePassword(anyString())).thenReturn("");
        when(validationMock.validateWebsite(anyString())).thenReturn("");
        when(encryptionMock.randomIV()).thenReturn("testIV".getBytes());

        when(
                encryptionMock.encryptAES128(anyString(), any(byte[].class), any(byte[].class))
        ).thenReturn("testEncrypted");

        PasswordService.PasswordCreationException thrown = assertThrows(
                PasswordService.PasswordCreationException.class,
                () ->  passwordService.addPassword(testUser, "testMasterPass".getBytes(), passwordParams)
        );

        verify(
                validationMock, times(1)
        ).validatePassword(passwordParams.get(Password.PASSWORD));

        verify(
                validationMock, times(1)
        ).validateLogin(passwordParams.get(Password.LOGIN));

        verify(
                validationMock, times(1)
        ).validateWebsite(passwordParams.get(Password.WEBSITE));

        verify(encryptionMock, times(1)).randomIV();

        verify(
                encryptionMock, times(1)
        ).encryptAES128(
                passwordParams.get(Password.PASSWORD), "testMasterPass".getBytes(), "testIV".getBytes()
        );

        verify(
                dataAccessMock, times(1)
        ).createPassword(
                testUser.getId(),
                passwordParams.get(Password.LOGIN),
                "testEncrypted",
                "dGVzdElW",
                passwordParams.get(Password.WEBSITE),
                passwordParams.get(Password.DESCRIPTION)
        );

        assertEquals(PasswordService.COULD_NOT_CREATE, thrown.getMessage());
    }

    @Test
    public void updatePasswords_ReturnsTrue_IfPasswordsUpdated() {
        ArrayList<Password> passwords = new ArrayList<>();
        passwords.add(
                new Password(
                        1,
                        1,
                        "log",
                        "encryptedPassword1",
                        "tiv",
                        "website",
                        "testDesc"
                )
        );

        passwords.add(
                new Password(
                        2,
                        1,
                        "log2",
                        "encryptedPassword2",
                        "tiv",
                        "website",
                        "testDesc"
                )
        );

        when(encryptionMock.randomIV()).thenReturn("testIV".getBytes());
        when(dataAccessMock.updatePasswords(any(ArrayList.class))).thenReturn(true);
        when(
                encryptionMock.decryptAES128(anyString(), any(byte[].class), any(byte[].class))
        ).thenReturn("decryptedPassword1").thenReturn("decryptedPassword2");

        when(
                encryptionMock.encryptAES128(anyString(), any(byte[].class), any(byte[].class))
        ).thenReturn("newEncryptedPassword1").thenReturn("newEncryptedPassword2");


        boolean result = passwordService.updatePasswords(
                passwords, "testMasterPassword".getBytes(), "testMasterPassword2".getBytes()
        );

        verify(encryptionMock, times(passwords.size())).randomIV();

        verify(
                encryptionMock,
                times(1)
        ).decryptAES128("encryptedPassword1", "testMasterPassword".getBytes(), new byte[]{(byte) 0xB6, (byte) 0x2B});

        verify(
                encryptionMock,
                times(1)
        ).decryptAES128("encryptedPassword2", "testMasterPassword".getBytes(), new byte[]{(byte) 0xB6, (byte) 0x2B});

        verify(
                encryptionMock,
                times(1)
        ).encryptAES128("decryptedPassword1", "testMasterPassword2".getBytes(), "testIV".getBytes());

        verify(
                encryptionMock,
                times(1)
        ).encryptAES128("decryptedPassword2", "testMasterPassword2".getBytes(), "testIV".getBytes());

        verify(dataAccessMock, times(1)).updatePasswords(passwords);

        assertEquals("newEncryptedPassword1", passwords.get(0).getPassword());
        assertEquals("newEncryptedPassword2", passwords.get(1).getPassword());

        assertEquals("dGVzdElW", passwords.get(0).getIV());
        assertEquals("dGVzdElW", passwords.get(1).getIV());
        assertTrue(result);
    }

}