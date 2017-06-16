package com.division70.jobbe.jobbe_git;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by giorgio on 18/11/15.
 */
public class JobCategoryRelative extends RelativeLayout {

    private ImageView catPic;
    private ImageView picOverlay;
    private TextView catName;

    LayoutInflater mInflater;
    public JobCategoryRelative(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        init();

    }
    public JobCategoryRelative(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        init();
    }
    public JobCategoryRelative(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init();
    }

    private void init(){
        mInflater.inflate(R.layout.job_category_relative, this, true);
        this.catName = (TextView) findViewById(R.id.category_name);
        this.picOverlay = (ImageView) findViewById(R.id.category_overlay);
        this.catPic = (ImageView) findViewById(R.id.category_pic);
    }

    public void setCategory(String name, String icon){
        this.catName.setText(name);
        int resourceId = getResources().getIdentifier(icon, "drawable", "com.division70.jobbe.jobbe_git");
        this.catPic.setImageResource(resourceId);
        //this.picOverlay.setVisibility(VISIBLE);
    }

    public void select(){
        this.catName.setTypeface(null, Typeface.BOLD);
        this.picOverlay.setVisibility(VISIBLE);
    }

    public void deselect(){
        this.catName.setTypeface(null, Typeface.NORMAL);
        this.picOverlay.setVisibility(GONE);
    }

    public String getCategoryName(){
        return this.catName.getText().toString();
    }
}
