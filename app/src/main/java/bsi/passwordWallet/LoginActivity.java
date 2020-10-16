package bsi.passwordWallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /** bind views */
        final EditText loginInput = findViewById(R.id.login_input);
        final EditText passwordInput = findViewById(R.id.password_input);
        final RadioGroup encryptionRadioGroup = findViewById(R.id.encryption_group);

        // obscure the password input
        passwordInput.setTransformationMethod(new PasswordTransformationMethod());

        //deleteDatabase(DatabaseOpenHelper.DATABASE_NAME);

        /** Make first call to the database. Create the database and tables if necessary. */
        DataAccess.initialize(this);

        findViewById(R.id.signup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = loginInput.getText().toString();
                String password = passwordInput.getText().toString();
                User user;

                String message = Validation.validateLogin(login);
                // check if the login has invalid format
                if(!message.isEmpty()) {
                    // inform the user and abort sign up process
                    displayToast(message);
                    return;
                }

                message = Validation.validatePassword(password);
                // check if the password has invalid format
                if(!message.isEmpty()) {
                    displayToast(message);
                    return;
                }

                user = DataAccess.getUser(login);
                // check if provided login is already in use
                if(user != null) {
                    displayToast("Login already exists");
                    return;
                }

                // generate random salt
                String salt = Encryption.generateSalt();
                String encryptionMethod;
                String passwordHash;

                // TODO: pepper

                // generate hash for chosen encryption method
                if(encryptionRadioGroup.getCheckedRadioButtonId() == R.id.SHA256) {
                    encryptionMethod = Encryption.SHA256;
                    passwordHash = Encryption.calculateSHA265(password, salt, null);
                }
                else {
                    encryptionMethod = Encryption.HMAC;
                    passwordHash = Encryption.calculateHMAC(password, salt, null);
                }

                // create a user
                user = DataAccess.createUser(login, encryptionMethod, passwordHash, salt);

                // check if the user was properly created
                if(user == null) {
                    displayToast("Couldn't create user's account");
                    return;
                }

                // call method for signing in
                findViewById(R.id.signin_button).callOnClick();
            }
        });

        findViewById(R.id.signin_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              String login = loginInput.getText().toString();
              String password = passwordInput.getText().toString();
//                String login = "user1";
//                String password = "123";

                String message = Validation.validateLogin(login);
                // check if the login has invalid format
                if(!message.isEmpty()) {
                    // inform the user and abort sign up process
                    displayToast(message);
                    return;
                }

                message = Validation.validatePassword(password);
                // check if the password has invalid format
                if(!message.isEmpty()) {
                    displayToast(message);
                    return;
                }

                // try to get user with provided login
                User user = DataAccess.getUser(login);
                // check if the user was found
                if(user == null) {
                    displayToast("User doesn't exist"); // inform user and abort sign in process
                    return;
                }

                String hash;
                if(user.getEncryptionMethod().equals(Encryption.SHA256))
                    hash = Encryption.calculateSHA265(password, user.getSalt(), null);
                else
                    hash = Encryption.calculateHMAC(password, user.getSalt(), null);

                // check if provided password is valid
                if(hash.equals(user.getPassword())) {
                    Intent intent = new Intent(getApplicationContext(), WalletActivity.class);
                    // pass User instance to next activity
                    intent.putExtra("user", user);
                    // pass plain (unencrypted) password
                    intent.putExtra("user_password", password);
                    // go to the WalletActivity
                    startActivity(intent);
                }
                else {
                    // inform the user that credentials were incorrect
                    displayToast("Incorrect password");
                }
            }
        });
    }

    void displayToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataAccess.close();
    }
}