package bsi.passwordWallet;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PasswordAdapter extends ArrayAdapter<Password> implements View.OnClickListener {
    private ArrayList<Password> dataSet;
    Context context;

    public PasswordAdapter(ArrayList<Password> data, Context context) {
        super(context, R.layout.wallet_item, data);
        this.dataSet = data;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        int position=(Integer) v.getTag();
        Object object= getItem(position);
//        DataModel dataModel=(DataModel)object;
//
//        switch (v.getId())
//        {
//            case R.id.item_info:
//                Snackbar.make(v, "Release date " +dataModel.getFeature(), Snackbar.LENGTH_LONG)
//                        .setAction("No action", null).show();
//                break;
//        }
    }

    private int lastPosition = -1;

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
        tv.setText(password.website);

        // Return the completed view to render on screen
        return convertView;
    }
}
