package bsi.passwordWallet.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;
import bsi.passwordWallet.ActivityLog;
import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.Encryption;
import bsi.passwordWallet.Password;
import bsi.passwordWallet.R;
import bsi.passwordWallet.User;
import bsi.passwordWallet.services.PasswordService;
import bsi.passwordWallet.services.UserService;

public class PasswordDetailsActivity extends AppCompatActivity {
    private Password password;
    private byte[] userPassword;
    private boolean editModeEnabled;
    private boolean detailsChanged;
    private UserService userService = new UserService();
    private User user;


    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void afterTextChanged(Editable s) {
            detailsChanged = true;
        }
    };

    View.OnClickListener disabledInputOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(
                    PasswordDetailsActivity.this,
                    "You have to switch to the edit mode first.",
                    Toast.LENGTH_LONG
            ).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle intentBundle = getIntent().getExtras();
        try {
            password = (Password)intentBundle.get("password");
            userPassword = intentBundle.getByteArray("userMasterPassword");
            editModeEnabled = intentBundle.getBoolean("editModeEnabled");
            user = intentBundle.getParcelable("user");
        } catch(Exception e) {
          e.printStackTrace();
          finish();
        }


        // register 'view' activity
        userService.registerUserActivity(
                new ActivityLog(user.getId(), password.getId(), new Date().getTime(), ActivityLog.VIEW));

        setContentView(R.layout.activity_password_details);

        final EditText loginEditText = findViewById(R.id.login_input);
        final EditText passwordEditText = findViewById(R.id.password_input);
        final EditText websiteEditText = findViewById(R.id.website_input);
        final EditText descriptionEditText = findViewById(R.id.description_input);
        final EditText shareEditText = findViewById(R.id.share_input);

        final Button shareButton = findViewById(R.id.share_button);
        final Button deleteButton = findViewById(R.id.delete_button);

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

        findViewById(R.id.reveal_old_password_button).setOnClickListener(v -> {
            // check if the password is visible
            if(passwordEditText.getTransformationMethod() == null)
                // hide the password
                passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
            else
                // display the password
                passwordEditText.setTransformationMethod(null);

        });

        ArrayList<String> users = new ArrayList<>();
        ArrayAdapter<String> usersAdapter = new ArrayAdapter<>(this, R.layout.part_owner_item, R.id.user_login, users);

        // check if the password belongs to the current user
        if(password.getUserId() == user.getId()) {
            usersAdapter.addAll(DataAccess.getInstance().getPartOwners(password.getId()));

            shareButton.setOnClickListener(view -> {
                if(!shareEditText.getText().toString().isEmpty()) {
                    PasswordService passwordService = new PasswordService();
                    String result = passwordService.sharePassword(password, shareEditText.getText().toString());
                    Toast.makeText(PasswordDetailsActivity.this, result, Toast.LENGTH_SHORT).show();

                    if(result.equals("Password has been shared")) {
                        usersAdapter.clear();
                        usersAdapter.addAll(DataAccess.getInstance().getPartOwners(password.getId()));
                        usersAdapter.notifyDataSetChanged();
                        // register 'share' activity
                        userService.registerUserActivity(
                                new ActivityLog(user.getId(), password.getId(), new Date().getTime(), ActivityLog.SHARE));
                    }
                }
            });
        }
        else {
            ((TextView)findViewById(R.id.share_list_header)).setText(getText(R.string.belongs_to));
            usersAdapter.addAll(DataAccess.getInstance().getPasswordOwner(password.getId()));

            shareEditText.setVisibility(View.GONE);
            shareButton.setVisibility(View.GONE);
            findViewById(R.id.share_with_label).setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);

            loginEditText.setOnClickListener(disabledInputOnClick);
            passwordEditText.setOnClickListener(disabledInputOnClick);
            websiteEditText.setOnClickListener(disabledInputOnClick);
            descriptionEditText.setOnClickListener(disabledInputOnClick);

            editModeEnabled = false;
        }

        ((ListView)findViewById(R.id.share_list)).setAdapter(usersAdapter);

        findViewById(R.id.save_button).setOnClickListener(v -> {
            if(!detailsChanged)
                return;

            password.setLogin(loginEditText.getText().toString());
            password.setPassword(passwordEditText.getText().toString());
            password.setWebsite(websiteEditText.getText().toString());
            password.setDescription(descriptionEditText.getText().toString());

            boolean result = false;
            try {
                result = new PasswordService().updatePassword(password, userPassword);
            } catch (PasswordService.PasswordCreationException e) {
                Toast.makeText(
                        PasswordDetailsActivity.this,
                        e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }

            if(result) {
                Toast.makeText(
                        PasswordDetailsActivity.this,
                        "Password has been modified.",
                        Toast.LENGTH_LONG
                ).show();

                // TODO: register changes
            }

        });

        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        loginEditText.setEnabled(editModeEnabled);
        passwordEditText.setEnabled(editModeEnabled);
        websiteEditText.setEnabled(editModeEnabled);
        descriptionEditText.setEnabled(editModeEnabled);
        deleteButton.setEnabled(editModeEnabled);

        if(editModeEnabled) {
            loginEditText.addTextChangedListener(textWatcher);
            passwordEditText.addTextChangedListener(textWatcher);
            websiteEditText.addTextChangedListener(textWatcher);
            descriptionEditText.addTextChangedListener(textWatcher);
        }

        findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        deleteButton.setOnClickListener(v -> {
            if(!new DataAccess().deletePassword(password.getId())) {
                Toast.makeText(PasswordDetailsActivity.this, "Could not delete the password", Toast.LENGTH_SHORT).show();
            }
            else {
                // register 'delete' activity
                userService.registerUserActivity(
                        new ActivityLog(user.getId(), password.getId(), new Date().getTime(), ActivityLog.DELETE));

                Toast.makeText(PasswordDetailsActivity.this, "The password has been deleted.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

}