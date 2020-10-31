package bsi.passwordWallet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DataAccessTest {

    @Before
    public void setUp() {
        DataAccess.database = databaseMock;
    }

    @Mock
    SQLiteDatabase databaseMock;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void getUser_ReturnsNull_IfUserDoesNotExist() {
        Cursor cursor = mock(Cursor.class);

        when(databaseMock.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.getCount()).thenReturn(0);
        verifyNoMoreInteractions(cursor);

        User user = DataAccess.getUser("testLogin");
        assertNull(user);
    }

    @Test
    public void getUser_ReturnsUser_IfUserExists() {
        Cursor cursor = mock(Cursor.class);
        when(databaseMock.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.getCount()).thenReturn(1);
        when(cursor.getLong(anyInt())).thenReturn(1L);
        when(cursor.getString(anyInt())).thenReturn("test");

        User user = DataAccess.getUser("testLogin");
        assertNotNull(user);
        assertEquals(user.getUserID(), 1L);
        assertNotNull(user.getLogin());
        assertNotNull(user.getEncryptionMethod());
        assertNotNull(user.getPassword());
        assertNotNull(user.getSalt());
    }

    @Test
    public void createUser_ReturnsNewUser_IfSuccessfullyInserted() {
        //ContentValues values = mock(ContentValues.class);

    }


}