package bsi.passwordWallet;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PasswordAdapter extends ArrayAdapter<Password> {
    private ArrayList<Password> dataSet;
    Context context;

    public PasswordAdapter(ArrayList<Password> data, Context context) {
        super(context, R.layout.wallet_item, data);
        this.dataSet = data;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get the data item for position
        Password password = getItem(position);
        final View result;

        // check if the view is being reused
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.wallet_item, parent, false);
        }

        TextView tv = convertView.findViewById(R.id.website);
        tv.setText(password.getWebsite());

        convertView.setTag(convertView.getId(), dataSet.get(position));
        tv = convertView.findViewById(R.id.description);
        tv.setText(password.getDescription());

        // Return the completed view to render on screen
        return convertView;
    }
}
