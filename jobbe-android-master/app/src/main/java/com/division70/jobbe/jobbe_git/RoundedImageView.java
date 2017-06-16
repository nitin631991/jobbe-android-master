package com.division70.jobbe.jobbe_git;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by giorgio on 17/11/15.
 */
public class RoundedImageView extends ImageView {

    public RoundedImageView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable drawable = getDrawable();

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        Bitmap b =  ((BitmapDrawable)drawable).getBitmap() ;
        if(b != null){
            Bitmap bitmap = b.copy(Bitmap.Config.ARGB_4444, true);
            //Bitmap bitmap = b.copy(Bitmap.Config.RGB_565, true);

            int w = getWidth(), h = getHeight();

            //bitmap = getResizedBitmap(bitmap, 400);

            Bitmap roundBitmap =  getCroppedBitmap(bitmap, w);
            canvas.drawBitmap(roundBitmap, 0, 0, null);
            roundBitmap.recycle();
            bitmap.recycle();
            //b.recycle();
        }

    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        Log.i("resize_startW", width + "");
        Log.i("resize_startH", height + "");

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        Log.i("resize_endW", width + "");
        Log.i("resize_endH", height + "");

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap sbmp;

        //Crop image to fit a square iconView and avoid a stretching effect
        if(bmp.getHeight() == bmp.getWidth()) {} //Do nothing in this case
        else if(bmp.getHeight() > bmp.getWidth()) {
            int y1 = (bmp.getHeight() - bmp.getWidth()) / 2;

            bmp = Bitmap.createBitmap(bmp, 0, y1, bmp.getWidth(), bmp.getWidth());
        }
        else if(bmp.getHeight() < bmp.getWidth()) {
            int x1 = (bmp.getWidth() - bmp.getHeight()) / 2;

            bmp = Bitmap.createBitmap(bmp, x1, 0, bmp.getHeight(), bmp.getHeight());
        }

        if(bmp.getWidth() != radius || bmp.getHeight() != radius)
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        else
            sbmp = bmp;

        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(),
                sbmp.getHeight(), Bitmap.Config.ARGB_4444);
        //Bitmap output = Bitmap.createBitmap(sbmp.getWidth(),
        //        sbmp.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(output);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        //canvas.drawRGB(0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f, sbmp.getHeight() / 2 + 0.7f,
                sbmp.getWidth() / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);
        sbmp.recycle();
        return output;
    }

}
