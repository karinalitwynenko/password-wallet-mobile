package bsi.passwordWallet;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class WalletActivity extends AppCompatActivity {
    ListView passwordsListView;
    ArrayList<Password> passwords;
    PasswordAdapter passwordAdapter;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException e) {
            finish();
        }

        if(getIntent().getExtras() != null) {
            user = (User)getIntent().getExtras().get("user");
        }
        passwordsListView = findViewById(R.id.passwordsListView);


        passwords = new ArrayList<>();
        passwords.add(new Password("gamil.com"));
        passwords.add(new Password("ga32mil.com"));
        passwords.add(new Password("23.com"));
        passwords.add(new Password("gamil.com"));
        passwords.add(new Password("2eaw.com"));
        passwords.add(new Password("pollub.com"));
        passwords.add(new Password("xx2.com"));

        passwordAdapter = new PasswordAdapter(passwords, this);
        passwordsListView.setAdapter(passwordAdapter);

        findViewById(R.id.add_new_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordDetailsDialog dialog =
                        (PasswordDetailsDialog)getSupportFragmentManager().findFragmentByTag("PasswordDetails");
                if(dialog == null){
                    dialog = new PasswordDetailsDialog();
                    dialog.show(getSupportFragmentManager(), "PasswordDetails");
                }

                //DataAccess.createPassword(user.getLogin(), Encryption.encryptAES128())

            }
        });

    }

}
