package bsi.passwordWallet;

import android.os.Parcel;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertNotNull;

public class UserTest {
    User user;

    @BeforeMethod
    public void setUp() {
        user = new User(
                1,
                "TestLogin",
                Encryption.SHA512,
                "testpassword",
                "testsalt"
        );
    }

    @Test
    public void User_InitializesFromParcel_WhenValidDataPassed() {
        Parcel parcel = mock(Parcel.class);
        user = new User(parcel);
        verify(parcel, times(1)).readLong();
        verify(parcel, times(4)).readString();
        verifyNoMoreInteractions(parcel);
    }

    @Test
    public void User_WritesToParcel_WhenValidDataPassed() {
        Parcel parcel = mock(Parcel.class);
        user.writeToParcel(parcel, 0);
        verify(parcel, times(1)).writeLong(user.getUserID());
        verify(parcel, times(1)).writeString(user.getLogin());
        verify(parcel, times(1)).writeString(user.getEncryptionMethod());
        verify(parcel, times(1)).writeString(user.getPassword());
        verify(parcel, times(1)).writeString(user.getSalt());

        verifyNoMoreInteractions(parcel);
    }

    @Test
    public void createFromParcel_ReturnsUserInstance_WhenValidDataPassed() {
        Parcel parcel = mock(Parcel.class);
        user = User.CREATOR.createFromParcel(parcel);
        assertNotNull(user);
    }

    @Test
    public void newArray_ReturnsUserArray_WhenValidDataPassed() {
        User[] users = User.CREATOR.newArray(2);
        assertNotNull(users);
        assertEquals(users.length, 2);
    }
}