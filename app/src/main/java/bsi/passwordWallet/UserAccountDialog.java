package bsi.passwordWallet;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
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
        final EditText oldPasswordInput = v.findViewById(R.id.password_input);
        final EditText newPasswordInput = v.findViewById(R.id.new_password_input);

        loginInput.setText(user.getLogin());

        // obscure the password input
        oldPasswordInput.setTransformationMethod(new PasswordTransformationMethod());
        newPasswordInput.setTransformationMethod(new PasswordTransformationMethod());

        oldPasswordInput.addTextChangedListener(new TextWatcher() {
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
                    String oldPassword = oldPasswordInput.getText().toString();
                    String newPassword = newPasswordInput.getText().toString();

                    // validate password format
                    String validationResult = Validation.validatePassword(oldPassword);
                    if(!validationResult.equals("")) {
                        // inform the user and abort password modification
                        Toast.makeText(getContext(), validationResult, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    validationResult = Validation.validatePassword(newPassword);
                    if(!validationResult.equals("")) {
                        // inform the user and abort password modification
                        Toast.makeText(getContext(), validationResult, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // check if provided password is valid
                    if(!oldPassword.equals(userPassword)) {
                        Toast.makeText(getContext(), "Incorrect user password", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // check if current and new passwords are the same
                    if(oldPassword.equals(newPassword)) {
                        Toast.makeText(getContext(), "New password is the same as the current one.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Encryption encryption = new Encryption();
                    String newPasswordHash, encryptionMethod;
                    String newSalt = Encryption.generateSalt64();

                    if(user.getEncryptionMethod().equals(Encryption.SHA512))
                        encryptionMethod = Encryption.SHA512;
                    else
                        encryptionMethod = Encryption.HMAC_SHA512;

                    newPasswordHash = LoginActivity.calculateHash(encryption, encryptionMethod, newPassword, newSalt);

                    if(DataAccess.updateUserMasterPassword(user.getUserID(), newPasswordHash, newSalt)) {
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

        v.findViewById(R.id.reveal_old_password_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save current cursor position
                int cursorPosition = oldPasswordInput.getSelectionStart();

                // check if the password is visible
                if(oldPasswordInput.getTransformationMethod() == null)
                    // hide the password
                    oldPasswordInput.setTransformationMethod(new PasswordTransformationMethod());
                else
                    // display the password
                    oldPasswordInput.setTransformationMethod(null);

                // restore cursor position
                oldPasswordInput.setSelection(cursorPosition);
            }
        });

        v.findViewById(R.id.reveal_new_password_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save current cursor position
                int cursorPosition = newPasswordInput.getSelectionStart();

                // check if the password is visible
                if(newPasswordInput.getTransformationMethod() == null)
                    // hide the password
                    newPasswordInput.setTransformationMethod(new PasswordTransformationMethod());
                else
                    // display the password
                    newPasswordInput.setTransformationMethod(null);

                // restore cursor position
                newPasswordInput.setSelection(cursorPosition);
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
