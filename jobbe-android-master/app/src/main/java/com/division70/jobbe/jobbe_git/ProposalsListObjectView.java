package com.division70.jobbe.jobbe_git;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

/**
 * Created by osvy on 10/11/15.
 */
public class ProposalsListObjectView extends RelativeLayout {
    private ImageView suppPhotoView;
    private TextView suppNameView;
    private TextView numRatingView;
    private RatingBar ratingBar;
    private TextView suppTypeView;
    private TextView priceView;
    private TextView pricetypeView;
    private RelativeLayout disableOverlay;

    LayoutInflater mInflater;
    ParseObject proposal;
    ParseObject supplier;
    ParseObject request;
    Context context;

    public ProposalsListObjectView(Context context) {
        super(context);
        this.context=context;
        mInflater = LayoutInflater.from(context);
        init();

    }

    public ProposalsListObjectView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        init();
    }

    public ProposalsListObjectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init();
    }

    public void init()
    {
        mInflater.inflate(R.layout.proposalslistobjectview_layout, this, true);

        getViews();
    }

    public void setRatingBarPublic(){
        setRatingBar(this.ratingBar);
    }

    private void setRatingBar(RatingBar ratingBar){
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();

        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.jobbe_ratings),
                PorterDuff.Mode.SRC_ATOP); // for filled stars

        /*
        stars.getDrawable(1).setColorFilter(Color.TRANSPARENT,
                PorterDuff.Mode.SRC_ATOP); // for half filled stars
        */

        stars.getDrawable(1).setColorFilter(Color.BLACK,
                PorterDuff.Mode.SRC_ATOP); // for half filled stars


        //stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.jobbe_transparent),
        //        PorterDuff.Mode.SRC_ATOP);

        stars.getDrawable(0).setColorFilter(0xfff3f3f3,
                PorterDuff.Mode.SRC_ATOP); // for half filled stars

        //stars.getDrawable(0).setAlpha(0);

    }

    private void getViews(){
        suppPhotoView = (ImageView) this.findViewById(R.id.suppphoto_iv);
        suppNameView = (TextView) this.findViewById(R.id.nome_tv);
        numRatingView = (TextView) this.findViewById(R.id.numrating_tv);
        ratingBar = (RatingBar) this.findViewById(R.id.rating_rb);
        suppTypeView = (TextView) this.findViewById(R.id.supptype_tv);
        priceView = (TextView) this.findViewById(R.id.price_tv);
        pricetypeView = (TextView) this.findViewById(R.id.pricetype_tv);
        disableOverlay = (RelativeLayout) this.findViewById(R.id.disable_overlay);
        disableOverlay.setVisibility(GONE);

        setRatingBar(ratingBar);
    }

    public void setProposal(ParseObject proposal){
        this.proposal = proposal;
        this.supplier = (ParseObject) proposal.get("supplierId");
        this.request = (ParseObject) proposal.get("requestId");
        try {
            supplier.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        setPhotoView();
        setNameView();
        setRatingViews();
        setSuppTypeView();
        setPrice();

    }

    public void setPhotoView(){
        ParseFile photo = (ParseFile) supplier.get("profilePhoto");
        if (photo!=null) {
            Picasso.with(context).load(photo.getUrl()).into(suppPhotoView);
//            photo.getDataInBackground(new GetDataCallback() {
//                @Override
//                public void done(byte[] data, ParseException e) {
//                    if (e == null) {
//                        suppPhotoView.setImageDrawable(null);
//                        Log.i("data length", data.length + "");
//                        int scaleStep = getResources().getInteger(R.integer.photo_scale_step);
//                        int scaleFactor = data.length / scaleStep;
//                        Log.i("scaleFactor", scaleFactor + "");
//                        BitmapFactory.Options options=new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
//                        options.inPurgeable = true; // inPurgeable is used to free up memory while required
//                        if(scaleFactor > 1)
//                            options.inSampleSize = scaleFactor;
//                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
//                        suppPhotoView.setImageBitmap(bmp);
//                    }
//                }
//            });
        }
    }


    public void setNameView(){
        suppNameView.setText(supplier.getString("displayName"));
    }

    public void setRatingViews(){
        int numRating = supplier.getInt("numberOfReviews");
        if (numRating == 0){
            ratingBar.setVisibility(GONE);
            numRatingView.setText("nuovo fornitore");
        }
        else {
            numRatingView.setText(numRating + "");
            ratingBar.setRating((float) supplier.getDouble("averageRating"));
        }

        //setRatingBar(ratingBar);
    }

    public void setSuppTypeView(){
        String type = proposal.getString("type");
        switch (type){
            case "professionist":
                suppTypeView.setText("Professionista");
                suppTypeView.setBackgroundColor(getResources().getColor(R.color.jobbe_violet));
                suppTypeView.setTextColor(0xFFFFFFFF);
                suppTypeView.setTypeface(null, Typeface.BOLD);
                break;
            case "hobby":
                suppTypeView.setText("Non Professionista");
                //suppTypeView.setBackgroundColor(0x00000000);
                //suppTypeView.setBackgroundResource(R.color.jobbe_transparent);
                suppTypeView.setTextColor(0xFF777777);
                suppTypeView.setTypeface(null, Typeface.NORMAL);
                suppTypeView.setPadding(0,0,0,0);
                break;
        }
    }

    public void setPrice(){
        Double price = proposal.getDouble("price");
        String priceType = proposal.getString("priceType");
        Log.i("priceType", priceType);
        priceView.setText(price + "€");
        if(priceType.equals("ore"))
            pricetypeView.setText("a misura");
        else
            pricetypeView.setText("a corpo");

    }

    public void disable(){
        disable(0);
    }

    public void disable(int state){
        disableOverlay.setVisibility(VISIBLE);
        if (state == 0)
            suppTypeView.setText("Questa proposta è stata ritirata");
        else
            suppTypeView.setText("Questa proposta è stata scartata");
        suppTypeView.setTextColor(0xFF000000);
        suppTypeView.setBackgroundResource(R.color.jobbe_transparent);
        suppTypeView.setTypeface(null, Typeface.NORMAL);
        suppTypeView.setTextSize(9);
        suppTypeView.setPadding(0, 0, 0, 0);
    }

}
