package bsi.passwordWallet.activities;

import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;
import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.Encryption;
import bsi.passwordWallet.Password;
import bsi.passwordWallet.R;
import bsi.passwordWallet.User;
import bsi.passwordWallet.services.PasswordService;

public class PasswordDetailsActivity extends AppCompatActivity {
    private WalletActivity.PasswordDeletedListener passwordDeletedListener;
    private Password password;
    private byte[] userPassword;
    private boolean editModeEnabled;
    private ArrayList<String> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User user = null;
        Bundle intentBundle = getIntent().getExtras();
        try{
            password = (Password)intentBundle.get("password");
            userPassword = intentBundle.getByteArray("userMasterPassword");
            editModeEnabled = intentBundle.getBoolean("editModeEnabled");
            user = intentBundle.getParcelable("user");
        } catch(Exception e) {
          e.printStackTrace();
          finish();
        }

        setContentView(R.layout.activity_password_details);

        final EditText loginEditText = findViewById(R.id.login_input);
        final EditText passwordEditText = findViewById(R.id.password_input);
        final EditText websiteEditText = findViewById(R.id.website_input);
        final EditText descriptionEditText = findViewById(R.id.description_input);
        final EditText shareEditText = findViewById(R.id.share_input);

        final Button shareButton = findViewById(R.id.share_button);
        final Button deleteButton = findViewById(R.id.delete_button);

        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        loginEditText.setEnabled(editModeEnabled);
        passwordEditText.setEnabled(editModeEnabled);
        websiteEditText.setEnabled(editModeEnabled);
        descriptionEditText.setEnabled(editModeEnabled);
        deleteButton.setEnabled(editModeEnabled);

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

        findViewById(R.id.reveal_old_password_button).setOnClickListener(new View.OnClickListener() {
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
        findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!new DataAccess().deletePassword(password.getId())) {
                    Toast.makeText(PasswordDetailsActivity.this, "Could not delete the password", Toast.LENGTH_SHORT).show();
                }
                else {
                    finish();
                }
            }
        });

        users = new ArrayList<>();
        ArrayAdapter<String> usersAdapter = new ArrayAdapter<>(this, R.layout.part_owner_item, R.id.user_login, users);

        // check if the password belongs to the current user
        if(password.getUserId() == user.getId()) {
            usersAdapter.addAll(DataAccess.getInstance().getPartOwners(password.getId()));

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!shareEditText.getText().toString().isEmpty()) {
                        PasswordService passwordService = new PasswordService();
                        String result = passwordService.sharePassword(password, shareEditText.getText().toString());
                        Toast.makeText(PasswordDetailsActivity.this, result, Toast.LENGTH_SHORT).show();

                        usersAdapter.clear();
                        usersAdapter.addAll(DataAccess.getInstance().getPartOwners(password.getId()));
                        usersAdapter.notifyDataSetChanged();
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
        }

        ((ListView)findViewById(R.id.share_list)).setAdapter(usersAdapter);
    }
}