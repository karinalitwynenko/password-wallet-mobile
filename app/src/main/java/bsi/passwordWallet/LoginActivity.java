package bsi.passwordWallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText loginInput = findViewById(R.id.login_input);
        final EditText passwordInput = findViewById(R.id.password_input);
        final RadioGroup encryptionRadioGroup = findViewById(R.id.encryption_group);

        DataAccess da = new DataAccess(getApplicationContext());

        findViewById(R.id.signup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = loginInput.getText().toString();
                String password = passwordInput.getText().toString();
                User user;
                String feedback;

                if(Validation.validateLogin(login) && Validation.validatePassword(password)) {
                    user = DataAccess.getUser(login);
                    // check if login is already in use
                    if(user != null) {
                        toast = Toast.makeText(getApplicationContext(), "Login already exists", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }

                    // get chosen encryption method
                    String encryptionMethod = encryptionRadioGroup.getCheckedRadioButtonId() == R.id.SHA256 ?
                            Encryption.SHA256 : Encryption.HMAC;

                    String salt = Encryption.generateSalt();

                    user = DataAccess.createUser(
                            login,
                            encryptionMethod,
                            Encryption.encryptSHA265(password, salt, null),
                            salt
                    );

                    if(user == null) {
                        feedback = "Couldn't create user's account.";
                        return;
                    }

                    Intent intent = new Intent(getApplicationContext(), WalletActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.signin_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), WalletActivity.class);
                intent.putExtra("user", new User(0, "pajonk", "", "2332", "23"));
                startActivity(intent);
//                String login = loginInput.getText().toString();
//                String password = passwordInput.getText().toString();
//                String feedback;
//                if(Validation.validateLogin(login) && Validation.validatePassword(password)) {
//
//                    User user = DataAccess.getUser(login);
//                    if(user == null) {
//                        toast = Toast.makeText(getApplicationContext(), "User doesn't exist", Toast.LENGTH_SHORT);
//                        toast.show();
//                    }
//                    else if(User.loginUser(user, password)){
//                        Intent intent = new Intent(getApplicationContext(), WalletActivity.class);
//                        intent.putExtra("user", user);
//                        startActivity(intent);
//                    }
//                    else {
//                        toast = Toast.makeText(getApplicationContext(), "Incorrect user credentials", Toast.LENGTH_SHORT);
//                        toast.show();
//                    }
//                }


            }
        });
    }



}