package bsi.passwordWallet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

import androidx.fragment.app.DialogFragment;

public class AddPasswordDialog extends DialogFragment {
    EditText loginEditText;
    EditText passwordEditText;
    EditText websiteEditText;
    EditText descriptionEditText;
    Button addButton;
    Button closeButton;

    User user;
    byte[] masterPassword;

    PasswordService passwordService = new PasswordService();

    WalletActivity.PasswordCreatedListener passwordCreatedListener;

    View.OnClickListener addPasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            HashMap<String, String> passwordParams = new HashMap<>();
            passwordParams.put(DataAccess.LOGIN, loginEditText.getText().toString());
            passwordParams.put(DataAccess.PASSWORD, passwordEditText.getText().toString());
            passwordParams.put(DataAccess.WEBSITE, websiteEditText.getText().toString());
            passwordParams.put(DataAccess.DESCRIPTION, descriptionEditText.getText().toString());

            Password newPassword;
            try {
                newPassword = passwordService.addPassword(user, masterPassword, passwordParams);
            }
            catch(PasswordService.PasswordCreationException e) {
                Toast.makeText(
                        getContext(),
                        e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();

                dismiss();
                return;
            }

            Toast.makeText(
                    getContext(),
                    PasswordService.NEW_PASSWORD_ADDED,
                    Toast.LENGTH_LONG
            ).show();

            passwordCreatedListener.passwordCreated(newPassword);
            dismiss();
        }
    };

    View.OnClickListener closeOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    public AddPasswordDialog(User user, byte[] masterPassword, WalletActivity.PasswordCreatedListener passwordCreatedListener) {
        this.user = user;
        this.masterPassword = masterPassword;
        this.passwordCreatedListener = passwordCreatedListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View v = inflater.inflate(R.layout.dialog_add_password, container, false);

        loginEditText = v.findViewById(R.id.login_input);
        passwordEditText = v.findViewById(R.id.password_input);
        websiteEditText = v.findViewById(R.id.website_input);
        descriptionEditText = v.findViewById(R.id.description_input);
        addButton = v.findViewById(R.id.add_button);
        closeButton = v.findViewById(R.id.close_button);
        addButton.setOnClickListener(addPasswordOnClickListener);
        // dismiss the dialog when the user clicks on close button (cross)
        closeButton.setOnClickListener(closeOnClickListener);

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
