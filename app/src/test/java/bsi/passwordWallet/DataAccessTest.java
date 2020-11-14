package bsi.passwordWallet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataAccessTest {

    @Mock
    SQLiteDatabase databaseMock;
    @Mock
    ContentValues contentValuesMock;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    DataAccess dataAccess;

    @Before
    public void setUp() {
        DataAccess.initialize(databaseMock);
        dataAccess = new DataAccess();
        dataAccess.setContentValues(contentValuesMock);
    }

    @Test
    public void getUser_ReturnsNull_IfUserNotExist() {
        Cursor cursor = mock(Cursor.class);

        when(databaseMock.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.getCount()).thenReturn(0);

        User user = dataAccess.getUser("testLogin");

        verify(databaseMock)
                .rawQuery("select * from " + DataAccess.USER_TABLE + " where login = ?", new String[] {"testLogin"});
        verify(cursor).getCount();
        verifyNoMoreInteractions(cursor);
        assertNull(user);
    }

    @Test
    public void getUser_ReturnsUser_IfUserExists() {
        Cursor cursor = mock(Cursor.class);
        when(databaseMock.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.getCount()).thenReturn(1);
        when(cursor.getLong(anyInt())).thenReturn(1L);
        when(cursor.getString(anyInt())).thenReturn("test");

        User user = dataAccess.getUser("testLogin");

        verify(databaseMock)
                .rawQuery("select * from " + DataAccess.USER_TABLE + " where login = ?", new String[] {"testLogin"});
        verify(cursor).getCount();
        verify(cursor).moveToFirst();
        verify(cursor).getColumnIndex(User.USER_ID);
        verify(cursor).getColumnIndex(User.LOGIN);
        verify(cursor).getColumnIndex(User.ENCRYPTION_TYPE);
        verify(cursor).getColumnIndex(User.PASSWORD_HASH);
        verify(cursor).getColumnIndex(User.SALT);
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("test", user.getLogin());
        assertNotNull("test", user.getEncryptionMethod());
        assertNotNull("test", user.getPassword());
        assertNotNull("test", user.getSalt());
    }

    @Test
    public void createUser_ReturnsNewUser_IfCreated() {
        when(databaseMock.insert(anyString(), any(), any())).thenReturn(2L);

        User user = dataAccess.createUser(
                "testLogin", Encryption.HMAC_SHA512, "testHash", "testSalt"
        );

        verify(contentValuesMock).put(User.LOGIN, "testLogin");
        verify(contentValuesMock).put(User.ENCRYPTION_TYPE, Encryption.HMAC_SHA512);
        verify(contentValuesMock).put(User.PASSWORD_HASH, "testHash");
        verify(contentValuesMock).put(User.SALT, "testSalt");
        verify(databaseMock).insert(DataAccess.USER_TABLE, null, contentValuesMock);
        assertNotNull(user);
        assertEquals(2L, user.getId());
        assertEquals("testLogin", user.getLogin());
        assertNotNull(Encryption.HMAC_SHA512, user.getEncryptionMethod());
        assertNotNull("testHash", user.getPassword());
        assertNotNull("testSalt", user.getSalt());
    }

    @Test
    public void createUser_ReturnsNull_IfUserNotCreated() {
        when(databaseMock.insert(anyString(), any(), any())).thenReturn(-1L);

        User user = dataAccess.createUser(
                "testLogin", Encryption.HMAC_SHA512, "testHash", "testSalt"
        );

        verify(contentValuesMock).put(User.LOGIN, "testLogin");
        verify(contentValuesMock).put(User.ENCRYPTION_TYPE, Encryption.HMAC_SHA512);
        verify(contentValuesMock).put(User.PASSWORD_HASH, "testHash");
        verify(contentValuesMock).put(User.SALT, "testSalt");
        verify(databaseMock).insert(DataAccess.USER_TABLE, null, contentValuesMock);
        assertNull(user);
    }

    @Test
    public void updateUserMasterPassword_ReturnsTrue_IfPasswordUpdated() {
        when(databaseMock.update(anyString(), any(), anyString(), any())).thenReturn(1);

        boolean result =
                dataAccess.updateUserMasterPassword(1, "testPassword", "testSalt");

        verify(contentValuesMock).put(User.PASSWORD_HASH, "testPassword");
        verify(contentValuesMock).put(User.SALT, "testSalt");
        verify(databaseMock).beginTransaction();
        verify(databaseMock)
                .update(DataAccess.USER_TABLE, contentValuesMock, "user_id = ?", new String[] {"1"});
        assertTrue(result);
    }

    @Test
    public void updateUserMasterPassword_ReturnsFalse_IfPasswordNotUpdated() {
        when(databaseMock.update(anyString(), any(), anyString(), any())).thenReturn(-1);

        boolean result = dataAccess.updateUserMasterPassword(1, "testPassword", "testSalt");

        verify(contentValuesMock).put(User.PASSWORD_HASH, "testPassword");
        verify(contentValuesMock).put(User.SALT, "testSalt");
        verify(databaseMock).beginTransaction();
        verify(databaseMock)
                .update(DataAccess.USER_TABLE, contentValuesMock, "user_id = ?", new String[] {"1"});
        assertFalse(result);
    }

    @Test
    public void getPasswords_ReturnsNonEmptyPasswordList_IfPasswordsExist() {
        Cursor cursor = mock(Cursor.class);

        when(databaseMock.rawQuery(anyString(), any())).thenReturn(cursor);
        when(cursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(cursor.getLong(anyInt())).thenReturn(1L);
        when(cursor.getString(anyInt())).thenReturn("test");

        ArrayList<Password> passwords = dataAccess.getPasswords(1);

        verify(databaseMock)
                .rawQuery("select * from " + DataAccess.PASSWORD_TABLE + " where user_id = ?", new String[] {"1"});
        verify(cursor, times(3)).moveToNext();
        verify(cursor, times(4)).getLong(anyInt());
        verify(cursor, times(10)).getString(anyInt());
        assertEquals(2, passwords.size());
        assertNotNull(passwords.get(0));
        assertNotNull(passwords.get(1));
    }

    @Test
    public void getPasswords_ReturnsEmptyPasswordList_IfPasswordsDoNotExist() {
        Cursor cursor = mock(Cursor.class);
        when(databaseMock.rawQuery(anyString(), any())).thenReturn(cursor);
        when(cursor.moveToNext()).thenReturn(false);

        ArrayList<Password> passwords = dataAccess.getPasswords(1);

        verify(databaseMock)
                .rawQuery("select * from " + DataAccess.PASSWORD_TABLE + " where user_id = ?", new String[] {"1"});
        verify(cursor, times(1)).moveToNext();
        verifyNoMoreInteractions(cursor);
        assertNotNull(passwords);
        assertEquals(0, passwords.size());
    }

    @Test
    public void createPassword_ReturnsNewPassword_IfCreated() {
        when(databaseMock.insert(anyString(), any(), any())).thenReturn(1L);

        Password password = dataAccess.createPassword(
                1L,
                "testLogin",
                "passwd",
                "testIV",
                "testWebsite",
                "testDesc"
        );

        verify(contentValuesMock).put(Password.USER_ID, 1L);
        verify(contentValuesMock).put(Password.LOGIN, "testLogin");
        verify(contentValuesMock).put(Password.PASSWORD, "passwd");
        verify(contentValuesMock).put(Password.IV, "testIV");
        verify(contentValuesMock).put(Password.WEBSITE, "testWebsite");
        verify(contentValuesMock).put(Password.DESCRIPTION, "testDesc");
        verify(databaseMock).insert(DataAccess.PASSWORD_TABLE, null, contentValuesMock);
        assertEquals(1L, password.getId());
        assertEquals("testLogin", password.getLogin());
        assertEquals("passwd", password.getPassword());
        assertEquals("testIV", password.getIV());
        assertEquals("testWebsite", password.getWebsite());
        assertEquals("testDesc", password.getDescription());
        assertNotNull(password);
    }

    @Test
    public void createPassword_ReturnsNull_IfNotCreated() {
        when(databaseMock.insert(anyString(), any(), any())).thenReturn(-1L);

        Password password = dataAccess.createPassword(
                1L,
                "testLogin",
                "passwd",
                "testIV",
                "testWebsite",
                "testDesc"
        );

        verify(contentValuesMock).put(Password.USER_ID, 1L);
        verify(contentValuesMock).put(Password.LOGIN, "testLogin");
        verify(contentValuesMock).put(Password.PASSWORD, "passwd");
        verify(contentValuesMock).put(Password.IV, "testIV");
        verify(contentValuesMock).put(Password.WEBSITE, "testWebsite");
        verify(contentValuesMock).put(Password.DESCRIPTION, "testDesc");
        verify(databaseMock).insert(DataAccess.PASSWORD_TABLE, null, contentValuesMock);
        assertNull(password);
    }

    @Test
    public void deletePassword_ReturnsTrue_IfPasswordDeleted() {
        when(databaseMock.delete(anyString(), anyString(), any())).thenReturn(1);

        assertTrue(dataAccess.deletePassword(1));
        verify(databaseMock).delete(DataAccess.PASSWORD_TABLE, "password_id = ?", new String[] {"1"});
    }

    @Test
    public void deletePassword_ReturnsFalse_IfPasswordNotDeleted() {
        when(databaseMock.delete(anyString(), anyString(), any())).thenReturn(0);

        assertFalse(dataAccess.deletePassword(1));
        verify(databaseMock).delete(DataAccess.PASSWORD_TABLE, "password_id = ?", new String[] {"1"});
    }

    @Test
    public void updatePasswords_ReturnsTrue_IfPasswordsUpdated() {
        when(databaseMock.update(anyString(), any(), anyString(), any())).thenReturn(1);

        ArrayList<Password> passwords = new ArrayList<>();
        passwords.add(new Password(1, 1, "testLog", "pass", "testIV", "exampleWebsite", ""));
        passwords.add(new Password(2, 1, "testLog", "pass", "testIV", "exampleWebsite", ""));

        boolean result = dataAccess.updatePasswords(passwords);

        verify(databaseMock, times(1))
                .update(DataAccess.PASSWORD_TABLE, contentValuesMock, "password_id = ?", new String[] {"1"});
        verify(databaseMock, times(1))
                .update(DataAccess.PASSWORD_TABLE, contentValuesMock, "password_id = ?", new String[] {"2"});

        verify(databaseMock, times(1)).setTransactionSuccessful();
        verify(databaseMock, times(1)).endTransaction();
        verifyNoMoreInteractions(databaseMock);
        assertTrue(result);
    }

    @Test
    public void updatePasswords_ReturnsFalse_IfPasswordsNotUpdated() {
        when(databaseMock.update(anyString(), any(), anyString(), any())).thenReturn(1).thenReturn(0);

        ArrayList<Password> passwords = new ArrayList<>();
        passwords.add(new Password(1, 1, "testLog", "pass", "testIV", "exampleWebsite", ""));
        passwords.add(new Password(2, 1, "testLog", "pass", "testIV", "exampleWebsite", ""));
        passwords.add(new Password(3, 1, "testLog", "pass", "testIV", "exampleWebsite", ""));

        boolean result = dataAccess.updatePasswords(passwords);
        verify(databaseMock, times(1))
                .update(DataAccess.PASSWORD_TABLE, contentValuesMock, "password_id = ?", new String[] {"1"});
        verify(databaseMock, times(1))
                .update(DataAccess.PASSWORD_TABLE, contentValuesMock, "password_id = ?", new String[] {"2"});
        verify(databaseMock, times(1)).endTransaction();
        verifyNoMoreInteractions(databaseMock);
        assertFalse(result);
    }
}