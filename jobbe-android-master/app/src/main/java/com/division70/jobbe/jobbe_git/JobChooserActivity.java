package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by giorgio on 25/10/15.
 */
public class JobChooserActivity extends Activity {

    private ListView jobsListView;
    private ListView tagsListView;
    private ListView jobsFromTagsListView;
    private ArrayList<ParseObject> jobCat;
    private ArrayList<ParseObject> jobTags;
    private ArrayList<String> jobTagNames;
    private ArrayList<String> jobsList;
    private ArrayList<HashMap<String,Object>> jobsArray;
    private TextView presCat;
    private EditText jobTagSuggest;
    private RelativeLayout jobChooserBodyLayout;
    private LinearLayout catLinearLayout;
    private String iconPath;
    private ArrayList<JobCategoryRelative> catItemList;
    private JobCategoryRelative selectedItem;
    private RelativeLayout jobsFromTagsLayout;
    private RelativeLayout spinnerLayout;

    private String selectedCategory = "";

    private boolean textChangedEnabled = true;

    private boolean tooltip1 = false;
    private boolean tooltip2 = false;
    private boolean tooltip3 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jobchooser_layout);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setUpActionBar();
        new initAsync().execute();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        switch (getIntent().getStringExtra("purpose")) {
            case "requestCompletion":
                sendScreenAnalytics("CLIENTE - CREA RICHIESTA - SELEZIONE LAVORO");
                customActionbar.setTitle("Di cosa hai bisogno?");
                break;
            case "supplierSignup":
                sendScreenAnalytics("SIGNUP - FORNITORE SELEZIONE LAVORO");
                customActionbar.setTitle("Seleziona lavoro");
                break;
        }

        customActionbar.disableSubtitle();
        actionBar.setCustomView(customActionbar);
        actionBar.setDisplayShowCustomEnabled(true);

        ImageView back = (ImageView) customActionbar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void sendScreenAnalytics(String screenName){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private boolean checkIfFirstTime(String element){

        SharedPreferences settings = getPreferences(0);
        boolean first = settings.getBoolean(element + "isFirstTime", true);

        switch(element){
            case "search":
                this.tooltip1 = first;
                break;
            case "category":
                this.tooltip2 = first;
                break;
            case "job":
                this.tooltip3 = first;
                break;
        }
        return first;
    }

    private void unsetFirstTime(String element){
        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(element + "isFirstTime", false);
        editor.commit();
    }

    private void showTooltipLayout(int id){
        com.venmo.view.TooltipView tooltipLayout;
        switch(id){
            case 1:
                tooltipLayout = (com.venmo.view.TooltipView) findViewById(R.id.tooltip1);
                tooltipLayout.setVisibility(View.VISIBLE);
                break;
            case 2:
                tooltipLayout = (com.venmo.view.TooltipView) findViewById(R.id.tooltip2);
                tooltipLayout.setVisibility(View.VISIBLE);
                break;
            case 3:
                tooltipLayout = (com.venmo.view.TooltipView) findViewById(R.id.tooltip3);
                tooltipLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void hideTooltipLayout(String element, int id){

        unsetFirstTime(element); //Disable tooltip from now on

        com.venmo.view.TooltipView tooltipLayout;
        switch(id){
            case 1:
                tooltipLayout = (com.venmo.view.TooltipView) findViewById(R.id.tooltip1);
                tooltipLayout.setVisibility(View.GONE);
                this.tooltip1 = false;
                break;
            case 2:
                tooltipLayout = (com.venmo.view.TooltipView) findViewById(R.id.tooltip2);
                tooltipLayout.setVisibility(View.GONE);
                this.tooltip2 = false;
                break;
            case 3:
                tooltipLayout = (com.venmo.view.TooltipView) findViewById(R.id.tooltip3);
                tooltipLayout.setVisibility(View.GONE);
                this.tooltip3 = false;
                break;
        }
    }

    private void initCategoryList(){
        for(int i = 0; i < this.jobCat.size(); i++){
            final int index = i;
            ParseObject cat = this.jobCat.get(i);

            if(!cat.get("name").toString().equals("Altro")){  // By GST ............................................................................................
                final JobCategoryRelative item = new JobCategoryRelative(getApplicationContext());
                this.catItemList.add(item);
                String catName = cat.get("name").toString();
                final String catPic = cat.get("icon").toString().replace("-", "_");
                item.setCategory(catName, catPic);
                final String listColor = cat.get("color").toString();
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(tooltip2)
                            hideTooltipLayout("category", 2);

                        selectedCategory = item.getCategoryName();
                        selectCategory(item);
                        jobsArray = (ArrayList<HashMap<String, Object>>) jobCat.get(index).get("jobs");
                        setSelectionLabel(jobsList.get(index));

                        //Making "Altro" listview text BLACK, otherwise WHITE
                        if(selectedCategory.equals("Altro"))
                        {
                            renderList(true);
                            presCat.setTextColor(Color.BLACK);
                        }
                        else {
                            renderList(false);
                            presCat.setTextColor(Color.WHITE);
                        }

                        RelativeLayout body_layout = (RelativeLayout) findViewById(R.id.body_layout);
                        body_layout.setBackgroundColor(Color.parseColor("#" + listColor));

                        RelativeLayout text_layout = (RelativeLayout) findViewById(R.id.text_layout);
                        text_layout.setBackgroundColor(Color.parseColor("#" + listColor));

                        iconPath = catPic;

                        hideKeyboard();
                    }
                });
                this.catLinearLayout.addView(item);
                if(i == 0)
                    item.callOnClick();
            }
        }
    }

    private void selectCategory(JobCategoryRelative item){
        if(this.selectedItem != null)
            this.selectedItem.deselect();
        item.select();
        this.selectedItem = item;
    }

    private void setSuggesterListener(){
        this.jobTagSuggest.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (textChangedEnabled) {
                    if (s.length() != 0) {
                        jobsFromTagsLayout.setVisibility(View.GONE);
                        ArrayList<String> tmp = getSuggestedTags(s.toString());
                        if (tmp != null) {
                            renderTagList(tmp);
                            setTagsListViewListener();
                            tagsListView.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(getApplicationContext(), "Nessun tag corrispondente", Toast.LENGTH_LONG).show();
                            showJobsCategories();
                            tagsListView.setVisibility(View.GONE);
                        }
                    } else {
                        //jobChooserBodyLayout.setVisibility(View.VISIBLE);
                        showJobsCategories();
                        tagsListView.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void renderJobsFromTagsListView(ArrayList<String> values){
        ArrayAdapter adapter = new ArrayAdapter<>(this,
                R.layout.job_tags_list_layout, R.id.label, values);
        this.jobsFromTagsListView.setAdapter(adapter);
    }

    private void setTagsListViewListener(){
        this.tagsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                textChangedEnabled = false;
                jobTagSuggest.setText(tagsListView.getItemAtPosition(position).toString());
                textChangedEnabled = true;
                jobTagSuggest.setSelection(jobTagSuggest.getText().toString().length());
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                ArrayList<HashMap<String, Object>> jobs = new ArrayList<HashMap<String, Object>>();
                for (ParseObject obj : jobTags)
                    if (obj.get("name").toString().equals(tagsListView.getItemAtPosition(position).toString())) {
                        jobs = (ArrayList<HashMap<String, Object>>) obj.get("jobs");
                        break;
                    }
                ArrayList<String> tmp = new ArrayList<String>();
                for (HashMap<String, Object> jsonJob : jobs) {
                    if (jsonJob.get("name") != null) {
                        tmp.add(jsonJob.get("name").toString());
                        Log.i("JOB:", jsonJob.get("name").toString());
                    }
                }

                //Get tracker.
                Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
                //Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.analytics_cat_pref_client))
                        .setAction(getString(R.string.analytics_action_search))
                        .setLabel(jobTagSuggest.getText().toString().toLowerCase())
                        .build());

                renderJobsFromTagsListView(tmp);
                showJobsFromTags();
                setJobsFromTagsListListeners(jobs);
            }
        });
    }

    private void setJobsFromTagsListListeners(final ArrayList<HashMap<String, Object>> jobs){
        this.jobsFromTagsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, Object> jsonJobCat = jobs.get(position);
                jsonJobCat = (HashMap<String, Object>) jsonJobCat.get("categoryListId");
                try {
                    String catId = jsonJobCat.get("objectId").toString();
                    ArrayList<ParseObject> cat = (ArrayList<ParseObject>) ParseQuery.getQuery("CategoryList").whereEqualTo("objectId", catId).find();
                    if (cat != null) {
                        selectedCategory = cat.get(0).getString("name");
                        iconPath = cat.get(0).get("icon").toString().replace("-", "_");
                        Log.i("SELECTED CAT:", selectedCategory);
                        switch (getIntent().getStringExtra("purpose")) {
                            case "requestCompletion":
                                startRequestCompletion(jobsFromTagsListView, position, false);
                                break;
                            case "supplierSignup":
                                startSupplierSignup(jobsFromTagsListView, position);
                                break;
                        }
                    }
                } catch (ParseException e) {
                    ParseErrorHandler.handleParseError(e, getApplicationContext());
                    e.printStackTrace();
                }
            }
        });
    }

    private void showJobsFromTags(){
        TextView txt = (TextView) findViewById(R.id.textView53);
        RelativeLayout iconsLayout = (RelativeLayout) findViewById(R.id.icons_layout);
        RelativeLayout jobBodyLayout = (RelativeLayout) findViewById(R.id.job_body_layout);
        ListView tagsListView = (ListView) findViewById(R.id.tags_list_view);
        tagsListView.setVisibility(View.GONE);
        txt.setVisibility(View.GONE);
        iconsLayout.setVisibility(View.GONE);
        jobBodyLayout.setVisibility(View.GONE);
        jobsFromTagsLayout.setVisibility(View.VISIBLE);

    }

    private void showJobsCategories(){
        TextView txt = (TextView) findViewById(R.id.textView53);
        RelativeLayout iconsLayout = (RelativeLayout) findViewById(R.id.icons_layout);
        RelativeLayout jobBodyLayout = (RelativeLayout) findViewById(R.id.job_body_layout);
        ListView tagsListView = (ListView) findViewById(R.id.tags_list_view);
        tagsListView.setVisibility(View.VISIBLE);
        txt.setVisibility(View.VISIBLE);
        iconsLayout.setVisibility(View.VISIBLE);
        jobBodyLayout.setVisibility(View.VISIBLE);
        jobsFromTagsLayout.setVisibility(View.GONE);
    }

    private ArrayList<String> getSuggestedTags(String word){
        ArrayList<String> tmp = new ArrayList<>();
        for(String tag : this.jobTagNames)
           // if(tag.toLowerCase().startsWith(word.toLowerCase())) //Checking with IGNORECASE to get all tags
         //   if(tag.toLowerCase().contains(word.toLowerCase())) //Checking with IGNORECASE to get all tags
                if(tag.toLowerCase().indexOf(word.toLowerCase()) >= 0)
                tmp.add(tag);
        if(tmp.size() == 0)
            return null;
        else
            return tmp;
    }

    private void renderTagList(ArrayList<String> values){
        Collections.sort(values);
        ArrayAdapter adapter = new ArrayAdapter<>(this,
                R.layout.job_tags_list_layout, R.id.label, values);

        this.tagsListView.setAdapter(adapter);
    }

    private void renderList(boolean neededBlack){
        int label_id = 0;

        if(neededBlack)
            label_id = R.id.label_black;
        else
            label_id = R.id.label;

        ArrayAdapter adapter = new ArrayAdapter<>(this,
               R.layout.job_listview_layout, label_id, this.getJobsList());
        this.jobsListView.setAdapter(adapter);
    }

    private ArrayList<String> getJobsList(){
        ArrayList<String> tmp = new ArrayList<>();
        int j = 0;
        for(HashMap<String,Object> obj : this.jobsArray)
            tmp.add(this.jobsArray.get(j++).get("name").toString());
        Collections.sort(tmp);
        return tmp;
    }

    private void getJobs(){
        Map<String,Object> empty = new HashMap<String, Object>() {};
        HashMap<String, Object> result = new HashMap<String, Object>() {};
        try {
            result = ParseCloud.callFunction("getJobs", empty); //Synchronous call in order to wait until categories retrieval
            this.jobCat = (ArrayList<ParseObject>) result.get("jobCategory");
            this.jobTags = (ArrayList<ParseObject>) result.get("tag");
            this.getCategories();
            this.getTagNames();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void getTagNames(){
        for(ParseObject obj : jobTags)
            this.jobTagNames.add(obj.get("name").toString());
    }

   /* private void getCategories(){
        for(ParseObject obj : jobCat)
            this.jobsList.add(obj.get("name").toString());
    }*/
    private void getCategories(){
        for(ParseObject obj : jobCat){

            if(!obj.get("name").toString().equals("Altro")){
                //  System.out.println(" getCat   ====  " + obj.get("name").toString());
                this.jobsList.add(obj.get("name").toString());
            }

        }

    }
    private void setListViewListener(){
        this.jobsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(tooltip1)
                    hideTooltipLayout("search", 1);
                if(tooltip2)
                    hideTooltipLayout("category", 2);
                if(tooltip3)
                    hideTooltipLayout("job", 3);

                switch (getIntent().getStringExtra("purpose")) {
                    case "requestCompletion":
                        startRequestCompletion(jobsListView, position, true);
                        break;
                    case "supplierSignup":
                        startSupplierSignup(jobsListView, position);
                        break;
                }
            }
        });
    }

    private void startRequestCompletion(ListView list, int position, boolean analytics){
        String selectedJob = list.getItemAtPosition(position).toString();
        Intent intent = new Intent(getApplicationContext(), RequestCompletionActivity.class);
        intent.putExtra("selectedJob", selectedJob);
        intent.putExtra("jobIconPath", this.iconPath);
        intent.putExtra("category", this.selectedCategory);

        if(analytics) { //From job list
            //Get tracker.
            Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
            //Build and send an Event.
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.analytics_cat_pref_client))
                    .setAction(getString(R.string.analytics_action_lavoro))
                    .setLabel(selectedJob.toLowerCase())
                    .build());
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.analytics_cat_pref_client))
                    .setAction(getString(R.string.analytics_action_categoria))
                    .setLabel(this.selectedCategory.toLowerCase())
                    .build());
        }
        else{
            //Get tracker.
            Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
            //Build and send an Event.
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.analytics_cat_usability_client))
                    .setAction(getString(R.string.analytics_action_click))
                    .setLabel("tappa")
                    .build());
        }

        startActivity(intent);
        overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
    }

    private void startSupplierSignup(ListView list, int position){
        String selectedJob = list.getItemAtPosition(position).toString();
        Intent intent = new Intent(getApplicationContext(), ZoneChooserActivity.class);
        intent.putExtra("selectedJob", selectedJob);
        intent.putExtra("switchFromClient", getIntent().getBooleanExtra("switchFromClient",false));
        intent.putExtra("category", this.selectedCategory);
        startActivity(intent);
        overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
    }

    private void setSelectionLabel(String job){
        this.presCat.setText("Scegli un mestiere nella categoria " + job);
    }

    private void init(){
        this.jobsFromTagsLayout = (RelativeLayout) findViewById(R.id.jobs_from_tags_layout);
        this.jobTagNames = new ArrayList<>();
        this.catItemList = new ArrayList<>();
        this.jobsArray = new ArrayList<>();
        this.jobsList = new ArrayList<>();
        this.jobCat = new ArrayList<>();
        this.presCat = (TextView) findViewById(R.id.txt_cat_pres);
        this.tagsListView = (ListView) findViewById(R.id.tags_list_view);
        this.jobsListView = (ListView) findViewById(R.id.jobs_list);
        this.jobsFromTagsListView = (ListView) findViewById(R.id.jobs_from_tags_listview);
        this.jobTagSuggest = (EditText) findViewById(R.id.job_tag_suggest);
        this.jobChooserBodyLayout = (RelativeLayout) findViewById(R.id.job_body_layout);
        this.catLinearLayout = (LinearLayout) findViewById(R.id.cat_linear_layout);
        this.spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
        if(this.jobTagSuggest != null)
            this.jobTagSuggest.setText("");
    }

    private class initAsync extends AsyncTask<Void, Void, Integer>{

        @Override
        protected Integer doInBackground(Void... voids) {
            init();

            //Get Jobs and Categories Lists
            getJobs();
            Log.i("LoadingAsync","getJobsIsOver");
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.i("LoadingAsync", "Into onPostExecute");
            initCategoryList();

            setListViewListener();
            setSuggesterListener();
            spinnerLayout.setVisibility(View.GONE);

            if(checkIfFirstTime("search"))
                showTooltipLayout(1);
            if(checkIfFirstTime("category"))
                showTooltipLayout(2);
            if(checkIfFirstTime("job"))
                showTooltipLayout(3);

            jobTagSuggest.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(tooltip1)
                        hideTooltipLayout("search", 1);
                    return false;
                }
            });
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }

}
