package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by giorgio on 22/10/15.
 */
public class CreateCustomerActivity extends Activity {
    private Uri outputFileUri;
    private RoundedImageView imageView;

    private Button buttonUomo;
    private Button buttonDonna;

    private String buttonEnabledColor = "#bedadb";
    private String buttonDisabledColor = "#e5e8e9";

    private String gender = "male"; //Default
    private String startingMail;
    private String  ImagePath = "";
    private boolean modifiedImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createcustomer_layout);

        sendScreenAnalytics();

        EditText nameTextEdit = (EditText) findViewById(R.id.editText);
        nameTextEdit.requestFocus();

        Button avanti = (Button) findViewById(R.id.avanti);
        avanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText nameTextEdit = (EditText) findViewById(R.id.editText);
                String name = nameTextEdit.getText().toString();

                EditText emailTextEdit = (EditText) findViewById(R.id.editText2);
                String email = emailTextEdit.getText().toString();

                EditText annoTextEdit = (EditText) findViewById(R.id.editText3);
                String anno = annoTextEdit.getText().toString();

                if(checkCode(name,email,anno))
                    startPhoneNumber(name, email, anno, gender);
            }
        });

        buttonUomo = (Button) findViewById(R.id.button_uomo);
        buttonDonna = (Button) findViewById(R.id.button_donna);

        this.buttonUomo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableUomo();
            }
        });
        this.buttonDonna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableDonna();
            }
        });

        TextView choosePictureTextVeiw = (TextView) findViewById(R.id.add_modify_pic);
        choosePictureTextVeiw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Salvataggio su file, forse non necessario
                /*final File root = new File(Environment.getExternalStorageDirectory() + "/Jobbe/");
                root.mkdirs();
                final String fname = "userpic.jpg";
                final File sdImageMainDirectory = new File(root, fname);
                outputFileUri = Uri.fromFile(sdImageMainDirectory);

                //From camera
                final List<Intent> cameraIntents = new ArrayList<Intent>();
                final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                final PackageManager packageManager = getPackageManager();
                final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
                for(ResolveInfo res : listCam) {
                    final String packageName = res.activityInfo.packageName;
                    final Intent intent = new Intent(captureIntent);
                    intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    intent.setPackage(packageName);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    cameraIntents.add(intent);
                }

                //From gallery
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                // Chooser of filesystem options.
                final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

                // Add the camera options.
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

                startActivityForResult(chooserIntent, 1234);*/

                Image_Picker_Dialog();

            }

            ;
        });

        imageView = (RoundedImageView) findViewById(R.id.imageView2);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Salvataggio su file, forse non necessario
               /* final File root = new File(Environment.getExternalStorageDirectory() + "/Jobbe/");
                root.mkdirs();
                final String fname = "userpic.jpg";
                final File sdImageMainDirectory = new File(root, fname);
                outputFileUri = Uri.fromFile(sdImageMainDirectory);

                //From camera
                final List<Intent> cameraIntents = new ArrayList<Intent>();
                final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                final PackageManager packageManager = getPackageManager();
                final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
                for(ResolveInfo res : listCam) {
                    final String packageName = res.activityInfo.packageName;
                    final Intent intent = new Intent(captureIntent);
                    intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    intent.setPackage(packageName);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    cameraIntents.add(intent);
                }

                //From gallery
                //final Intent galleryIntent = new Intent();
                //galleryIntent.setType("image*//*");
                //galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                // Chooser of filesystem options.
                final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

                // Add the camera options.
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

                startActivityForResult(chooserIntent, 1234);*/

                Image_Picker_Dialog();
            }

            ;
        });

        startingMail = "";

        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Inserisci i tuoi dati");
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

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("SIGNUP - CLIENTE BIO");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void enableUomo(){
        this.buttonUomo.setBackgroundColor(Color.parseColor(this.buttonEnabledColor));
        this.buttonDonna.setBackgroundColor(Color.parseColor(this.buttonDisabledColor));
        this.gender = "male";
    }

    private void enableDonna(){
        this.buttonDonna.setBackgroundColor(Color.parseColor(this.buttonEnabledColor));
        this.buttonUomo.setBackgroundColor(Color.parseColor(this.buttonDisabledColor));
        this.gender = "female";
    }

    private void startPhoneNumber(String name, String email, String anno, String gender){
        Intent intent = new Intent(CreateCustomerActivity.this, PhoneNumberActivity.class);
        intent.putExtra("displayName", name);
        intent.putExtra("email", email);
        intent.putExtra("anno", anno);
        intent.putExtra("gender", gender);
        intent.putExtra("type", "client");
        startActivity(intent);
        overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
    }

    private boolean checkCode(String name, String email, String anno){
        if (name.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Il campo nome non può essere vuoto", Toast.LENGTH_LONG).show();
            return false;
        }
        else if (!name.contains(" ")) {
            Toast.makeText(getApplicationContext(), "Il nome deve contenere nome e cognome", Toast.LENGTH_LONG).show();
            return false;
        }
        else if (email.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Il campo email non può essere vuoto", Toast.LENGTH_LONG).show();
            return false;
        }
        else if(!email.matches("^(.+)@(.+)\\.(.+)$")){
            Toast.makeText(getApplicationContext(), "L\'indirizzo email inserito non è valido", Toast.LENGTH_LONG).show();
            return false;
        }
        else if(!anno.isEmpty())
            if((!anno.matches("19[\\d][\\d]")) && (!anno.matches("20[\\d][\\d]"))) {
                Toast.makeText(getApplicationContext(), "L'anno di nascita non è corretto", Toast.LENGTH_LONG).show();
                return false;
            }
        else if(!startingMail.equals(email))
            if(emailExists(email)){
                Toast.makeText(getApplicationContext(), "L'email inserita è già registrata", Toast.LENGTH_LONG).show();
                return false;
            }

        return true; //No problems here
    }

    private boolean emailExists(String email){
        Log.i("CHECK EMAIL", email);
        ParseUser x;
        try {
            //x = ParseUser.getQuery().whereEqualTo("email", email).getFirst();
            x = ParseUser.getQuery().whereEqualTo("userEmail", email).getFirst();
            if (x == null)
                return false;
            Log.i("MAIL ESISTE GIÀ", email + " " + x.getString("displayName"));
        } catch (ParseException e) {
            Log.i("EXCEPTION!!!", e.getMessage());
            if (e.getCode() == ParseException.OBJECT_NOT_FOUND)  //NON ESISTE NESSUN UTENTE, MA LA QUERY È ANDATA A BUON FINE
                return false;
            e.printStackTrace();
        }
        return true;
    }

  /*  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        byte[] image;
        if (resultCode == RESULT_OK) {
            if (requestCode == 1234) {
                System.out.println("onActivityResult Create Customer Activity ");
                final boolean isCamera;
                if (data == null || data.getData() == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }
                //Log.i("imageUri",selectedImageUri.toString());
                if (selectedImageUri != null){
                    try {
                        int rotation = getImageRotation(selectedImageUri);
                        Log.i("rotation", rotation + "");
                        image = getBytesFromUri(selectedImageUri);
                        final File root = new File(Environment.getExternalStorageDirectory() + "/Jobbe/");
                        root.mkdirs();
                        final String fname = "userpic.jpg";
                        final File sdImageMainDirectory = new File(root, fname);
                        //TODO Resize immagine
                        *//*
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(sdImageMainDirectory));
                        bos.write(image);
                        bos.flush();
                        bos.close();

                        imageView.setImageDrawable(null);
                        Uri uri = Uri.fromFile(sdImageMainDirectory);
                        imageView.setImageURI(uri);
                        *//*
                        setPic(sdImageMainDirectory.toString(), image, rotation);

                    }
                    catch (IOException e){
                        Toast.makeText(getApplicationContext(), R.string.image_loading_error, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }
*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE ) {
            if(resultCode == Activity.RESULT_OK){
                // if(data != null){
                //      Bitmap bm = (Bitmap) data.getExtras().get("data");
                modifiedImage = true;
                File file1 = new File(Environment.getExternalStorageDirectory()+ "/Jobbe/userpic.jpg");
                System.out.println("Now Path   " + file1.getAbsolutePath());
                picturePath = file1.getAbsolutePath();
                // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
//	            Uri tempUri = getImageUri(getActivity().getApplicationContext(), bm);
                //     Bitmap bitmap11 = decodeSampledBitmapFromFile(file1.getAbsolutePath(), 500, 300);
                Bitmap bitmap = BitmapFactory.decodeFile(file1.getPath());
                ImagePath = picturePath;
                ExifInterface ei = null;
                try {
                    ei = new ExifInterface(file1.getPath());
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                    switch(orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            bitmap=rotateBitmap(bitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            bitmap=rotateBitmap(bitmap, 180);
                            break;
                        // etc.
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


                bitmap = Bitmap.createScaledBitmap(bitmap,200,200,true);
                imageView.setImageBitmap(bitmap);


                //   imagesPathList.add(persistImage(bitmap));

                // mCircularNetworkImageView.setImageBitmap(getRoundedCroppedBitmap(bitmap, 180));
                //            System.out.println("Profile Camera Image PAth:  " +getRealPathFromURI(tempUri));
                // CALL THIS METHOD TO GET THE ACTUAL PATH
                //   File finalFile = new File(getRealPathFromURI(tempUri));
                //  }
            } else if(resultCode == Activity.RESULT_CANCELED){
                ImagePath = "";
                modifiedImage = false;
            }

        }

        else if (requestCode == GALLERY ) {
            if(resultCode == Activity.RESULT_OK){
                modifiedImage = true;
                Uri selectedImageUri = data.getData();

    /*            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    picturePath = getRealPathFromURI_BelowAPI11(EditClientUserDataActivity.this,selectedImageUri );
                }else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB || Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    picturePath = getRealPathFromURI_API11to18(EditClientUserDataActivity.this,selectedImageUri );
                }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
                    picturePath = getRealPathFromURI_API19(EditClientUserDataActivity.this,selectedImageUri );
                }*/


                picturePath = getPath(selectedImageUri);
               // System.out.println("Profile Gallery Image Path : " + picturePath);
                ImagePath = picturePath;

                Uri uri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    // Log.d(TAG, String.valueOf(bitmap));

                    imageView.setImageBitmap(bitmap);
                    copyDirectoryOneLocationToAnotherLocation(new File(picturePath), new File(Environment.getExternalStorageDirectory()+ "/Jobbe/userpic.jpg"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //  imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));


                //  imagesPathList.add(persistImage(BitmapFactory.decodeFile(picturePath)));
                // mCircularNetworkImageView.setImageBitmap(getRoundedCroppedBitmap(BitmapFactory.decodeFile(picturePath), 180));
            } else if(resultCode == Activity.RESULT_CANCELED){
                ImagePath = "";
                modifiedImage = false;
            }
        }
    }


    public static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
           // System.out.println(" Source Location is directory");
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
           // System.out.println(" Source Location is NOT directory");
            InputStream in = new FileInputStream(sourceLocation);

            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

    }





    public byte[] getBytesFromUri(Uri uri) throws IOException {

        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    private int getImageRotation(Uri uri){
        int rotate = 0;
        try {

            File imageFile = new File(uri.getPath());
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Log.i("imageOrientation", orientation + "");

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }


    private void setPic(String outputPath, byte[] image, int rotation) {
        // Get the dimensions of the View
        //int targetW = photoView.getWidth();
        //int targetH = photoView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(image, 0, image.length, bmOptions);
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
            fos = new FileOutputStream(outputPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length, bmOptions);
        bitmap = rotateBitmap(bitmap, (float) rotation);
        if(bitmap == null){
            System.out.println("bitmap is null");
        }else{
            System.out.println("bitmap is not null");
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,fos);
        imageView.setImageBitmap(bitmap);
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Log.i("Orientation", "merda staladita");
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        if(outputFileUri != null)
            savedInstanceState.putString("outputFileUri", outputFileUri.toString());

        super.onSaveInstanceState(savedInstanceState);
    }
    //onRestoreInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        if(outputFileUri != null) {
            String uriString = savedInstanceState.getString("outputFileUri");
            File file = new File(uriString);
            outputFileUri = Uri.fromFile(file);
        }
    }
// change on camara or gellery

    public int GALLERY = 13;
    public int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 12;
    public String picturePath = "";

    //open image picker dialog to capture image with camera or select from gallery
    public void Image_Picker_Dialog()
    {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(CreateCustomerActivity.this);
        myAlertDialog.setTitle("Pictures Option");
        myAlertDialog.setMessage("Select Picture Mode");

        myAlertDialog.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select File"), GALLERY);
            }
        });
        myAlertDialog.setNegativeButton("Camera", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface arg0, int arg1)
            {
	      /* Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	           intent.putExtra(MediaStore.EXTRA_OUTPUT,
	                          MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
	           startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);*/


                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

                File file = new File(Environment.getExternalStorageDirectory()+ "/Jobbe/userpic.jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));


                try {

                    intent.putExtra("return-data", true);

                    startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    // Do nothing for now
                }

            }
        });
        myAlertDialog.show();

    }


    // get path of selected image
    public String getPath(Uri uri)
    {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

}