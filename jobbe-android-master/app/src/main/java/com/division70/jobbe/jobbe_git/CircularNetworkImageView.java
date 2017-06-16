package com.division70.jobbe.jobbe_git;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;


public class CircularNetworkImageView extends ImageView {
	Context mContext;
	 private int borderWidth = 4;
	    private int viewWidth;
	    private int viewHeight;
	    private Bitmap image;
	    private Paint paint;
	    private Paint paintBorder;
	    private BitmapShader shader;
	    
	public CircularNetworkImageView(Context context) {
		super(context);
		mContext = context;
		setup();
	}
 
	public CircularNetworkImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mContext = context;
		setup();
	}
 
	public CircularNetworkImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setup();
	}
 
//	@Override
//	public void setImageBitmap(Bitmap bm) {
//		if(bm==null) return;
//		setImageDrawable(new BitmapDrawable(mContext.getResources(),
//				//getCircularBitmap(bm)));
//	}
 
	/**
	 * Creates a circular bitmap and uses whichever dimension is smaller to determine the width
	 * <br/>Also constrains the circle to the leftmost part of the image
	 * 
	 *
	 */
//	public Bitmap getCircularBitmap(Bitmap bitmap) {
//		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
//				bitmap.getHeight(), Config.ARGB_8888);
//		Canvas canvas = new Canvas(output);
//		int width = bitmap.getWidth();
//		if(bitmap.getWidth()>bitmap.getHeight())
//			width = bitmap.getHeight();
//		final int color = 0xffff0000;
//		final Paint paint = new Paint();
//		final Rect rect = new Rect(0, 0, width, width);
//		final RectF rectF = new RectF(rect);
//		final float roundPx = width / 2;
// 
//		paint.setAntiAlias(true);
//		canvas.drawARGB(0, 0, 0, 0);
//		paint.setColor(color);
//		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
// 
//		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
//		canvas.drawBitmap(bitmap, rect, rect, paint);
// 
//		return output;
//	}
	
	private void setup()
    {
        // init paint
        paint = new Paint();
        paint.setAntiAlias(true);

        paintBorder = new Paint();
        setBorderColor(Color.parseColor("#ac7aaa"));
        paintBorder.setAntiAlias(true);
        this.setLayerType(LAYER_TYPE_SOFTWARE, paintBorder);
        paintBorder.setShadowLayer(4.0f, 0.0f, 2.0f, Color.parseColor("#ac7aaa"));
    }

    public void setBorderWidth(int borderWidth)
    {
        this.borderWidth = borderWidth;
        this.invalidate();
    }

    public void setBorderColor(int borderColor)
    {
        if (paintBorder != null)
            paintBorder.setColor(borderColor);

        this.invalidate();
    }

    private void loadBitmap()
    {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) this.getDrawable();

        if (bitmapDrawable != null)
            image = bitmapDrawable.getBitmap();
    }

    
    @Override
    public void onDraw(Canvas canvas)
    {
        // load the bitmap
        loadBitmap();

        // init shader
        if (image != null)
        {
            shader = new BitmapShader(Bitmap.createScaledBitmap(image, canvas.getWidth(), canvas.getHeight(), false), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            int circleCenter = viewWidth / 2;

            // circleCenter is the x or y of the view's center
            // radius is the radius in pixels of the cirle to be drawn
            // paint contains the shader that will texture the shape
            canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter + borderWidth - 4.0f, paintBorder);
            canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec, widthMeasureSpec);

        viewWidth = width - (borderWidth * 2);
        viewHeight = height - (borderWidth);

        setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec)
    {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY)
        {
            // We were told how big to be
            result = specSize;
        }
        else
        {
            // Measure the text
            result = viewWidth;
        }

        return result;
    }

    private int measureHeight(int measureSpecHeight, int measureSpecWidth)
    {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY)
        {
            // We were told how big to be
            result = specSize;
        }
        else
        {
            // Measure the text (beware: ascent is a negative number)
            result = viewHeight;
        }

        return (result + 2);
    }
 
}
