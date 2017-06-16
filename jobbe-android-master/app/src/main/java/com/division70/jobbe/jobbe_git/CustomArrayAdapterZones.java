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

/**
 * Created by giorgio on 30/11/15.
 */
public class CustomArrayAdapterZones extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> values;

    public CustomArrayAdapterZones(Context context, int rowlayout, int label, ArrayList<String> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        textView.setText(values.get(position));
        // change the icon for Windows and iPhone

        if(ZoneChooserActivity.cityList.contains(textView.getText().toString())){
            listPainter(rowView, true);
        }
        else {
            listPainter(rowView, false);
        }

        return rowView;
    }

    private void listPainter(View view, boolean enabled){
        TextView text = (TextView) view.findViewById(R.id.label);
        ImageView image = (ImageView) view.findViewById(R.id.icon);
        Log.i("IntoListPainter", text.getText().toString() + " " + view.isEnabled());
        if(enabled){
            text.setTextColor(Color.BLACK);
            image.setImageResource(R.drawable.tick);
            view.setEnabled(true);
        }
        else {
            text.setTextColor(Color.GRAY);
            image.setImageResource(R.drawable.notick);
            view.setEnabled(false);
            Log.i("AfterDisable", text.getText().toString() + " " + view.isEnabled());
        }
    }
}


