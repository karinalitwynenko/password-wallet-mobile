package bsi.passwordWallet.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import bsi.passwordWallet.ActivityLog;
import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.Encryption;
import bsi.passwordWallet.LoginLog;
import bsi.passwordWallet.Password;
import bsi.passwordWallet.R;
import bsi.passwordWallet.User;
import bsi.passwordWallet.services.LogService;

public class ActionLogActivity extends AppCompatActivity {
    private ListView logList;
    private LogAdapter adapter;
    private User user;

    class LogAdapter extends ArrayAdapter<ActivityLog> {
        private final ArrayList<ActivityLog> dataSet;

        public LogAdapter(ArrayList<ActivityLog> data, Context context) {
            super(context, R.layout.log_item, data);
            this.dataSet = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ActivityLog log = getItem(position);

            // check if the view is being reused
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.activity_log_item, parent, false);
            }

            convertView.setTag(convertView.getId(), dataSet.get(position));

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(log.getTime()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            TextView tv = convertView.findViewById(R.id.date);
            tv.setText(sdf.format(calendar.getTime()));

            tv = convertView.findViewById(R.id.action);
            tv.setText(log.getFunction());

            if(log.getFunction().equals(ActivityLog.DELETE)) {
                tv.setTextColor(Color.RED);
            }
            else if(log.getFunction().equals(ActivityLog.CREATE)) {
                tv.setTextColor(getColor(android.R.color.holo_green_dark));
            }
            else if(log.getFunction().equals(ActivityLog.SHARE)) {
                tv.setTextColor(getColor(R.color.colorAccent));
            }
            else {
                tv.setTextColor(getColor(R.color.dark_text));
            }

            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_log);
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
            user = intentBundle.getParcelable("user");
            userButton.setText(user.getLogin());

        } catch(Exception e) {
            e.printStackTrace();
            finish();
        }

        logList = findViewById(R.id.log_list);
        adapter = new LogAdapter(DataAccess.getInstance().getActivityLogs(user.getId()), this);
        logList.setAdapter(adapter);

        findViewById(R.id.back_button).setOnClickListener(view -> finish());

    }


}