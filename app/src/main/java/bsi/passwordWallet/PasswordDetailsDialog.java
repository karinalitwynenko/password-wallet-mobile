package bsi.passwordWallet;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Base64;

import androidx.fragment.app.DialogFragment;

public class PasswordDetailsDialog extends DialogFragment {
    WalletActivity.PasswordDeletedListener passwordDeletedListener;
    Password password;
    byte[] userPassword;

    public PasswordDetailsDialog(Password password, byte[] userPassword, WalletActivity.PasswordDeletedListener passwordDeletedListener) {
        this.password = password;
        this.userPassword = userPassword;
        this.passwordDeletedListener = passwordDeletedListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.dialog_password_details, container, false);

        final EditText loginEditText = v.findViewById(R.id.login_input);
        final EditText passwordEditText = v.findViewById(R.id.password_input);
        final EditText websiteEditText = v.findViewById(R.id.website_input);
        final EditText descriptionEditText = v.findViewById(R.id.description_input);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        loginEditText.setText(password.getLogin());
        passwordEditText.setText(Encryption.decryptAES128(password.getPassword(), userPassword, Base64.getDecoder().decode(password.getIV())));
        websiteEditText.setText(password.getWebsite());
        descriptionEditText.setText(password.getDescription());

        v.findViewById(R.id.reveal_old_password_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if the password is visible
                if(passwordEditText.getTransformationMethod() == null)
                    // hide the password
                    passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
                else
                    // display the password
                    passwordEditText.setTransformationMethod(null);

            }
        });

        // dismiss the dialog when the user clicks on close button (cross)
        v.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        v.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DataAccess.deletePassword(password.getPasswordID())) {
                    Toast.makeText(getContext(), "Could not delete the password", Toast.LENGTH_SHORT).show();
                }
                else {
                    passwordDeletedListener.passwordModified(password);
                    dismiss();
                }
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
