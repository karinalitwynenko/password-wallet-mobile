package bsi.passwordWallet;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class WalletActivity extends AppCompatActivity {
    ListView passwordsListView;
    ArrayList<Password> passwords;
    PasswordAdapter passwordAdapter;
    User user;
    String userPassword;
    byte[] userPasswordHash;  // stored as MD5 hash

    interface PasswordCreatedListener {
        void passwordCreated(Password password);
    }

    interface PasswordDeletedListener {
        void passwordModified(Password password);
    }

    interface UserPasswordModifiedListener {
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
            /* updating user object is not necessary */
            byte[] newUserPasswordHash = Encryption.calculateMD5(newUserPassword);
            if(updatePasswords(passwords, newUserPasswordHash)) {
                // if the passwords updated successfully, update password fields
                userPassword = newUserPassword;
                userPasswordHash = newUserPasswordHash;

                Toast.makeText(
                        getApplicationContext(),
                        "User's password updated successfully",
                        Toast.LENGTH_SHORT
                ).show();
            }
            else {
                // reload the passwords from the database
                passwords = DataAccess.getPasswords(user.getUserID());
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
            userPasswordHash = Encryption.calculateMD5(userPassword);
            userButton.setText(user.getLogin());
        }

        passwordsListView = findViewById(R.id.passwordsListView);
        passwords = DataAccess.getPasswords(user.getUserID());

        passwordAdapter = new PasswordAdapter(passwords, this);
        passwordsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PasswordDetailsDialog dialog =
                        (PasswordDetailsDialog)getSupportFragmentManager().findFragmentByTag("PasswordDetails");
                if(dialog == null) {
                    dialog = new PasswordDetailsDialog(
                            passwords.get(position), userPasswordHash, passwordDeletedListener
                    );
                    dialog.show(getSupportFragmentManager(), "PasswordDetails");
                }
            }
        });

        passwordsListView.setAdapter(passwordAdapter);

        findViewById(R.id.add_new_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPasswordDialog dialog =
                        (AddPasswordDialog)getSupportFragmentManager().findFragmentByTag("AddPassword");
                if(dialog == null) {
                    dialog = new AddPasswordDialog(user.getUserID(), userPasswordHash, passwordCreatedListener);
                    dialog.show(getSupportFragmentManager(), "AddPassword");
                }
            }
        });

        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserAccountDialog dialog =
                        (UserAccountDialog)getSupportFragmentManager().findFragmentByTag("UserAccount");
                if(dialog == null) {
                    dialog = new UserAccountDialog(user, userPassword, userPasswordModifiedListener);
                    dialog.show(getSupportFragmentManager(), "UserAccount");
                }
            }
        });
    }

    boolean updatePasswords(ArrayList<Password> passwords, byte[] newUserPasswordHash) {
        String decryptedPassword;
        byte[] randomIV;
        for (Password p : passwords) {
            // decrypt the password using previous user's key
            decryptedPassword = Encryption.decryptAES128(
                    p.getPassword(), userPasswordHash, Base64.getDecoder().decode(p.getIV())
            );
            // generate new initialization vector
            randomIV = Encryption.randomIV();
            // encrypt the password using new user's key
            p.setPassword(Encryption.encryptAES128(decryptedPassword, newUserPasswordHash, randomIV));
            p.setIV(Base64.getEncoder().encodeToString(randomIV));
        }

        return DataAccess.updatePasswords(passwords);
    }

}
