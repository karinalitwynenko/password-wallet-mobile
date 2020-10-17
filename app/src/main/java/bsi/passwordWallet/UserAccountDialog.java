package bsi.passwordWallet;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

public class UserAccountDialog extends DialogFragment {
    WalletActivity.UserPasswordModifiedListener userPasswordModifiedListener;
    User user;
    String userPassword;
    boolean passwordModified = false;
    public UserAccountDialog(User user, String userPassword, WalletActivity.UserPasswordModifiedListener userPasswordModifiedListener) {
        this.user = user;
        this.userPassword = userPassword;
        this.userPasswordModifiedListener = userPasswordModifiedListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View v = inflater.inflate(R.layout.dialog_user_account, container, false);

        /* bind views and populate with the data */
        final EditText loginInput = v.findViewById(R.id.login_input);
        final EditText passwordInput = v.findViewById(R.id.password_input);
        loginInput.setText(user.getLogin());
        passwordInput.setText(userPassword);

        // obscure the password input
        passwordInput.setTransformationMethod(new PasswordTransformationMethod());

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                passwordModified = true;
            }
        });

        v.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if user's password was modified
                if(passwordModified) {
                    String newPassword = passwordInput.getText().toString();

                    // validate password format
                    String validationResult = Validation.validatePassword(newPassword);
                    if(!validationResult.equals("")) {
                        // inform the user and abort password modification
                        Toast.makeText(getContext(), validationResult, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String newPasswordHash;
                    String newSalt = Encryption.generateSalt64();

                    // get preferred encryption method and calculate the hash
                    if(user.getEncryptionMethod().equals(Encryption.SHA512))
                        newPasswordHash = Encryption.calculate512(newPassword, newSalt, Encryption.PEPPER);
                    else
                        newPasswordHash = Encryption.calculateHMAC(passwordInput.getText().toString(), newSalt, Encryption.PEPPER);

                    if(DataAccess.updateUserPassword(user.getUserID(), newPasswordHash, newSalt)) {
                        // notify the WalletActivity
                        userPasswordModifiedListener.userPasswordModified(newPassword);
                        dismiss();
                    }
                    else // if any error occurred
                         // notify the user
                        Toast.makeText(getContext(), "Could not change user's password", Toast.LENGTH_SHORT).show();
                }

            }
        });

        v.findViewById(R.id.reveal_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save current cursor position
                int cursorPosition = passwordInput.getSelectionStart();

                // check if the password is visible
                if(passwordInput.getTransformationMethod() == null)
                    // hide the password
                    passwordInput.setTransformationMethod(new PasswordTransformationMethod());
                else
                    // display the password
                    passwordInput.setTransformationMethod(null);

                // restore cursor position
                passwordInput.setSelection(cursorPosition);
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
