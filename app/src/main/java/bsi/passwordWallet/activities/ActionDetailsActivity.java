package bsi.passwordWallet.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import bsi.passwordWallet.ActivityLog;
import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.Password;
import bsi.passwordWallet.PasswordChange;
import bsi.passwordWallet.R;
import bsi.passwordWallet.User;
import bsi.passwordWallet.services.PasswordService;

public class ActionDetailsActivity extends AppCompatActivity {
    private ListView list;
    private LogAdapter adapter;
    private User user;
    private ArrayList<PasswordChange> passwordChanges;
    private Password password;

    class LogAdapter extends ArrayAdapter<ActivityLog> {
        private ArrayList<ActivityLog> dataSet;
        int clickedPosition = -1;
        public LogAdapter(ArrayList<ActivityLog> data, Context context) {
            super(context, R.layout.action_details_item, data);
            this.dataSet = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ActivityLog change = getItem(position);

            // check if the view is being reused
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.action_details_item, parent, false);
            }

            if(position != clickedPosition)
                convertView.setBackgroundColor(Color.WHITE);
            else
                convertView.setBackgroundColor(getColor(R.color.lightBlue));

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(change.getTime()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            TextView tv = convertView.findViewById(R.id.date);
            tv.setText(sdf.format(calendar.getTime()));

            tv = convertView.findViewById(R.id.previous_value);
            tv.setText(change.getPreviousValue() + "");

            tv = convertView.findViewById(R.id.current_value);
            tv.setText(change.getCurrentValue() + "");

            String actionType = change.getActionType();
            tv = convertView.findViewById(R.id.action);
            tv.setText(actionType);

            switch(actionType) {
                case ActivityLog.DELETE:
                    tv.setTextColor(Color.RED);
                    break;
                case ActivityLog.CREATE:
                    tv.setTextColor(getColor(R.color.green));
                    break;
                case ActivityLog.UPDATE:
                    tv.setTextColor(getColor(android.R.color.holo_orange_dark));
                    break;
                case ActivityLog.RECOVER:
                    tv.setTextColor(getColor(android.R.color.holo_green_dark));
                    break;
                case ActivityLog.SHARE:
                    tv.setTextColor(getColor(R.color.colorAccent));
                    break;
                default:
                    tv.setTextColor(getColor(R.color.dark_text));
            }

            return convertView;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        } catch (NullPointerException e) {
            finish();
        }

        TextView userButton = findViewById(R.id.user_button);

        try {
            Bundle intentBundle = getIntent().getExtras();
            long passwordId = intentBundle.getLong("password_id");
            user = intentBundle.getParcelable("user");
            password = DataAccess.getInstance().getPasswordById(passwordId);
            userButton.setText(user.getLogin());

        } catch(Exception e) {
            e.printStackTrace();
            finish();
        }

        list = findViewById(R.id.list);

        adapter = new LogAdapter(DataAccess.getInstance().getExtendedActivityLogs(password.getId()),this);
        list.setAdapter(adapter);
        findViewById(R.id.back_button).setOnClickListener(view -> finish());

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setBackgroundColor(getColor(R.color.lightBlue));
                adapter.clickedPosition = position;
                adapter.notifyDataSetChanged();
            }
        });
    }

}
