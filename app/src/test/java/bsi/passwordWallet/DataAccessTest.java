package bsi.passwordWallet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    public void getUser_ReturnsNull_IfUserDoesNotExist() {
        Cursor cursor = mock(Cursor.class);

        when(databaseMock.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.getCount()).thenReturn(0);
        verifyNoMoreInteractions(cursor);

        User user = dataAccess.getUser("testLogin");
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
        assertNotNull(user);
        assertEquals(user.getId(), 1L);
        assertNotNull(user.getLogin());
        assertNotNull(user.getEncryptionMethod());
        assertNotNull(user.getPassword());
        assertNotNull(user.getSalt());
    }

    @Test
    public void createUser_ReturnsNewUser_IfCreated() {
        when(databaseMock.insert(DataAccess.USER_TABLE, null, contentValuesMock)).thenReturn(1L);
        User user = dataAccess.createUser("testLogin", "", "testHash", "testSalt");

        verify(contentValuesMock, times(4)).put(anyString(), anyString());
        assertNotNull(user);
    }

    @Test
    public void createUser_ReturnsNull_IfUserNotCreated() {
        when(databaseMock.insert(DataAccess.USER_TABLE, null, contentValuesMock)).thenReturn(-1L);

        User user = dataAccess.createUser("testLogin", "", "testHash", "testSalt");

        assertNull(user);
    }

    @Test
    public void updateUserMasterPassword_ReturnsTrue_IfPasswordUpdated() {
        when(databaseMock.update(DataAccess.USER_TABLE, contentValuesMock, "user_id = ?", new String[] {String.valueOf(1)})).thenReturn(1);
        boolean result = dataAccess.updateUserMasterPassword(1, "testPassword", "testSalt");

        verify(databaseMock, times(1)).beginTransaction();
        assertTrue(result);
    }

    @Test
    public void updateUserMasterPassword_ReturnsFalse_IfPasswordNotUpdated() {
        when(databaseMock.update(DataAccess.USER_TABLE, contentValuesMock, "user_id = ?", new String[] {String.valueOf(1)})).thenReturn(-1);
        boolean result = dataAccess.updateUserMasterPassword(1, "testPassword", "testSalt");
        assertFalse(result);
    }

    @Test
    public void getPasswords_ReturnsNonEmptyPasswordList_IfPasswordsExist() {
        Cursor cursor = mock(Cursor.class);

        when(databaseMock.rawQuery(
                "select * from " + DataAccess.PASSWORD_TABLE + " where user_id = ?",
                new String[] {String.valueOf(1)}
                )
        ).thenReturn(cursor);

        when(cursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);

        ArrayList<Password> passwords = DataAccess.getPasswords(1);
        verify(cursor, atLeastOnce()).getLong(anyInt());
        verify(cursor, atLeastOnce()).getString(anyInt());

        assertEquals(2, passwords.size());
    }

    @Test
    public void getPasswords_ReturnsEmptyPasswordList_IfPasswordsDoNotExist() {
        Cursor cursor = mock(Cursor.class);

        when(databaseMock.rawQuery(
                "select * from " + DataAccess.PASSWORD_TABLE + " where user_id = ?",
                new String[] {String.valueOf(1)}
                )
        ).thenReturn(cursor);

        when(cursor.moveToNext()).thenReturn(false);

        ArrayList<Password> passwords = DataAccess.getPasswords(1);
        assertEquals(0, passwords.size());
    }

    @Test
    public void createPassword_ReturnsNewPassword_IfCreated() {
        when(databaseMock.insert(DataAccess.PASSWORD_TABLE, null, contentValuesMock))
                .thenReturn(1L);

        Password password = dataAccess.createPassword(
                1L,
                "testLogin",
                "passwd",
                "testIV",
                "testWebsite",
                "testDesc"
        );

        verify(contentValuesMock, times(1)).put(anyString(), anyLong());
        verify(contentValuesMock, times(5)).put(anyString(), anyString());
        assertNotNull(password);
    }

    @Test
    public void createPassword_ReturnsNull_IfNotCreated() {
        when(databaseMock.insert(DataAccess.PASSWORD_TABLE, null, contentValuesMock))
                .thenReturn(-1L);

        Password password = dataAccess.createPassword(
                1L,
                "testLogin",
                "passwd",
                "testIV",
                "testWebsite",
                "testDesc"
        );

        assertNull(password);
    }

    @Test
    public void deletePassword_ReturnsTrue_IfPasswordDeleted() {
        when(databaseMock.delete(DataAccess.PASSWORD_TABLE,"password_id = ?", new String[] {String.valueOf(1L)}))
                .thenReturn(1);

        assertTrue(dataAccess.deletePassword(1));
    }

    @Test
    public void deletePassword_ReturnsFalse_IfPasswordNotDeleted() {
        when(databaseMock.delete(DataAccess.PASSWORD_TABLE,"password_id = ?", new String[] {String.valueOf(1L)}))
                .thenReturn(0);

        assertFalse(dataAccess.deletePassword(1));
    }

    @Test
    public void updatePasswords_ReturnsTrue_IfPasswordsUpdated() {
        when(
                databaseMock.update(
                        DataAccess.PASSWORD_TABLE, contentValuesMock, "password_id = ?",
                        new String[] {"1"}
                        )
        ).thenReturn(1);

        ArrayList<Password> passwords = new ArrayList<>();
        passwords.add(new Password(1, 1, "testLog", "pass", "testIV", "exampleWebsite", ""));
        passwords.add(new Password(1, 1, "testLog", "pass", "testIV", "exampleWebsite", ""));

        boolean result = dataAccess.updatePasswords(passwords);

        verify(databaseMock, times(1)).setTransactionSuccessful();
        verify(databaseMock, times(1)).endTransaction();

        assertTrue(result);
    }

    @Test
    public void updatePasswords_ReturnsFalse_IfPasswordsNotUpdated() {
        when(
                databaseMock.update(
                        DataAccess.PASSWORD_TABLE, contentValuesMock, "password_id = ?",
                        new String[] {"1"}
                )
        ).thenReturn(0);

        ArrayList<Password> passwords = new ArrayList<>();
        passwords.add(new Password(1, 1, "testLog", "pass", "testIV", "exampleWebsite", ""));

        boolean result = dataAccess.updatePasswords(passwords);

        verify(databaseMock, times(1)).endTransaction();
        assertFalse(result);
    }
}