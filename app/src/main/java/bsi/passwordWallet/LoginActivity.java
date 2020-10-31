package bsi.passwordWallet;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText loginInput;
    EditText passwordInput;
    Button signInButton;

    TextView confirmPasswordLabel;
    EditText confirmPasswordInput;
    RadioGroup encryptionRadioGroup;
    TextView changeAction;
    TextView promptLabel;

    View.OnClickListener signUpListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String login = loginInput.getText().toString();
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            ArrayList<String> validationResults = new ArrayList<>();
            validationResults.add(Validation.validatePassword(confirmPassword));
            validationResults.add(Validation.validatePassword(password));
            validationResults.add(Validation.validateLogin(login));

            int validationMessageIndex = -1;
            for(int i = 0; i < validationResults.size(); i++) {
                if(!validationResults.get(i).isEmpty())
                    validationMessageIndex = i;
            }

            if(validationMessageIndex != -1) {
                // inform the user and abort account creation
                displayToast(validationResults.get(validationMessageIndex));
                return;
            }

            if(!password.equals(confirmPassword)) {
                displayToast("Passwords don't match");
                return;
            }

            User user = DataAccess.getUser(login);
            // check if provided login is already in use
            if(user != null) {
                displayToast("Login already exists");
                return;
            }

            // generate random salt
            String salt = Encryption.generateSalt64();
            String encryptionMethod;
            String hash;

            Encryption encryption = new Encryption();
            // generate hash for chosen encryption method
            if(encryptionRadioGroup.getCheckedRadioButtonId() == R.id.SHA512)
                encryptionMethod = Encryption.SHA512;
            else
                encryptionMethod = Encryption.HMAC_SHA512;


            hash = calculateHash(encryption, encryptionMethod, password, salt);


            // create a user
            user = DataAccess.createUser(login, encryptionMethod, hash, salt);

            // check if the user was properly created
            if(user == null) {
                displayToast("Couldn't create user's account");
                return;
            }

            // call method for signing in
            signInListener.onClick(signInButton);
        }
    };

    View.OnClickListener signInListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String login = loginInput.getText().toString();
            String password = passwordInput.getText().toString();

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
            Encryption encryption = new Encryption();
            String encryptionMethod;
            if(user.getEncryptionMethod().equals(Encryption.SHA512))
                encryptionMethod = Encryption.SHA512;
            else
                encryptionMethod = Encryption.HMAC_SHA512;

            hash = calculateHash(encryption, encryptionMethod, password, user.getSalt());

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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* bind views */
        loginInput = findViewById(R.id.login_input);
        passwordInput = findViewById(R.id.password_input);
        signInButton = findViewById(R.id.signin_button);

        confirmPasswordLabel = findViewById(R.id.confirm_password_label);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        encryptionRadioGroup = findViewById(R.id.encryption_group);
        changeAction = findViewById(R.id.change_action_button);
        promptLabel = findViewById(R.id.prompt_label);

        changeAction.setPaintFlags(changeAction.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        // obscure the password input
        passwordInput.setTransformationMethod(new PasswordTransformationMethod());

        // uncomment this line for quick database deletion
         deleteDatabase(DatabaseOpenHelper.DATABASE_NAME);

        /* Make first call to the database. Create the database and tables if necessary. */
        DataAccess.initialize(this);

        signInButton.setOnClickListener(signInListener);

        findViewById(R.id.change_action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility;
                if(((TextView)v).getText().toString().equals(getString(R.string.sign_in))) {
                    visibility = View.GONE;
                    changeAction.setText(getString(R.string.sign_up));
                    promptLabel.setText(getString(R.string.do_not_have));
                    signInButton.setText(R.string.sign_in);
                    signInButton.setOnClickListener(signInListener);
                }
                else {
                    visibility = View.VISIBLE;
                    changeAction.setText(getString(R.string.sign_in));
                    promptLabel.setText(getString(R.string.already_have));
                    signInButton.setText(R.string.sign_up);
                    signInButton.setOnClickListener(signUpListener);
                }

                confirmPasswordLabel.setVisibility(visibility);
                confirmPasswordInput.setVisibility(visibility);
                encryptionRadioGroup.setVisibility(visibility);
            }
        });

    }

    public void displayToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataAccess.close();
    }

    boolean comparePasswords() {
        return true;
    }

    static String calculateHash(Encryption encryption, String encryptionMethod, String input, String salt) {
        String hash;
        if(encryptionMethod.equals(Encryption.SHA512)) {
            try {
                encryption.setMessageDigest(MessageDigest.getInstance(Encryption.SHA512));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            hash = encryption.calculateSHA512(input, salt, Encryption.PEPPER);

        }
        else {
            try {
                encryption.setMac(new Encryption.MacWrapper(Mac.getInstance(Encryption.HMAC_SHA512)));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            hash = encryption.calculateHMAC(
                    input,
                    new SecretKeySpec(salt.getBytes(StandardCharsets.UTF_8), Encryption.HMAC_SHA512),
                    Encryption.PEPPER
            );
        }

        return hash;
    }
}