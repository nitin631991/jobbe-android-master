package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by osvy on 02/11/15.
 */
public class EditSupplierUserDataActivity extends Activity {
    private String name;
    private String email;
    private String gender;
    private String year;
    private ParseFile photo;
    private String cf;
    private String ditta;
    private String desc;


    private EditText nameET;
    private EditText yearET;
    private EditText mailET;
    private EditText cfET;
    private EditText dittaET;
    private ActionEditText descET;
    private boolean modifiedImage = false;
    private RoundedImageView photoView;
    private RelativeLayout spinnerLayout;

    private Uri outputFileUri;
    private ParseUser user;
    private String startingMail;
    private int yearInt;
    private LinearLayout photoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.editsupplier_layout);

        sendScreenAnalytics("FORNITORE - PROFILO - AGGIORNA PROFILO");

        user = ParseUser.getCurrentUser();
        name = (String) user.get("displayName");
        email = (String) user.get("userEmail");
        year = Integer.toString(user.getInt("anno"));
        photo = (ParseFile) user.get("profilePhoto");
        cf = (String) user.get("codFiscale");
        ditta = (String) user.get("ditta");
        desc = (String) user.get("desc");

        startingMail = (email == null) ? "" : email;

        nameET = (EditText) findViewById(R.id.supp_name);
        mailET = (EditText) findViewById(R.id.supp_email);
        yearET = (EditText) findViewById(R.id.supp_anno);
        cfET = (EditText) findViewById(R.id.supp_cod_fis);
        dittaET = (EditText) findViewById(R.id.supp_ditta);
        descET = (ActionEditText) findViewById(R.id.supp_desc);
        spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);

        descET.setHorizontallyScrolling(false);
        descET.setLines(1);
        descET.setMaxLines(10);

        nameET.setText(name);
        mailET.setText(email);
        if (year != null && (!(year.equals("0"))))
            yearET.setText(year);
        cfET.setText(cf);
        dittaET.setText(ditta);
        descET.setText(desc);

        photoView = (RoundedImageView) findViewById(R.id.supp_pic_button);
        photoLayout = (LinearLayout) findViewById(R.id.photo_layout);
        photoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Salvataggio su file, forse non necessario
                final File root = new File(Environment.getExternalStorageDirectory() + "/Jobbe/");
                root.mkdirs();
                final String fname = "userpic.jpg";
                final File sdImageMainDirectory = new File(root, fname);
                outputFileUri = Uri.fromFile(sdImageMainDirectory);
                Log.i("OutputFileURI", outputFileUri + "");
                //From camera
                final List<Intent> cameraIntents = new ArrayList<Intent>();
                final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                final PackageManager packageManager = getPackageManager();
                final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
                for (ResolveInfo res : listCam) {
                    final String packageName = res.activityInfo.packageName;
                    Log.i("packageName: ", res.activityInfo.packageName);
                    final Intent intent = new Intent(captureIntent);
                    intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    intent.setPackage(packageName);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    cameraIntents.add(intent);
                }

                //Intent testIntent = new Intent(EditSupplierUserDataActivity.this, CustomCameraActivity.class);
                //cameraIntents.add(testIntent);

                //From gallery
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                // Chooser of filesystem options.
                final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");


                // Add the camera options.
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));


                startActivityForResult(chooserIntent, 1234);

            }

            ;
        });

        if (photo != null) {

            TextView txtView = (TextView) findViewById(R.id.add_modify_pic);
            txtView.setText("Modifica foto profilo");
            Picasso.with(this).load(photo.getUrl()).into(photoView);
//            photo.getDataInBackground(new GetDataCallback() {
//                @Override
//                public void done(byte[] data, ParseException e) {
//                    if (e == null) {
//                        photoView.setImageDrawable(null);
//                        Log.i("data length", data.length + "");
//                        int scaleStep = getResources().getInteger(R.integer.photo_scale_step);
//                        int scaleFactor = data.length / scaleStep;
//                        Log.i("scaleFactor", scaleFactor + "");
//                        BitmapFactory.Options options = new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
//                        options.inPurgeable = true; // inPurgeable is used to free up memory while required
//                        if (scaleFactor > 1)
//                            options.inSampleSize = scaleFactor;
//                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
//                        photoView.setImageBitmap(bmp);
//                    }
//                }
//            });
        }

        Button avanti = (Button) findViewById(R.id.supp_avanti);
        avanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = nameET.getText().toString();
                email = mailET.getText().toString();
                year = yearET.getText().toString();
                ditta = dittaET.getText().toString();
                cf = cfET.getText().toString();
                desc = descET.getText().toString();

                if (checkCode(name, email, cf, desc, year)) {
                    yearInt = Integer.parseInt(year);
                    spinnerLayout.setVisibility(View.VISIBLE);

                    new saveAsync().execute();
                    /*
                    user.put("displayName", name);
                    user.put("userEmail", email);
                    user.put("anno", yearInt);
                    user.put("ditta",ditta);
                    user.put("codFiscale",cf);
                    user.put("desc",desc);
                    if (modifiedImage) {
                        ParseFile imageFile = new ParseFile(new File(Environment.getExternalStorageDirectory() + getResources().getString(R.string.pic_path)));
                        user.put("profilePhoto", imageFile);
                    }
                    try{
                        user.save();
                    }
                    catch (ParseException e){
                        Log.i("ParseError", e.toString());
                        Toast.makeText(getApplicationContext(),"C\'è stato un errore durante il salvataggio. Le modifiche non saranno mantenute", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("modified", true);
                    setResult(Activity.RESULT_OK, returnIntent);
                    spinnerLayout.setVisibility(View.GONE);
                    finish();
                    */
                }
            }
        });

        setUpActionBar();
    }

    private void setUpActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Modifica dati");
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

    private void sendScreenAnalytics(String screenName) {
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        byte[] image;

        if (resultCode == 7777) {
            modifiedImage = true;
            File root = new File(Environment.getExternalStorageDirectory() + "/Jobbe/");
            String fname = "userpic.jpg";
            String photoPath = new File(root, fname).getAbsolutePath();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
            photoView.setImageBitmap(bitmap);
        } else if (resultCode == RESULT_OK) {
            modifiedImage = true;
            if (requestCode == 1234) {
                final boolean isCamera;
                if (data == null || data.getData() == null) {
                    isCamera = true;

                    File root = new File(Environment.getExternalStorageDirectory() + "/Jobbe/");
                    root.mkdirs();
                    String fname = "userpic.jpg";
                    File sdImageMainDirectory = new File(root, fname);
                    outputFileUri = Uri.fromFile(sdImageMainDirectory);
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Log.i("DATA: ", "" + data);
                Log.i("URI: ", "" + outputFileUri);
                Log.i("IsCamera", isCamera + "");

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }
                //Log.i("imageUri", selectedImageUri.toString());
                if (selectedImageUri != null) {
                    try {
                        int rotation = getImageRotation(selectedImageUri);
                        Log.i("rotation", rotation + "");
                        image = getBytesFromUri(selectedImageUri);
                        final File root = new File(Environment.getExternalStorageDirectory() + "/Jobbe/");
                        root.mkdirs();
                        final String fname = "userpic.jpg";
                        final File sdImageMainDirectory = new File(root, fname);
                        //TODO Resize immagine

                        /*
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(sdImageMainDirectory));
                        bos.write(image);
                        bos.flush();
                        bos.close();
                        */
                        if (image.length > 0)
                            setPic(sdImageMainDirectory.toString(), image, rotation);

                        else
                            Toast.makeText(getApplicationContext(), "Qualcosa è andato storto, riprova", Toast.LENGTH_LONG).show();

                        /*
                        photoView.setImageDrawable(null);
                        Uri uri = Uri.fromFile(sdImageMainDirectory);
                        photoView.setImageURI(uri);
                        */
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), R.string.image_loading_error, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        if (outputFileUri != null)
            savedInstanceState.putString("outputFileUri", outputFileUri.toString());

        super.onSaveInstanceState(savedInstanceState);
    }

    //onRestoreInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        if (outputFileUri != null) {
            String uriString = savedInstanceState.getString("outputFileUri");
            File file = new File(uriString);
            outputFileUri = Uri.fromFile(file);
        }
    }

    @Override
    protected void onResume() {
        Log.i("Orientation BEFORE", getResources().getConfiguration().orientation + "");
        super.onResume();
        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
        Log.i("Orientation AFTER", getResources().getConfiguration().orientation + "");
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

    private int getImageRotation(Uri uri) {
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
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        photoView.setImageBitmap(bitmap);
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Log.i("Orientation", "merda staladita");
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private boolean checkCode(String name, String email, String CF, String desc, String anno) {

        if (name.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Il campo nome non può essere vuoto", Toast.LENGTH_LONG).show();
            return false;
        } else if (!name.contains(" ")) {
            Toast.makeText(getApplicationContext(), "Il nome deve contenere nome e cognome", Toast.LENGTH_LONG).show();
            return false;
        } else if (email.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Il campo email non può essere vuoto", Toast.LENGTH_LONG).show();
            return false;
        } else if (!email.matches("^(.+)@(.+)\\.(.+)$")) {
            Toast.makeText(getApplicationContext(), "L\'indirizzo email inserito non è valido", Toast.LENGTH_LONG).show();
            return false;
        } else if (desc.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Il campo descrizione non può essere vuoto", Toast.LENGTH_LONG).show();
            return false;
        } else if (CF.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Il campo codice fiscale non può essere vuoto", Toast.LENGTH_LONG).show();
            return false;
        } else if (!ValidCF(CF)) {
            Toast.makeText(getApplicationContext(), "Il codice fiscale non è corretto", Toast.LENGTH_LONG).show();
            return false;
        } else if (anno.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Il campo anno non può essere vuoto", Toast.LENGTH_LONG).show();
            return false;
        } else if ((!anno.matches("19[\\d][\\d]")) && (!anno.matches("20[\\d][\\d]"))) {
            Toast.makeText(getApplicationContext(), "L'anno di nascita non è corretto", Toast.LENGTH_LONG).show();
            return false;
        } else if (!startingMail.equals(email)) {
            if (emailExists(email)) {
                Toast.makeText(getApplicationContext(), "L'email inserita è già registrata", Toast.LENGTH_LONG).show();
                return false;
            }
        } else if (!new RegexValidator(getApplicationContext()).validate(desc))
            return false;

        return true; //No problems here
    }


    private boolean emailExists(String email) {
        Log.i("CHECK EMAIL", email);
        ParseUser x;
        try {
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

    static boolean ValidCF(String cf) {
        Log.i("In VALIDCF", "Into");
        int i, s, c;
        String cf2;
        int setdisp[] = {1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20,
                11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23};
        if (cf.length() == 0)
            return false;
        Log.i("VALIDCF", "CHECKPOINT 1");
        if (cf.length() != 16)
            return false;
        Log.i("VALIDCF", "CHECKPOINT 2");
        cf2 = cf.toUpperCase();
        for (i = 0; i < 16; i++) {
            c = cf2.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'Z'))
                return false;
        }
        Log.i("VALIDCF", "CHECKPOINT 3");
        s = 0;
        for (i = 1; i <= 13; i += 2) {
            c = cf2.charAt(i);
            if (c >= '0' && c <= '9')
                s = s + c - '0';
            else
                s = s + c - 'A';
        }
        for (i = 0; i <= 14; i += 2) {
            c = cf2.charAt(i);
            if (c >= '0' && c <= '9') c = c - '0' + 'A';
            s = s + setdisp[c - 'A'];
        }
        if (s % 26 + 'A' != cf2.charAt(15))
            return false;

        Log.i("VALIDCF", "Seems valid: " + cf);
        return true;
    }

    private class saveAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            user.put("displayName", name);
            user.put("userEmail", email);
            user.put("anno", yearInt);
            user.put("ditta", ditta);
            user.put("codFiscale", cf);
            user.put("desc", desc);
            if (modifiedImage) {
                ParseFile imageFile = new ParseFile(new File(Environment.getExternalStorageDirectory() + getResources().getString(R.string.pic_path)));
                user.put("profilePhoto", imageFile);
            }
            try {
                user.save();
            } catch (ParseException e) {
                Log.i("ParseError", e.toString());
                Toast.makeText(getApplicationContext(), "C\'è stato un errore durante il salvataggio. Le modifiche non saranno mantenute", Toast.LENGTH_LONG).show();
                return null;
            }
            Intent returnIntent = new Intent();
            returnIntent.putExtra("modified", true);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            spinnerLayout.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }
}
