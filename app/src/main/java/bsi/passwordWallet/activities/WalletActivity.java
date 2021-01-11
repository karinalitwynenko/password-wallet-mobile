package bsi.passwordWallet.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import bsi.PasswordWalletApplication;
import bsi.passwordWallet.ActivityLog;
import bsi.passwordWallet.Encryption;
import bsi.passwordWallet.Password;
import bsi.passwordWallet.R;
import bsi.passwordWallet.User;
import bsi.passwordWallet.services.PasswordService;
import bsi.passwordWallet.services.UserService;

public class WalletActivity extends AppCompatActivity {
    private ListView passwordsListView;
    private ArrayList<Password> passwords;
    private PasswordAdapter passwordAdapter;
    private User user;
    private String masterPassword;
    private byte[] masterPasswordHash;  // MD5 hash
    PasswordWalletApplication app;

    private boolean editModeEnabled;

    static class PasswordAdapter extends ArrayAdapter<Password> {
        private ArrayList<Password> dataSet;

        public PasswordAdapter(ArrayList<Password> data, Context context) {
            super(context, R.layout.wallet_item, data);
            this.dataSet = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Password password = getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.wallet_item, parent, false);
            }

            TextView tv = convertView.findViewById(R.id.website);
            tv.setText(password.getWebsite());

            convertView.setTag(convertView.getId(), dataSet.get(position));
            tv = convertView.findViewById(R.id.description);
            tv.setText(password.getDescription());

            // Return the completed view to render on screen
            return convertView;
        }
    }

    public interface PasswordCreatedListener {
        void passwordCreated(Password password);
    }

    public interface UserPasswordModifiedListener {
        void userPasswordModified(String newUserPassword);
    }

    // called by AddPasswordDialog when user creates new password
    PasswordCreatedListener passwordCreatedListener = new PasswordCreatedListener() {
        @Override
        public void passwordCreated(Password password) {
            passwords.add(password);
            passwordAdapter.notifyDataSetChanged();

            // register create activity
            new PasswordService().registerUserActivity(
                    new ActivityLog(
                            user.getId(),
                            password.getId(),
                            new Date().getTime(),
                            ActivityLog.CREATE,
                            new Password(),
                            password
                    )
            );
        }
    };

    UserPasswordModifiedListener userPasswordModifiedListener = new UserPasswordModifiedListener() {
        @Override
        public void userPasswordModified(String newUserPassword) {
            byte[] newUserPasswordHash = new Encryption().calculateMD5(newUserPassword);
            PasswordService passwordService = new PasswordService();
            if(passwordService.updatePasswordHashes(passwords, masterPasswordHash, newUserPasswordHash)) {
                // if the passwords updated successfully, update password fields
                masterPassword = newUserPassword;
                masterPasswordHash = newUserPasswordHash;
                app.setMasterPasswordHash(masterPasswordHash);

                Toast.makeText(
                        getApplicationContext(),
                        "User's password updated",
                        Toast.LENGTH_SHORT
                ).show();
            }
            else {
                // reload the passwords from the database
                passwords = passwordService.getPasswords(user.getId());
                Toast.makeText(
                        getApplicationContext(),
                        "Failed to update user's password",
                        Toast.LENGTH_SHORT
                ).show();
            }

            passwordAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        } catch (NullPointerException e) {
            finish();
        }

        editModeEnabled = false;
        app = (PasswordWalletApplication)getApplication();

        TextView userButton = findViewById(R.id.user_button);

        if(getIntent().getExtras() != null) {
            user = (User)getIntent().getExtras().get("user");
            masterPassword = getIntent().getExtras().getString("user_password");
            masterPasswordHash = new Encryption().calculateMD5(masterPassword);
            app.setMasterPasswordHash(masterPasswordHash);
            userButton.setText(user.getLogin());
        }

        passwordsListView = findViewById(R.id.password_list);
        Button editModeToggle = findViewById(R.id.edit_mode_toggle);

        passwords = new ArrayList<>();
        passwordAdapter = new PasswordAdapter(passwords, this);
        passwordsListView.setAdapter(passwordAdapter);

        editModeToggle.setOnClickListener(view -> {
            TextView tv = (TextView)view;
            if(editModeEnabled) {
                tv.setText(R.string.ready_only);
                tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, 0, 0);
                editModeEnabled = false;
            }
            else {
                tv.setText(R.string.edit_mode_enabled);
                tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_open_24, 0, 0, 0);
                editModeEnabled = true;
            }
        });

        passwordsListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(), PasswordDetailsActivity.class);
            intent.putExtra("password", passwords.get(position));
            intent.putExtra("userMasterPassword", masterPasswordHash);
            intent.putExtra("editModeEnabled", editModeEnabled);
            intent.putExtra("user", user);

            startActivity(intent);
        });

        findViewById(R.id.add_new_password).setOnClickListener(v -> {
            AddPasswordDialog dialog =
                    (AddPasswordDialog)getSupportFragmentManager().findFragmentByTag("AddPassword");
            if(dialog == null) {
                dialog = new AddPasswordDialog(user, masterPasswordHash, passwordCreatedListener);
                dialog.show(getSupportFragmentManager(), "AddPassword");
            }
        });

        userButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), UserAccountActivity.class);
            intent.putExtra("user", user);
            startActivityForResult(intent, 0);
        });

        findViewById(R.id.change_master_password).setOnClickListener(v -> {
            UserAccountDialog dialog =
                (UserAccountDialog)getSupportFragmentManager().findFragmentByTag("UserAccount");
            if(dialog == null) {
                dialog = new UserAccountDialog(user, masterPassword, userPasswordModifiedListener);
                dialog.show(getSupportFragmentManager(), "UserAccount");
            }
        });

        findViewById(R.id.show_activity_log).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActionLogActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        PasswordService passwordService = new PasswordService();
        passwordService.checkForSharedPasswordUpdates(user, masterPasswordHash);
        passwordAdapter.dataSet.clear();
        passwordAdapter.dataSet.addAll(new PasswordService().getPasswords(user.getId()));
        passwordAdapter.notifyDataSetChanged();
    }
}
