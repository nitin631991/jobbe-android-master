package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by giorgio on 03/11/15.
 */
public class TimingListFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private ArrayList<String> values;
    private String defTime1 = "Il prima possibile";
    private String defTime2 = "Da concordare/non urgente";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initValues();

        RequestTimingListAdapter adapter = new RequestTimingListAdapter(getActivity(), R.layout.rowlayout, R.id.label, values);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    private void initValues(){
        this.values = new ArrayList<>();
        values.add(defTime1);
        values.add(defTime2);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        if(!view.isEnabled())
            switchRow(position);
    }

    private void switchRow(int position){
        if(position == 0){
            enableRow(getListView().getChildAt(0));
            disableRow(getListView().getChildAt(1));
        }
        else if(position == 1){
            enableRow(getListView().getChildAt(1));
            disableRow(getListView().getChildAt(0));
        }
        sendResult(position);
    }

    private void sendResult(int position){
        View item = getListView().getChildAt(position);
        TextView text = (TextView) item.findViewById(R.id.label);
        String newDefTime = text.getText().toString();
        Intent intent = new Intent();
        intent.putExtra("customDate", false);
        intent.putExtra("defTime", newDefTime);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
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