package bsi.passwordWallet.activities;

import android.graphics.Color;
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
import java.util.Base64;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import bsi.passwordWallet.ActivityLog;
import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.Encryption;
import bsi.passwordWallet.Password;
import bsi.passwordWallet.PasswordChange;
import bsi.passwordWallet.R;
import bsi.passwordWallet.User;
import bsi.passwordWallet.services.PasswordService;
import bsi.passwordWallet.services.UserService;

public class PasswordDetailsActivity extends AppCompatActivity {
    private Password password;
    private byte[] masterPasswordHash;
    private boolean editModeEnabled;
    private boolean detailsChanged;
    private UserService userService = new UserService();
    private PasswordService passwordService = new PasswordService();

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

    View.OnClickListener disabledInputOnClick = view -> Toast.makeText(
            PasswordDetailsActivity.this,
            "You have to switch to the edit mode first.",
            Toast.LENGTH_LONG
    ).show();


    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Toast.makeText(PasswordDetailsActivity.this, "You have to switch to the edit mode first.", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle intentBundle = getIntent().getExtras();
        try {
            password = (Password)intentBundle.get("password");
            masterPasswordHash = intentBundle.getByteArray("userMasterPassword");
            editModeEnabled = intentBundle.getBoolean("editModeEnabled");
            user = intentBundle.getParcelable("user");
        } catch(Exception e) {
          e.printStackTrace();
          finish();
        }

        /*
         *  register 'view' activity
         */
        userService.registerUserActivity(
                new ActivityLog(
                        user.getId(),
                        password.getId(),
                        new Date().getTime(),
                        ActivityLog.VIEW,
                        new Password(),
                        password
                )
        );

        setContentView(R.layout.activity_password_details);

        final EditText loginEditText = findViewById(R.id.login_input);
        final EditText passwordEditText = findViewById(R.id.password_input);
        final EditText websiteEditText = findViewById(R.id.website_input);
        final EditText descriptionEditText = findViewById(R.id.description_input);
        final EditText shareEditText = findViewById(R.id.share_input);

        final Button saveButton = findViewById(R.id.save_button);
        final Button shareButton = findViewById(R.id.share_button);
        final Button deleteButton = findViewById(R.id.delete_button);

        Encryption encryption = new Encryption();

        loginEditText.setText(password.getLogin());
        passwordEditText.setText(
                encryption.decryptAES128(
                        password.getPassword(),
                        masterPasswordHash,
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
                    String result = passwordService.sharePassword(password, shareEditText.getText().toString(), masterPasswordHash);
                    Toast.makeText(PasswordDetailsActivity.this, result, Toast.LENGTH_SHORT).show();

                    if(result.equals("Password has been shared")) {
                        usersAdapter.clear();
                        usersAdapter.addAll(DataAccess.getInstance().getPartOwners(password.getId()));
                        usersAdapter.notifyDataSetChanged();
                        /*
                         register 'share' activity
                         */
                        userService.registerUserActivity(
                                new ActivityLog(
                                        user.getId(),
                                        password.getId(),
                                        new Date().getTime(),
                                        ActivityLog.SHARE,
                                        new Password(),
                                        password
                                )
                        );
                    }
                }
            });

            if(!editModeEnabled) {
                loginEditText.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_edit_text));
                passwordEditText.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_edit_text));
                websiteEditText.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_edit_text));
                descriptionEditText.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_edit_text));

                loginEditText.setTextColor(Color.WHITE);
                passwordEditText.setTextColor(Color.WHITE);
                websiteEditText.setTextColor(Color.WHITE);
                descriptionEditText.setTextColor(Color.WHITE);

                loginEditText.setKeyListener(null);
                passwordEditText.setKeyListener(null);
                websiteEditText.setKeyListener(null);
                descriptionEditText.setKeyListener(null);

                loginEditText.setOnFocusChangeListener(onFocusChangeListener);
                passwordEditText.setOnFocusChangeListener(onFocusChangeListener);
                websiteEditText.setOnFocusChangeListener(onFocusChangeListener);
                descriptionEditText.setOnFocusChangeListener(onFocusChangeListener);
            }
            else {
                loginEditText.addTextChangedListener(textWatcher);
                passwordEditText.addTextChangedListener(textWatcher);
                websiteEditText.addTextChangedListener(textWatcher);
                descriptionEditText.addTextChangedListener(textWatcher);
            }
        }
        else {
            ((TextView)findViewById(R.id.share_list_header)).setText(getText(R.string.belongs_to));
            usersAdapter.addAll(DataAccess.getInstance().getPasswordOwner(password.getId()));

            shareEditText.setVisibility(View.GONE);
            shareButton.setVisibility(View.GONE);
            findViewById(R.id.share_with_label).setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);

            editModeEnabled = false;
        }

        ((ListView)findViewById(R.id.share_list)).setAdapter(usersAdapter);

        saveButton.setOnClickListener(v -> {
            if(!detailsChanged)
                return;

            // TODO: check if any data actually changed
            // make a copy
            Password modifiedPassword = new Password(password);

            modifiedPassword.setLogin(loginEditText.getText().toString());
            modifiedPassword.setPassword(passwordEditText.getText().toString());
            modifiedPassword.setWebsite(websiteEditText.getText().toString());
            modifiedPassword.setDescription(descriptionEditText.getText().toString());

            boolean result = false;
            try {
                result = passwordService.updatePassword(modifiedPassword, masterPasswordHash);
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

                userService.registerUserActivity(
                        new ActivityLog(user.getId(),
                                password.getId(),
                                new Date().getTime(),
                                ActivityLog.UPDATE,
                                password,
                                modifiedPassword
                        )
                );

            }

        });

        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        findViewById(R.id.close_button).setOnClickListener(v -> finish());

        deleteButton.setOnClickListener(v -> {
            if(!editModeEnabled) {
                Toast.makeText(PasswordDetailsActivity.this, "You have to switch to the edit mode first.", Toast.LENGTH_LONG).show();
                return;
            }

            if(!passwordService.deletePassword(password.getId())) {
                Toast.makeText(
                        PasswordDetailsActivity.this,
                        "Could not delete the password",
                        Toast.LENGTH_SHORT
                ).show();
            }
            else {
                // register 'delete' activity
                userService.registerUserActivity(
                        new ActivityLog(user.getId(), password.getId(), new Date().getTime(), ActivityLog.DELETE, password, new Password()));

                Toast.makeText(PasswordDetailsActivity.this, "The password has been deleted.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

}