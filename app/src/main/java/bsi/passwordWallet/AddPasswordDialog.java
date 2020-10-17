package bsi.passwordWallet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Base64;

import androidx.fragment.app.DialogFragment;

public class AddPasswordDialog extends DialogFragment {
    WalletActivity.PasswordCreatedListener passwordCreatedListener;
    long userID;
    byte[] userPassword;
    public AddPasswordDialog(long userID, byte[] userPassword, WalletActivity.PasswordCreatedListener passwordCreatedListener) {
        this.userID = userID;
        this.userPassword = userPassword;
        this.passwordCreatedListener = passwordCreatedListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View v = inflater.inflate(R.layout.dialog_add_password, container, false);

        final EditText loginEditText = v.findViewById(R.id.login_input);
        final EditText passwordEditText = v.findViewById(R.id.password_input);
        final EditText websiteEditText = v.findViewById(R.id.website_input);
        final EditText descriptionEditText = v.findViewById(R.id.description_input);

        v.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passwordText = passwordEditText.getText().toString();

                byte[] randomIV = Encryption.randomIV();

                Password password = DataAccess.createPassword(
                        userID,
                        loginEditText.getText().toString(),
                        Encryption.encryptAES128(passwordText, userPassword, randomIV),
                        Base64.getEncoder().encodeToString(randomIV),
                        websiteEditText.getText().toString(),
                        descriptionEditText.getText().toString()
                );

                if(password != null) {
                    passwordCreatedListener.passwordCreated(password);
                    dismiss();
                }
                else {
                    Toast.makeText(getContext(), "Could not create new password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // dismiss the dialog when the user clicks on close button (cross)
        v.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
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
