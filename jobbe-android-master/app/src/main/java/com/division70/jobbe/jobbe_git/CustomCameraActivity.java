package com.division70.jobbe.jobbe_git;

/**
 * Created by giorgio on 05/12/15.
 */
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class CustomCameraActivity extends Activity {
    private static final String TAG = "CamTestActivity";
    Preview preview;
    Button buttonClick;
    Camera camera;
    Activity act;
    Context ctx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        act = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.customcamera_layout);

        preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
        preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        ((RelativeLayout) findViewById(R.id.layout)).addView(preview);
        preview.setKeepScreenOn(true);

        ImageView takePictureButton = (ImageView) findViewById(R.id.imageView);
        takePictureButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                camera.takePicture(shutterCallback, rawCallback, jpegCallback);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                camera = Camera.open(0);

                switch(getResources().getConfiguration().orientation){
                    case 1:
                        camera.setDisplayOrientation(90);
                        break;
                    default:
                        break;
                }

                camera.startPreview();
                preview.setCamera(camera);
            } catch (RuntimeException ex){

            }
        }

    }

    @Override
    protected void onPause() {
        if(camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    private void resetCam() {
        camera.startPreview();
        preview.setCamera(camera);
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            //			 Log.d(TAG, "onShutter'd");
        }
    };

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };

    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                new SaveImageTask().execute(data).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent();
            setResult(7777, intent);
            finish();
        }
    };

    public static byte[] rotate(byte[] data) {
        Matrix matrix = new Matrix();
        //Device.getOrientation() is used in order to support the emulator and an actual device
        matrix.postRotate(90);
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (bitmap.getWidth() < bitmap.getHeight()) {
            //no rotation needed
            return data;
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true
        );
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, blob);
        byte[] bm = blob.toByteArray();
        return bm;
    }

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {

            // Write to SD Card
            try {

                //Rotate image if Samsung
                if(true && getResources().getConfiguration().orientation == 1){
                    data[0] = rotate(data[0]);
                }

                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/Jobbe");
                dir.mkdirs();

                String fileName = "userpic.jpg";
                File outFile = new File(dir, fileName);

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data[0], 0, data[0].length, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                int targetW = 400;
                int targetH = 400;

                // Determine how much to scale down the image
                int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

                Log.i("Sizes", "W: " + photoW + " " + targetW + " - H: " + photoH + " " + targetH + "");
                Log.i("scaleFactor", scaleFactor + "");

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(outFile.getPath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Bitmap bitmap = BitmapFactory.decodeByteArray(data[0], 0, data[0].length, bmOptions);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.flush();
                fos.close();
                bitmap.recycle();

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

                refreshGallery(outFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }

    }
}
