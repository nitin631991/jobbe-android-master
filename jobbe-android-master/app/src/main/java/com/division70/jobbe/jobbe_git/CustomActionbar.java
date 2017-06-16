package com.division70.jobbe.jobbe_git;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by giorgio on 23/11/15.
 */
public class CustomActionbar extends RelativeLayout {

    private ImageView back;
    private TextView title;
    private TextView subtitle;
    private LayoutInflater mInflater;

    public CustomActionbar(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        init();
    }
    public CustomActionbar(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        init();
    }
    public CustomActionbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init();
    }

    public void init() {
        mInflater.inflate(R.layout.customactionbar_layout, this, true);

        this.title = (TextView) findViewById(R.id.title);
        this.subtitle = (TextView) findViewById(R.id.subtitle);
        this.back = (ImageView) findViewById(R.id.back);
    }

    public void setTitle(String title){
        this.title.setText(title);
    }

    public void setSubtitle(String subtitle){
        this.title.setText(subtitle);
    }

    public void disableSubtitle(){
        this.subtitle.setVisibility(GONE);
    }

}
