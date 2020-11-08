package bsi.passwordWallet.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.Encryption;
import bsi.passwordWallet.Password;
import bsi.passwordWallet.R;
import bsi.passwordWallet.User;
import bsi.passwordWallet.services.PasswordService;
import bsi.passwordWallet.services.UserService;

public class WalletActivity extends AppCompatActivity {
    ListView passwordsListView;
    ArrayList<Password> passwords;
    PasswordAdapter passwordAdapter;
    User user;
    String userPassword;
    byte[] userPasswordHash;  // stored as MD5 hash

    static class PasswordAdapter extends ArrayAdapter<Password> {
        private final ArrayList<Password> dataSet;

        public PasswordAdapter(ArrayList<Password> data, Context context) {
            super(context, R.layout.wallet_item, data);
            this.dataSet = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // get the data item for position
            Password password = getItem(position);

            // check if the view is being reused
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

    public interface PasswordDeletedListener {
        void passwordModified(Password password);
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
        }
    };

    PasswordDeletedListener passwordDeletedListener = new PasswordDeletedListener() {
        @Override
        public void passwordModified(Password password) {
            passwords.remove(password);
            passwordAdapter.notifyDataSetChanged();
        }
    };

    UserPasswordModifiedListener userPasswordModifiedListener = new UserPasswordModifiedListener() {
        @Override
        public void userPasswordModified(String newUserPassword) {
            byte[] newUserPasswordHash = new Encryption().calculateMD5(newUserPassword);
            PasswordService passwordService = new PasswordService();
            if(passwordService.updatePasswords(passwords, userPasswordHash, newUserPasswordHash)) {
                // if the passwords updated successfully, update password fields
                userPassword = newUserPassword;
                userPasswordHash = newUserPasswordHash;

                Toast.makeText(
                        getApplicationContext(),
                        "User's password updated",
                        Toast.LENGTH_SHORT
                ).show();
            }
            else {
                // reload the passwords from the database
                passwords = DataAccess.getInstance().getPasswords(user.getId());
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

        TextView userButton = findViewById(R.id.user_button);

        if(getIntent().getExtras() != null) {
            user = (User)getIntent().getExtras().get("user");
            userPassword = getIntent().getExtras().getString("user_password");
            userPasswordHash = new Encryption().calculateMD5(userPassword);
            userButton.setText(user.getLogin());
        }

        passwordsListView = findViewById(R.id.passwordsListView);
        passwords = DataAccess.getInstance().getPasswords(user.getId());

        passwordAdapter = new PasswordAdapter(passwords, this);
        passwordsListView.setOnItemClickListener((parent, view, position, id) -> {
            PasswordDetailsDialog dialog =
                    (PasswordDetailsDialog)getSupportFragmentManager().findFragmentByTag("PasswordDetails");
            if(dialog == null) {
                dialog = new PasswordDetailsDialog(
                        passwords.get(position), userPasswordHash, passwordDeletedListener
                );
                dialog.show(getSupportFragmentManager(), "PasswordDetails");
            }
        });

        passwordsListView.setAdapter(passwordAdapter);

        findViewById(R.id.add_new_password).setOnClickListener(v -> {
            AddPasswordDialog dialog =
                    (AddPasswordDialog)getSupportFragmentManager().findFragmentByTag("AddPassword");
            if(dialog == null) {
                dialog = new AddPasswordDialog(user, userPasswordHash, passwordCreatedListener);
                dialog.show(getSupportFragmentManager(), "AddPassword");
            }
        });

        userButton.setOnClickListener(v -> {
            UserAccountDialog dialog =
                    (UserAccountDialog)getSupportFragmentManager().findFragmentByTag("UserAccount");
            if(dialog == null) {
                dialog = new UserAccountDialog(user, userPassword, userPasswordModifiedListener);
                dialog.show(getSupportFragmentManager(), "UserAccount");
            }
        });
    }
}
