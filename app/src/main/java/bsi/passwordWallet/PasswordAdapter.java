package bsi.passwordWallet;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PasswordAdapter extends ArrayAdapter<Password> {
    private ArrayList<Password> dataSet;

    public PasswordAdapter(ArrayList<Password> data, Context context) {
        super(context, R.layout.wallet_item, data);
        this.dataSet = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get the data item for position
        Password password = getItem(position);

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
