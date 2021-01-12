package bsi.passwordWallet.activities;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

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
import androidx.room.ForeignKey;
import bsi.passwordWallet.ActivityLog;
import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.R;
import bsi.passwordWallet.User;

public class ActionLogActivity extends AppCompatActivity {
    private ListView logList;
    private LogAdapter adapter;
    private User user;
    private HashMap<String, Boolean> actionFilters;
    private ArrayList<ActivityLog> activityLogs;
    private CheckBox viewCheckbox, updateCheckbox, createCheckbox, deleteCheckbox, shareCheckbox, recoverCheckbox;

    private ArrayList<CheckBox> checkBoxes;

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            filterLogs();
        }
    };

    class LogAdapter extends ArrayAdapter<ActivityLog> {
        private ArrayList<ActivityLog> dataSet;

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

            tv = convertView.findViewById(R.id.website);
            if(log.getCurrentValue().getWebsite() == null) {
                tv.setText(log.getPreviousValue().getWebsite());
            }
            else {
                tv.setText(log.getCurrentValue().getWebsite());
            }

            String actionType = log.getActionType();
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
        setContentView(R.layout.activity_action_log);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        } catch (NullPointerException e) {
            finish();
        }

        checkBoxes = new ArrayList<>(
                Arrays.asList(new CheckBox[] {
                        findViewById(R.id.view_checkbox),
                        findViewById(R.id.update_checkbox),
                        findViewById(R.id.create_checkbox),
                        findViewById(R.id.delete_checkbox),
                        findViewById(R.id.share_checkbox),
                        findViewById(R.id.recover_checkbox)
                }));

        actionFilters = new HashMap<>();

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

        findViewById(R.id.back_button).setOnClickListener(view -> finish());

        for(CheckBox checkBox : checkBoxes) {
            checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
        }

        logList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ActivityLog log =  adapter.getItem(position);
                if (user.getId() != log.getCurrentValue().getUserId()) {
                    Toast.makeText(
                            ActionLogActivity.this,
                            "Details can be displayed by owner only.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                Intent intent = new Intent(ActionLogActivity.this, ActionDetailsActivity.class);
                intent.putExtra("user", user);
                intent.putExtra("password_id", adapter.getItem(position).getPasswordId());
                startActivity(intent);
            }
        });

    }

    private void filterLogs() {
        for(CheckBox checkBox : checkBoxes) {
            actionFilters.put(checkBox.getText().toString(), checkBox.isChecked());
        }

        adapter.clear();
        for(ActivityLog log : activityLogs) {
            if(actionFilters.get(log.getActionType())) {
                adapter.add(log);
            }
            else {
                adapter.remove(log);
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityLogs = DataAccess.getInstance().getActivityLogs(user.getId());

        if(adapter == null) {
            adapter = new LogAdapter(new ArrayList<>(),this);
            logList.setAdapter(adapter);

        }
        else {
            adapter.clear();
        }

        adapter.addAll(DataAccess.getInstance().getActivityLogs(user.getId()));
        filterLogs();
        adapter.notifyDataSetChanged();
    }
}