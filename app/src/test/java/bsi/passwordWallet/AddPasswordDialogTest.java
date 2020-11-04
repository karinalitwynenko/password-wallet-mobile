package bsi.passwordWallet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddPasswordDialogTest {
    @Mock
    WalletActivity.PasswordCreatedListener passwordCreatedListener;
    @Mock
    View.OnClickListener addPasswordOnClickListener;
    @Mock
    View.OnClickListener closeOnClickListener;

    @InjectMocks
    AddPasswordDialog dialog = new AddPasswordDialog(null, null, null);

    @Test
    public void onCreateView_InflatesView() {
        LayoutInflater inflater = mock(LayoutInflater.class);
        ViewGroup container = mock(ViewGroup.class);
        View v = mock(View.class);
        when(inflater.inflate(R.layout.dialog_add_password, container, false)).thenReturn(v);
        when(v.findViewById(R.id.add_button)).thenReturn(mock(Button.class));
        when(v.findViewById(R.id.close_button)).thenReturn(mock(Button.class));

        dialog.onCreateView(inflater, container, null);
        verify(v, times(1)).findViewById(R.id.login_input);
        verify(v, times(1)).findViewById(R.id.password_input);
        verify(v, times(1)).findViewById(R.id.website_input);
        verify(v, times(1)).findViewById(R.id.description_input);
        verify(v, times(1)).findViewById(R.id.add_button);
        verify(v, times(1)).findViewById(R.id.close_button);

        verify(dialog.addButton, times(1)).setOnClickListener(addPasswordOnClickListener);
        verify(dialog.closeButton, times(1)).setOnClickListener(closeOnClickListener);

        assertNotNull(v);
    }

//    @Test
//    public void validate_ReturnsEmptyString_IfDataValid() {
//        Validation validation = mock(Validation.class);
//        dialog.setValidation(validation);
//        String login = "testLogin";
//        String password = "testPassword";
//        String website = "testWebsite";
//        when(validation.validateLogin(login)).thenReturn("");
//        when(validation.validatePassword(password)).thenReturn("");
//        when(validation.validateWebsite(website)).thenReturn("");
//
//        String result = dialog.validate(login, password, website);
//        assertEquals("", result);
//    }
//
//    @Test
//    public void validate_ReturnsNonEmptyMessage_IfDataInvalid() {
//        Validation validation = mock(Validation.class);
//        dialog.setValidation(validation);
//        String login = "testLogin";
//        String password = "testPassword";
//        String website = "testWebsite";
//        when(validation.validateLogin(login)).thenReturn("");
//        when(validation.validatePassword(password)).thenReturn(Validation.PASSWORD_CANT_BE_LONGER_THAN);
//        when(validation.validateWebsite(website)).thenReturn("");
//
//        String result = dialog.validate(login, password, website);
//        assertEquals(Validation.PASSWORD_CANT_BE_LONGER_THAN, result);
//    }

}