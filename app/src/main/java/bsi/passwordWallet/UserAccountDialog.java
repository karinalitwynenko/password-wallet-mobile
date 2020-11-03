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

    Validation validation;

    void setValidation(Validation validation) {
        this.validation = validation;
    }

    public UserAccountDialog(User user, String userPassword, WalletActivity.UserPasswordModifiedListener userPasswordModifiedListener) {
        this.user = user;
        this.userPassword = userPassword;
        this.userPasswordModifiedListener = userPasswordModifiedListener;
        setValidation(new Validation());
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

                    String newMasterPassword = null;
                    try {
                        newMasterPassword = new UserService().updatePassword(user, oldPassword, newPassword, userPassword);
                    } catch (UserService.UserAccountException e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    if(newMasterPassword != null) {
                        userPasswordModifiedListener.userPasswordModified(newMasterPassword);
                        dismiss();
                    }

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
