package com.division70.jobbe.jobbe_git;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giorgio on 03/11/15.
 */
public class RequestTimingListAdapter extends ArrayAdapter<String>{

    private final Context context;
    private final ArrayList<String> values;

    private String defTime1 = "Il prima possibile";

    public RequestTimingListAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        this.values = (ArrayList<String>) objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        textView.setText(values.get(position));

        String s = values.get(position);

        if (s.equals(RequestCompletionActivity.getDefTime()))
            enableRow(rowView);
        else
            disableRow(rowView);

        return rowView;
    }

    private void enableRow(View view){
        TextView text = (TextView) view.findViewById(R.id.label);
        ImageView image = (ImageView) view.findViewById(R.id.icon);

        text.setTextColor(Color.BLACK);
        image.setImageResource(R.drawable.tick);

        view.setEnabled(true);
    }

    private void disableRow(View view){
        TextView text = (TextView) view.findViewById(R.id.label);
        ImageView image = (ImageView) view.findViewById(R.id.icon);

        text.setTextColor(Color.GRAY);
        image.setImageResource(R.drawable.notick);

        view.setEnabled(false);
    }

}
