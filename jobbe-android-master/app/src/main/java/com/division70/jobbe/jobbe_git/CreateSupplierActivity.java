package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by giorgio on 26/10/15.
 */
public class CreateSupplierActivity extends Activity {

    private String selectedJob;
    private TextView supplier_job;

    private Switch pIVA_switch;
    private EditText pIVA;
    private EditText suppName;
    private EditText suppEmail;
    private EditText suppDitta;
    private ActionEditText suppDesc;
    private EditText suppCF;
    private EditText suppAnno;
    private RelativeLayout spinnerLayout;

    private ParseFile photo;

    RoundedImageView suppPic;
    //private Button suppPic;
    private Button suppAvanti;

    private Uri suppPicUri;

    private boolean haschangedPhoto = false;
    private ParseUser user;

    private String nome;
    private String email;
    private String CF;
    private String desc;
    private String ditta;
    private String anno;
    private String partIva;
    private String startingMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createsupplier_layout);

        sendScreenAnalytics();

        initActivity();

        setListeners();

        user = ParseUser.getCurrentUser();
        if (user!=null){
            //Sono nel caso dello switch, riempio i campi già noti
            suppName.setText(user.get("displayName").toString());
            suppEmail.setText(user.get("userEmail").toString());
            startingMail = user.get("userEmail").toString();
            int anno = user.getInt("anno");
            Log.i("anno", anno + "");
            if (anno != 0)
                suppAnno.setText(anno + "");
            this.photo = (ParseFile) user.get("profilePhoto");
            setSuppPicture();
        }
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
        t.setScreenName("SIGNUP - FORNITORE BIO");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void setSuppPicture(){
        this.suppPic = (RoundedImageView) findViewById(R.id.supp_pic);
        if (photo != null) {
            Picasso.with(this).load(photo.getUrl()).into(suppPic);
//            photo.getDataInBackground(new GetDataCallback() {
//                @Override
//                public void done(byte[] data, ParseException e) {
//                    if (e == null) {
//                        suppPic.setImageDrawable(null);
//                        Log.i("data length", data.length + "");
//                        int scaleStep = getResources().getInteger(R.integer.photo_scale_step);
//                        int scaleFactor = data.length / scaleStep;
//                        Log.i("scaleFactor", scaleFactor + "");
//                        BitmapFactory.Options options=new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
//                        options.inPurgeable = true; // inPurgeable is used to free up memory while required
//                        if(scaleFactor > 1)
//                            options.inSampleSize = scaleFactor;
//                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
//                        suppPic.setImageBitmap(bmp);
//                    } else {
//                    }
//                }
//            });
            haschangedPhoto = true;
        }
    }

    private void setListeners(){
        this.pIVA_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Reset fields depending on user's P.IVA enabling
                resetFields(isChecked);
            }
        });

        this.suppPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Salvataggio su file, forse non necessario
                final File root = new File(Environment.getExternalStorageDirectory() + "/Jobbe/");
                root.mkdirs();
                final String fname = "userpic.jpg";
                final File sdImageMainDirectory = new File(root, fname);
                suppPicUri = Uri.fromFile(sdImageMainDirectory);

                //From camera
                final List<Intent> cameraIntents = new ArrayList<Intent>();
                final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                final PackageManager packageManager = getPackageManager();
                final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
                for (ResolveInfo res : listCam) {
                    final String packageName = res.activityInfo.packageName;
                    final Intent intent = new Intent(captureIntent);
                    intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    intent.setPackage(packageName);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, suppPicUri);
                    cameraIntents.add(intent);
                }

                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                //Chooser of filesystem options.
                final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

                //Add the camera options.
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

                startActivityForResult(chooserIntent, 1234);
            }
        });

        this.suppAvanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nome = suppName.getText().toString();
                email = suppEmail.getText().toString();
                CF = suppCF.getText().toString();
                desc = suppDesc.getText().toString();
                ditta = suppDitta.getText().toString();
                anno = suppAnno.getText().toString();
                partIva = pIVA.getText().toString();
                if (checkCode(nome, email, CF, desc, anno)) {
                    if (getIntent().getBooleanExtra("switchFromClient", false) == true) {
                        spinnerLayout.setVisibility(View.VISIBLE);

                        new saveAsync().execute();
                        // INIZIO ROBA DA PORTARE IN ASYNCTASK
                        /*
                        //Salva utente e vai alla home

                        user = ParseUser.getCurrentUser();
                        user.put("displayName", nome);
                        user.put("userEmail", email);
                        user.put("codFiscale", CF);
                        user.put("desc", desc);
                        if(!ditta.isEmpty())
                            user.put("ditta", ditta);
                        if(!anno.isEmpty())
                            user.put("anno", Integer.parseInt(anno));
                        if (haschangedPhoto){
                            user.put("profilePhoto", new ParseFile(new File(Environment.getExternalStorageDirectory() + getResources().getString(R.string.pic_path))));
                        }
                        user.put("type","supplier");
                        user.put("isSupplier", true);

                        //Creo i job dell'utente
                        ArrayList<String> cityList = getIntent().getStringArrayListExtra("cityList");
                        ArrayList<ParseObject> zones = new ArrayList<>();
                        ArrayList<String> jobArray = new ArrayList<>();

                        for (String city : cityList) {
                            try {
                                zones.add(
                                        (ParseObject) ParseQuery.getQuery("Zone")
                                                .whereEqualTo("active", true)
                                                .whereEqualTo("nome", city).find().get(0));
                                Log.i("CITY", zones.get(0).get("nome").toString());
                            } catch (ParseException exc) {
                                exc.printStackTrace();
                            }
                        }

                        for (ParseObject zone : zones) {
                            ParseObject job = new ParseObject("Jobs");
                            job.put("name", selectedJob);
                            job.put("supplierId", user);
                            if (!pIVA.getText().toString().isEmpty()) job.put("piva", pIVA.getText().toString());
                            job.put("zoneId", zone);

                            Map<String, Object> empty = new HashMap<String, Object>() {
                            };
                            try {
                                HashMap<String, Object> result = ParseCloud.callFunction("getJobs", empty); //Synchronous call in order to wait until categories retrieval

                                String jobTofind = selectedJob.toString();
                                ArrayList<ParseObject> categorylist = (ArrayList<ParseObject>) result.get("jobCategory");
                                ArrayList<ParseObject> joblist = (ArrayList<ParseObject>) result.get("jobList");
                                for (ParseObject category : categorylist) {
                                    ArrayList<HashMap<String, String>> jobs = (ArrayList<HashMap<String, String>>) category.get("jobs");
                                    for (HashMap<String, String> x : jobs) {
                                        String jobName = x.get("name");
                                        if (jobName.equals(jobTofind)) {
                                            Log.i("JOB FOUND", "Job name: " + x.get("name") + " Job Id: " + x.get("id") + " Category Name: " + (String) category.get("name") + " Category Id: " + category.getObjectId());
                                            //job.put("categoryListId", category.getObjectId());
                                            jobArray.add(x.get("id"));
                                            job.put("categoryListId", category);
                                            for (ParseObject y : joblist) {
                                                if (y.getObjectId().equals(x.get("id"))){
                                                    job.put("jobListId",y);
                                                }
                                            }
                                            //job.put("jobListId", x.get("id"));
                                            Log.i("SAVE JOB", "START");
                                            job.save();
                                            Log.i("SAVE JOB", "DONE");
                                        }
                                    }
                                }
                            } catch (ParseException e1) {
                                Log.e("ParseException", e1.toString());
                            }
                        }
                        Log.i("MATCH REQUESTS", "START");
                        matchNewSupplierRequestsAPIcall(zones, jobArray);
                        Log.i("MATCH REQUESTS", "DONE");

                        //Salvo l'utente
                        try {
                            Log.i("SAVE USER", "START");
                            user.save();
                            Log.i("SAVE USER", "DONE");

                            //Go to supplier home
                            Intent intent = new Intent(CreateSupplierActivity.this, HomeSupplierActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        }
                        catch (ParseException e){
                            Toast.makeText(getApplicationContext(), "C'è stato un errore, le modifiche non verranno salvate", Toast.LENGTH_LONG).show();
                        }
                        spinnerLayout.setVisibility(View.GONE);
                        */
                    }

                    // FINE ROBA DA PORTARE IN ASYNCTASK

                    else {
                        Intent intent = new Intent(CreateSupplierActivity.this, PhoneNumberActivity.class);
                        intent.putExtra("job", selectedJob.toString());
                        intent.putExtra("displayName", suppName.getText().toString());
                        intent.putExtra("email", suppEmail.getText().toString());
                        intent.putExtra("type", "supplier");
                        intent.putExtra("suppPIVA", pIVA.getText().toString());
                        intent.putExtra("suppCF", suppCF.getText().toString());
                        intent.putExtra("suppDitta", suppDitta.getText().toString());
                        intent.putExtra("suppDesc", suppDesc.getText().toString());
                        intent.putExtra("anno", suppAnno.getText().toString());
                        intent.putExtra("cityList", getIntent().getStringArrayListExtra("cityList"));
                        for (String city : getIntent().getStringArrayListExtra("cityList")) {
                            Log.i("CreateSupplierCITY", city);
                        }
                        intent.putExtra("suppLatitude", getIntent().getDoubleExtra("suppLatitude", 0));
                        intent.putExtra("suppLongitude", getIntent().getDoubleExtra("suppLongitude", 0));
                        startActivity(intent);
                        overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
                    }
                }
            }
        });
    }

    private void initActivity(){
        this.selectedJob = getIntent().getStringExtra("selectedJob");
        this.supplier_job = (TextView) findViewById(R.id.supplier_job);
        this.supplier_job.setText(this.selectedJob);
        this.pIVA_switch = (Switch) findViewById(R.id.p_iva_switch);
        this.pIVA_switch.setChecked(getIntent().getBooleanExtra("pIVA_enabled", false));
        this.pIVA = (EditText) findViewById(R.id.p_iva);
        this.suppName = (EditText) findViewById(R.id.supp_name);
        this.suppEmail = (EditText) findViewById(R.id.supp_email);
        this.suppDitta = (EditText) findViewById(R.id.supp_ditta);
        this.suppDesc = (ActionEditText) findViewById(R.id.supp_desc);
        this.suppCF = (EditText) findViewById(R.id.supp_cod_fis);
        this.suppAnno = (EditText) findViewById(R.id.supp_anno);
        this.suppPic = (RoundedImageView) findViewById(R.id.supp_pic);
        this.suppAvanti = (Button) findViewById(R.id.supp_avanti);
        this.spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);

        suppDesc.setHorizontallyScrolling(false);
        suppDesc.setLines(1);
        suppDesc.setMaxLines(10);

        startingMail = "";

        this.resetFields(this.pIVA_switch.isChecked());
    }

    private void resetFields(boolean isChecked){
        if(!isChecked){
            this.pIVA.setVisibility(View.GONE);
            this.pIVA.setText("");
            this.suppDitta.setVisibility(View.GONE);
            this.suppDitta.setText("");
        }
        else{
            this.pIVA.setVisibility(View.VISIBLE);
            this.suppDitta.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        byte[] image;
        if (resultCode == RESULT_OK) {
            if (requestCode == 1234) {
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
                    selectedImageUri = suppPicUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }
                //Log.i("imageUri", selectedImageUri.toString());
                if (selectedImageUri != null){
                    try {
                        int rotation = getImageRotation(selectedImageUri);
                        Log.i("rotation", rotation + "");
                        image = getBytesFromUri(selectedImageUri);
                        final File root = new File(Environment.getExternalStorageDirectory() + "/Jobbe/");
                        root.mkdirs();
                        final String fname = "userpic.jpg";
                        final File sdImageMainDirectory = new File(root, fname);
                        /*
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(sdImageMainDirectory));
                        bos.write(image);
                        bos.flush();
                        bos.close();
                        haschangedPhoto = true;

                        suppPic.setImageDrawable(null);
                        Uri uri = Uri.fromFile(sdImageMainDirectory);
                        suppPic.setImageURI(uri);
                        */
                        setPic(sdImageMainDirectory.toString(), image, rotation);
                        haschangedPhoto = true;
                    }
                    catch (IOException e){
                        Toast.makeText(getApplicationContext(), "Attenzione, qualcosa è andato storto", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    public byte[] getBytesFromUri(Uri uri) throws IOException {

        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        //This is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        //We need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        //And then we can return your byte array.
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
        Log.i("imageSize", image.length + "");
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        suppPic.setImageBitmap(bitmap);
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Log.i("Orientation", "merda staladita");
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    private boolean checkCode(String name, String email, String CF, String desc, String anno){
        if(pIVA_switch.isChecked()) {
            if (pIVA.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Inserisci la partita IVA", Toast.LENGTH_LONG).show();
                return false;
            }

            if (!ValidPIVA(pIVA.getText().toString())) {
                Toast.makeText(getApplicationContext(), "La partita IVA inserita non è corretta", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        else if (name.isEmpty()) {
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
        } else if (!email.matches("^(.+)@(.+)\\.(.+)$")){
            Toast.makeText(getApplicationContext(), "L\'indirizzo email inserito non è valido", Toast.LENGTH_LONG).show();
            return false;
        }
        else if(CF.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Il campo codice fiscale non può essere vuoto", Toast.LENGTH_LONG).show();
            return false;
        }
        else if(!ValidCF(CF)) {
            Toast.makeText(getApplicationContext(), "Il codice fiscale non è corretto", Toast.LENGTH_LONG).show();
            return false;
        }
        else if(anno.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Il campo anno non può essere vuoto", Toast.LENGTH_LONG).show();
            return false;
        }
        else if ((!anno.matches("19[\\d][\\d]")) && (!anno.matches("20[\\d][\\d]"))) {
                Toast.makeText(getApplicationContext(), "L'anno di nascita non è corretto", Toast.LENGTH_LONG).show();
                return false;
        }
        else if(desc.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Il campo descrizione non può essere vuoto", Toast.LENGTH_LONG).show();
            return false;
        }
        else if(!haschangedPhoto){
            Toast.makeText(getApplicationContext(), "È obbligatorio inserire la foto", Toast.LENGTH_LONG).show();
            return false;
        }
        else if(!startingMail.equals(email)) {
                if (emailExists(email)) {
                    Toast.makeText(getApplicationContext(), "L'email inserita è già registrata", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        else if(!new RegexValidator(getApplicationContext()).validate(desc))
        return false;

        return true; //No problems here
    }

    private void matchNewSupplierRequestsAPIcall(ArrayList<ParseObject> zones, ArrayList<String> jobsId) {
        ArrayList<String> zonesId = new ArrayList<>();
        //ArrayList<String> jobsId = new ArrayList<>();
        Map<String, Object> params = new HashMap<String, Object>();
        for (ParseObject z : zones)
            zonesId.add(z.getObjectId());
        //for (ParseObject j : jobs)
        //    jobsId.add(j.getObjectId());

        params.put("jobs", jobsId);
        params.put("zones", zonesId);
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        try {
            ParseCloud.callFunction("matchNewSupplierRequests", params);
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }
    }

    static boolean ValidCF(String cf) {
        Log.i("In VALIDCF","Into");
        int i, s, c;
        String cf2;
        int setdisp[] = {1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20,
                11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23 };
        if( cf.length() == 0 )
            return false;
        Log.i("VALIDCF","CHECKPOINT 1");
        if( cf.length() != 16 )
            return false;
        Log.i("VALIDCF","CHECKPOINT 2");
        cf2 = cf.toUpperCase();
        for( i=0; i<16; i++ ){
            c = cf2.charAt(i);
            if( ! ( c>='0' && c<='9' || c>='A' && c<='Z' ) )
                return false;
        }
        Log.i("VALIDCF","CHECKPOINT 3");
        s = 0;
        for( i=1; i<=13; i+=2 ){
            c = cf2.charAt(i);
            if( c>='0' && c<='9' )
                s = s + c - '0';
            else
                s = s + c - 'A';
        }
        for( i=0; i<=14; i+=2 ){
            c = cf2.charAt(i);
            if( c>='0' && c<='9' )	 c = c - '0' + 'A';
            s = s + setdisp[c - 'A'];
        }
        if( s%26 + 'A' != cf2.charAt(15) )
            return false;

        Log.i("VALIDCF", "Seems valid: " + cf);
        return true;
    }


    static boolean ValidPIVA(String pi)
    {
        Log.i("VALID PIVA", "Into");
        int i, c, s;
        if( pi.length() == 0 )
            return false;
        Log.i("VALID PIVA", "Checkpoint 1");
        if( pi.length() != 11 )
            return false;
        Log.i("VALID PIVA", "Checkpoint 2");
        for( i=0; i<11; i++ ){
            if( pi.charAt(i) < '0' || pi.charAt(i) > '9' )
                return false;
        }
        Log.i("VALID PIVA", "Checkpoint 3");

        s = 0;
        for( i=0; i<=9; i+=2 )
            s += pi.charAt(i) - '0';
        for( i=1; i<=9; i+=2 ){
            c = 2*( pi.charAt(i) - '0' );
            if( c > 9 )  c = c - 9;
            s += c;
        }
        if( ( 10 - s%10 )%10 != pi.charAt(10) - '0' )
            return false;

        Log.i("VALID PIVA", "Seems valid: " + pi);
        return true;
    }

    private boolean emailExists(String email){
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

    private class saveAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            //Salva utente e vai alla home
            user = ParseUser.getCurrentUser();
            user.put("displayName", nome);
            user.put("userEmail", email);
            user.put("email", email);
            user.put("codFiscale", CF);
            user.put("desc", desc);
            if(!ditta.isEmpty())
                user.put("ditta", ditta);
            if(!anno.isEmpty())
                user.put("anno", Integer.parseInt(anno));
            if (haschangedPhoto){
                user.put("profilePhoto", new ParseFile(new File(Environment.getExternalStorageDirectory() + getResources().getString(R.string.pic_path))));
            }
            user.put("type","supplier");
            user.put("isSupplier", true);

            //Creo i job dell'utente
            ArrayList<String> cityList = getIntent().getStringArrayListExtra("cityList");
            ArrayList<ParseObject> zones = new ArrayList<>();
            ArrayList<String> jobArray = new ArrayList<>();

            for (String city : cityList) {
                try {
                    zones.add(
                            (ParseObject) ParseQuery.getQuery("Zone")
                                    .whereEqualTo("active", true)
                                    .whereEqualTo("nome", city).find().get(0));
                    Log.i("CITY", zones.get(0).get("nome").toString());
                } catch (ParseException exc) {
                    ParseErrorHandler.handleParseError(exc, getApplicationContext());
                    exc.printStackTrace();
                }
            }

            for (ParseObject zone : zones) {
                ParseObject job = new ParseObject("Jobs");
                job.put("name", selectedJob);
                job.put("supplierId", user);
                if (!partIva.isEmpty()){
                    job.put("piva", partIva);
                    job.put("supplierType", "professionist");
                } else
                    job.put("supplierType", "hobby");

                job.put("zoneId", zone);

                Map<String, Object> empty = new HashMap<String, Object>() {
                };
                try {
                    HashMap<String, Object> result = ParseCloud.callFunction("getJobs", empty); //Synchronous call in order to wait until categories retrieval

                    String jobTofind = selectedJob.toString();
                    ArrayList<ParseObject> categorylist = (ArrayList<ParseObject>) result.get("jobCategory");
                    ArrayList<ParseObject> joblist = (ArrayList<ParseObject>) result.get("jobList");
                    for (ParseObject category : categorylist) {
                        ArrayList<HashMap<String, String>> jobs = (ArrayList<HashMap<String, String>>) category.get("jobs");
                        for (HashMap<String, String> x : jobs) {
                            String jobName = x.get("name");
                            if (jobName.equals(jobTofind)) {
                                Log.i("JOB FOUND", "Job name: " + x.get("name") + " Job Id: " + x.get("id") + " Category Name: " + (String) category.get("name") + " Category Id: " + category.getObjectId());
                                //job.put("categoryListId", category.getObjectId());
                                jobArray.add(x.get("id"));
                                job.put("categoryListId", category);
                                for (ParseObject y : joblist) {
                                    if (y.getObjectId().equals(x.get("id"))){
                                        job.put("jobListId",y);
                                    }
                                }
                                //job.put("jobListId", x.get("id"));
                                Log.i("SAVE JOB", "START");
                                job.save();
                                Log.i("SAVE JOB", "DONE");
                            }
                        }
                    }
                } catch (ParseException e1) {
                    Log.e("ParseException", e1.toString());
                    ParseErrorHandler.handleParseError(e1, getApplicationContext());
                }
            }
            Log.i("MATCH REQUESTS", "START");
            matchNewSupplierRequestsAPIcall(zones, jobArray);
            Log.i("MATCH REQUESTS", "DONE");

            //Salvo l'utente
            try {
                Log.i("SAVE USER", "START");
                user.save();
                Log.i("SAVE USER", "DONE");

                //Go to supplier home
                Intent intent = new Intent(CreateSupplierActivity.this, HomeSupplierActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
            catch (ParseException e){
                Toast.makeText(getApplicationContext(), "C'è stato un errore, le modifiche non verranno salvate", Toast.LENGTH_LONG).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

            spinnerLayout.setVisibility(View.GONE);
        }

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

        if(suppPicUri != null)
            savedInstanceState.putString("outputFileUri", suppPicUri.toString());

        super.onSaveInstanceState(savedInstanceState);
    }
    //onRestoreInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        if(suppPicUri != null) {
            String uriString = savedInstanceState.getString("outputFileUri");
            File file = new File(uriString);
            suppPicUri = Uri.fromFile(file);
        }
    }

}
