package bsi.passwordWallet.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.Encryption;
import bsi.passwordWallet.Password;
import bsi.passwordWallet.R;
import bsi.passwordWallet.User;
import bsi.passwordWallet.services.PasswordService;

public class PasswordDetailsDialog extends DialogFragment {
    private WalletActivity.PasswordDeletedListener passwordDeletedListener;
    private Password password;
    private byte[] userPassword;
    private boolean editModeEnabled;
    private ArrayList<String> users;

    public PasswordDetailsDialog(Password password,
                                 byte[] userPassword,
                                 WalletActivity.PasswordDeletedListener passwordDeletedListener,
                                 boolean editModeEnabled) {
        this.password = password;
        this.userPassword = userPassword;
        this.passwordDeletedListener = passwordDeletedListener;
        this.editModeEnabled = editModeEnabled;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_password_details, container, true);
        final EditText loginEditText = v.findViewById(R.id.login_input);
        final EditText passwordEditText = v.findViewById(R.id.password_input);
        final EditText websiteEditText = v.findViewById(R.id.website_input);
        final EditText descriptionEditText = v.findViewById(R.id.description_input);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        Encryption encryption = new Encryption();

        loginEditText.setText(password.getLogin());
        passwordEditText.setText(
                encryption.decryptAES128(
                        password.getPassword(),
                        userPassword,
                        Base64.getDecoder().decode(password.getIV())
                )
        );
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
                if(!new DataAccess().deletePassword(password.getId())) {
                    Toast.makeText(getContext(), "Could not delete the password", Toast.LENGTH_SHORT).show();
                }
                else {
                    passwordDeletedListener.passwordModified(password);
                    dismiss();
                }
            }
        });

        v.findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText shareUser = v.findViewById(R.id.share_input);
                if(!shareUser.getText().toString().isEmpty()) {
                    PasswordService passwordService = new PasswordService();
                    String result = passwordService.sharePassword(password.getId(), shareUser.getText().toString());
                    Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
                }
            }
        });

        users = new ArrayList<>();
        users.add("sdds");
        users.add("dg4");
        ArrayAdapter<String> usersAdapter = new ArrayAdapter<>(getContext(), R.layout.part_owner_item, R.id.user_login, users);

        ((ListView)v.findViewById(R.id.share_list)).setAdapter(usersAdapter);
        return v;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
