package bsi.passwordWallet.activities;

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

import androidx.appcompat.app.AppCompatActivity;
import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.DatabaseOpenHelper;
import bsi.passwordWallet.Encryption;
import bsi.passwordWallet.R;
import bsi.passwordWallet.User;
import bsi.passwordWallet.services.UserService;

public class LoginActivity extends AppCompatActivity {
    EditText loginInput;
    EditText passwordInput;
    Button signInButton;

    TextView confirmPasswordLabel;
    EditText confirmPasswordInput;
    RadioGroup encryptionRadioGroup;
    TextView changeAction;
    TextView promptLabel;

    TextView ipAddressLabel;
    EditText ipAddressInput;

    View.OnClickListener changeActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int singUpViewsVisibility;
            int ipViewsVisibility;
            if(((TextView)v).getText().toString().equals(getString(R.string.sign_in))) {
                singUpViewsVisibility = View.GONE;
                ipViewsVisibility = View.VISIBLE;
                changeAction.setText(getString(R.string.sign_up));
                promptLabel.setText(getString(R.string.do_not_have));
                signInButton.setText(R.string.sign_in);
                signInButton.setOnClickListener(signInListener);
            }
            else {
                singUpViewsVisibility = View.VISIBLE;
                ipViewsVisibility = View.GONE;
                changeAction.setText(getString(R.string.sign_in));
                promptLabel.setText(getString(R.string.already_have));
                signInButton.setText(R.string.sign_up);
                signInButton.setOnClickListener(signUpListener);
            }

            confirmPasswordLabel.setVisibility(singUpViewsVisibility);
            confirmPasswordInput.setVisibility(singUpViewsVisibility);
            encryptionRadioGroup.setVisibility(singUpViewsVisibility);

            ipAddressLabel.setVisibility(ipViewsVisibility);
            ipAddressInput.setVisibility(ipViewsVisibility);
        }
    };

    View.OnClickListener signUpListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String encryptionMethod;

            if(encryptionRadioGroup.getCheckedRadioButtonId() == R.id.SHA512)
                encryptionMethod = Encryption.SHA512;
            else
                encryptionMethod = Encryption.HMAC_SHA512;

            User user;

            try {
                user = new UserService().signUp(
                        loginInput.getText().toString(),
                        passwordInput.getText().toString(),
                        confirmPasswordInput.getText().toString(),
                        encryptionMethod
                );
            } catch (UserService.UserAccountException e) {
                displayToast(e.getMessage());
                e.printStackTrace();
                return;
            }

            // check if the user was properly created
            if(user != null)
                changeAction.performClick();

        }
    };

    View.OnClickListener signInListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String password = passwordInput.getText().toString();
            User user;
            try {
                user = new UserService().signIn(
                        loginInput.getText().toString(),
                        password,
                        ipAddressInput.getText().toString()
                );
            } catch (UserService.UserAccountException e) {
                displayToast(e.getMessage());
                e.printStackTrace();
                return;
            }

            if(user != null) {
                Intent intent = new Intent(getApplicationContext(), WalletActivity.class);
                // pass User instance to next activity
                intent.putExtra("user", user);
                // pass plain (unencrypted) password
                intent.putExtra("user_password", password);
                // go to the WalletActivity
                startActivity(intent);
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

        ipAddressLabel = findViewById(R.id.ip_label);
        ipAddressInput = findViewById(R.id.ip_input);

        changeAction.setPaintFlags(changeAction.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        // obscure the password input
        passwordInput.setTransformationMethod(new PasswordTransformationMethod());

        // uncomment this line for quick database deletion
//        deleteDatabase(DatabaseOpenHelper.DATABASE_NAME);

        /* Make first call to the database. Create the database and tables if necessary. */
        DataAccess.initialize(new DatabaseOpenHelper(this).getWritableDatabase());

        signInButton.setOnClickListener(signInListener);
        changeAction.setOnClickListener(changeActionListener);
    }

    public void displayToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataAccess.close();
    }

}