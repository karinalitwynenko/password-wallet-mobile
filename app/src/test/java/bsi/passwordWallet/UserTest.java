package bsi.passwordWallet;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class UserTest {
    User user;

    @Before
    public void setUp() {
        user = new User(
                1,
                "testLogin",
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
    public void writeToParcel_WritesToParcel_WhenValidDataPassed() {
        Parcel parcel = mock(Parcel.class);
        user.writeToParcel(parcel, 0);

        verify(parcel, times(1)).writeLong(user.getId());
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