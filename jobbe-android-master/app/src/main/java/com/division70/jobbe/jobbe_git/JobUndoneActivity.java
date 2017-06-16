package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.fitness.service.SensorEventDispatcher;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by giorgio on 12/11/15.
 */
public class JobUndoneActivity extends Activity{

    private ListView list;
    private EditText note;
    private Button confirm;

    private String reqId;
    private String clientId;

    private ParseObject request;
    private ParseObject supplier;
    private ParseObject client;

    private String[] motivationArraySupplier = {"Mancanza di accordo per l'appuntamento",
                                    "Il cliente ha rinunciato",
                                    "Non posso più prendermi l'incarico",
                                    "Ho cambiato lavoro",
                                    "Altro"};

    private String[] motivationArrayClient = {"Mancanza di accordo con il fornitore",
                                    "Il fornitore non si è presentato",
                                    "Il fornitore non ha risolto il problema",
                                    "Non ho più bisogno",
                                    "Altro"};


    private String choosenMotivation = ""; //Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jobundone_layout);

        sendScreenAnalytics();

        this.init();
    }

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("CLIENTE - RICHIESTA - RICHIESTA NON COMPLETATA");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void init(){
        try {
            this.list = (ListView) findViewById(R.id.job_undone_list);
            this.note = (EditText) findViewById(R.id.job_undone_note);
            this.confirm = (Button) findViewById(R.id.confirm_job_undone);

            this.reqId = getIntent().getStringExtra("reqId");
            this.clientId = getIntent().getStringExtra("clientId");

            this.request = ParseObject.createWithoutData("Request", this.reqId);
            this.request.fetch();

            this.supplier = (ParseObject) this.request.get("choosenSupplier");
            supplier.fetchIfNeeded();

            this.client = (ParseObject) this.request.get("userId");
            this.client.fetchIfNeeded();

            String userType = getIntent().getStringExtra("userType");
            switch (userType) {
                case "client":
                    this.choosenMotivation = motivationArrayClient[0];
                    renderList(motivationArrayClient);
                    setUpList(motivationArrayClient);
                    setListeners(motivationArrayClient);
                    break;
                case "supplier":
                    this.choosenMotivation = motivationArraySupplier[0];
                    renderList(motivationArraySupplier);
                    setUpList(motivationArraySupplier);
                    setListeners(motivationArraySupplier);
                    break;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void renderList(String[] motivationArray){
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.rowlayout, R.id.label, motivationArray);
        this.list.setAdapter(adapter);
        setListViewHeightBasedOnChildren();
    }

    private void setUpList(final String[] motivationArray){
        this.list.post(new Runnable() {
            @Override
            public void run() {
                makeListSelection(motivationArray, 0); //Enabling the first one by default
            }
        });
    }

    private void setListeners(final String[] motivationArray) {
        this.confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNoteImput(motivationArray)) {
                    String userType = getIntent().getStringExtra("userType");
                    switch (userType) {
                        case "client":
                            setJobTerminatedByClient();
                            toClientHome();
                            break;
                        case "supplier":
                            setJobTerminatedBySupplier();
                            toSupplierHome();
                            break;
                    }
                }
            }
        });

        this.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkRow(motivationArray, view, position);
            }
        });
    }

    private void setJobTerminatedByClient(){
        Map<String, Object> incomplete = new HashMap<>();
        incomplete.put("reqId", request.getObjectId());
        incomplete.put("oldState", request.getInt("state"));
        //incomplete.put("uncompletionNote", choosenMotivation);
        incomplete.put("uncompletionNoteClient", choosenMotivation);
        //incomplete.put("clientId", client.getObjectId());
        incomplete.put("supplierDisplayName", supplier.get("displayName"));
        incomplete.put("reqTitle", request.get("title"));
        try {
            ParseCloud.callFunction("incompleteRequest", incomplete); //Incomplete request by CLIENT
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setJobTerminatedBySupplier(){
        Map<String, Object> incomplete = new HashMap<>();
        incomplete.put("reqId", request.getObjectId());
        incomplete.put("oldState", request.getInt("state"));
        incomplete.put("uncompletionNote", choosenMotivation);
        //incomplete.put("uncompletionNoteClient", choosenMotivation);
        incomplete.put("clientId", client.getObjectId());
        incomplete.put("supplierDisplayName", supplier.get("displayName"));
        incomplete.put("reqTitle", request.get("title"));
        try {
            ParseCloud.callFunction("incompleteRequest", incomplete); //Incomplete request by CLIENT
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private boolean checkNoteImput(String[] motivationArray){
        View item = getViewByPosition(motivationArray.length - 1, this.list);
        if(item.isEnabled()) {
            if (this.note.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Inserisci il motivo specifico",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private void checkRow(String[] motivationArray, View view, int position){
        if (!view.isEnabled()) {
            makeListSelection(motivationArray, position);
            if(position == motivationArray.length - 1)
                switchNoteState(true);
            else
                switchNoteState(false);
        }
        else
            ; //Do nothing
        TextView text = (TextView) view.findViewById(R.id.label);
        this.choosenMotivation = text.getText().toString();
        Log.i("itemText", this.choosenMotivation);
    }

    private void listPainter(View view, boolean enabled){
        TextView text = (TextView) view.findViewById(R.id.label);
        ImageView image = (ImageView) view.findViewById(R.id.icon);
        if(enabled){
            text.setTextColor(Color.BLACK);
            image.setImageResource(R.drawable.tick);
            view.setEnabled(true);
        }
        else {
            text.setTextColor(Color.GRAY);
            image.setImageResource(R.drawable.notick);
            view.setEnabled(false);
        }
    }

    private void makeListSelection(String[] motivationArray, int position){
        for (int i = 0; i < motivationArray.length; i++)
            listPainter(getViewByPosition(i, this.list), false); //Setting all disabled
        listPainter(getViewByPosition(position, list), true);
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private void switchNoteState(boolean state){
        if(state)
            this.note.setVisibility(View.VISIBLE);
        else {
            this.note.setVisibility(View.INVISIBLE);
            this.note.setText("");
        }
    }


    public void setListViewHeightBasedOnChildren() {
        ListAdapter listAdapter = this.list.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(this.list.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = getViewByPosition(i, this.list);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = this.list.getLayoutParams();
        params.height = totalHeight + (this.list.getDividerHeight() * (listAdapter.getCount() - 1)) + view.getMeasuredHeight();
        this.list.setLayoutParams(params);
    }

    private void toSupplierHome(){
        Intent intent = new Intent(JobUndoneActivity.this, HomeSupplierActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void toClientHome(){
        Intent intent = new Intent(JobUndoneActivity.this, HomeClientActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private int getUserRequestsNumber(ParseUser user) {
        List<ParseObject> list;
        ArrayList<Integer> illegalStates = new ArrayList<Integer>();
        illegalStates.add(4);
        illegalStates.add(5);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("userId", user);
        query.whereNotContainedIn("state", illegalStates);
        query.whereNotEqualTo("confirmUncompleteReq", true);
        query.include("job");
        query.include("jobCatId");
        query.include("choosenProposal");
        query.include("choosenSupplier");
        query.orderByDescending("createdAt");
        try {
            list = query.find();
            return list.size();
        }
        catch (ParseException e){
            Log.i("REQUEST_LIST", "FAILED");
            Log.i("ParseException",e.toString());
            ParseErrorHandler.handleParseError(e, getApplicationContext());
        }
        return 0;
    }


    @Override
    protected void onResume() {
        super.onResume();
        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }

}
