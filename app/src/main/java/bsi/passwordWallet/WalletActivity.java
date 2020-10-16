package bsi.passwordWallet;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class WalletActivity extends AppCompatActivity {
    ListView passwordsListView;
    ArrayList<Password> passwords;
    PasswordAdapter passwordAdapter;
    User user;
    byte[] userPassword;  // stored as MD5 hash

    interface PasswordCreatedListener {
        void passwordCreated(Password password);
    }

    interface PasswordDeletedListener {
        void passwordModified(Password password);
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

        if(getIntent().getExtras() != null) {
            user = (User)getIntent().getExtras().get("user");
            userPassword = Encryption.encryptMD5(getIntent().getExtras().getString("user_password"));
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
                    dialog = new PasswordDetailsDialog(passwords.get(position), userPassword, passwordDeletedListener);
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
                    dialog = new AddPasswordDialog(user.getUserID(), userPassword, passwordCreatedListener);
                    dialog.show(getSupportFragmentManager(), "AddPassword");
                }
            }
        });
    }

}
