package bsi.passwordWallet.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import bsi.passwordWallet.DataAccess;
import bsi.passwordWallet.LoginLog;
import bsi.passwordWallet.R;
import bsi.passwordWallet.User;
import bsi.passwordWallet.services.LogService;

public class UserAccountActivity extends AppCompatActivity {
    private ListView logListView;
    private ListView blockedIPsListView;

    private LogAdapter logAdapter;
    private BlockedIPsAdapter blockedIPsAdapter;
    private User user;
    private TextView userButton;

    private static interface ReloadData {
        void reloadData();
    }

    private ReloadData reloadBlockedIPsData = new ReloadData() {
        @Override
        public void reloadData() {
            blockedIPsAdapter.setDataSet(DataAccess.getInstance().getBlockedIPs(user.getId()));
            blockedIPsAdapter.notifyDataSetChanged();
        }
    };

    static class LogAdapter extends ArrayAdapter<LoginLog> {
        private final ArrayList<LoginLog> dataSet;

        public LogAdapter(ArrayList<LoginLog> data, Context context) {
            super(context, R.layout.log_item, data);
            this.dataSet = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // get the data item for position
            LoginLog log = getItem(position);

            // check if the view is being reused
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.log_item, parent, false);
            }
            convertView.setTag(convertView.getId(), dataSet.get(position));

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(log.getLoginTime()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            TextView tv = convertView.findViewById(R.id.date);
            tv.setText(sdf.format(calendar.getTime()));

            tv = convertView.findViewById(R.id.ip);
            tv.setText(log.getIpAddress());

            tv = convertView.findViewById(R.id.result);
            tv.setText(log.getLoginResult());
            if(log.getLoginResult().equals(LogService.LOGIN_SUCCESS))
                tv.setTextColor(Color.rgb(16, 163, 8));
            else
                tv.setTextColor(Color.RED);

            return convertView;
        }
    }

    class BlockedIPsAdapter extends ArrayAdapter<String> {
        private ArrayList<String> dataSet;

        public void setDataSet(ArrayList<String> dataSet) {
            this.dataSet = dataSet;
        }

        View.OnClickListener onItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentView = (View)v.getParent();
                String ip = ((TextView)parentView.findViewById(R.id.ip)).getText().toString();
                if(DataAccess.getInstance().deleteBlockedIP(ip)) {
                    dataSet.remove(ip);
                    notifyDataSetChanged();
                }
            }
        };

        public BlockedIPsAdapter(ArrayList<String> data, Context context) {
            super(context, R.layout.blocked_item, data);
            this.dataSet = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // get the data item for position
            String ip = getItem(position);

            // check if the view is being reused
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.blocked_item, parent, false);
                ImageButton imButton = convertView.findViewById(R.id.delete);
                imButton.setOnClickListener(onItemClickListener);
            }
            convertView.setTag(convertView.getId(), dataSet.get(position));

            TextView tv = convertView.findViewById(R.id.ip);
            tv.setText(ip);

            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        user = getIntent().getExtras().getParcelable("user");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        } catch (NullPointerException e) {
            finish();
        }

        userButton = findViewById(R.id.user_button);
        userButton.setText(user.getLogin());

        logListView = findViewById(R.id.login_log_list);
        blockedIPsListView = findViewById(R.id.blocked_ip_list);

        logAdapter = new LogAdapter(DataAccess.getInstance().getLoginLogs(user.getId(), null, 20), this);
        logListView.setAdapter(logAdapter);
        blockedIPsAdapter = new BlockedIPsAdapter(DataAccess.getInstance().getBlockedIPs(user.getId()), this);
        blockedIPsListView.setAdapter(blockedIPsAdapter);

        findViewById(R.id.change_master_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: change master password
//                UserAccountDialog dialog =
//                    (UserAccountDialog)getSupportFragmentManager().findFragmentByTag("UserAccount");
//                if(dialog == null) {
//                    dialog = new UserAccountDialog(user, userPassword, userPasswordModifiedListener);
//                    dialog.show(getSupportFragmentManager(), "UserAccount");
//                }
            }
        });

    }
}
