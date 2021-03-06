package ddwucom.mobileapplication.ma02_20181030.record_memo;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.libraries.places.api.model.Place;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ddwucom.mobileapplication.ma02_20181030.R;

public class AddMemoActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 200;

    private String mCurrentPhotoPath;

    ImageView ivPhoto;
    EditText etCafeName;
    EditText etLocation;
    RatingBar add_ratingbar;
    EditText etMemo;


    File photoFile = null;
    MemoDBHelper helper;

    Intent intent;
    Place place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_memo);

        helper = new MemoDBHelper(this);

        intent = getIntent();

        ivPhoto = (ImageView)findViewById(R.id.iv_addphoto);
        etMemo = (EditText)findViewById(R.id.etMemo);
        etCafeName = (EditText)findViewById(R.id.etCafeName);
        etLocation = (EditText)findViewById(R.id.etLocationName);
        add_ratingbar = findViewById(R.id.add_ratingbar);

        ivPhoto.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    ?????? ????????? ??????
                    dispatchTakePictureIntent();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(intent.getParcelableExtra("place") != null){
            place = intent.getParcelableExtra("place");
            etCafeName.setText(place.getName());
            etLocation.setText(place.getAddress());
        }
        if(intent.getStringExtra("address") != null){
            etLocation.setText(intent.getStringExtra("address"));
        }
    }

    public void savePhoto() {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues row = new ContentValues();
        row.put(MemoDBHelper.PATH, mCurrentPhotoPath);//??????
        Log.d("ADD", "path : " + mCurrentPhotoPath);
        row.put(MemoDBHelper.DATE, new SimpleDateFormat("yyyyMMdd").format(new Date()));//??????
        row.put(MemoDBHelper.CAFENAME, etCafeName.getText().toString());//??????
        row.put(MemoDBHelper.LOCATION, etLocation.getText().toString());//??????
        row.put(MemoDBHelper.RATINGBAR, Float.toString(add_ratingbar.getRating()));//rate
        row.put(MemoDBHelper.MEMO, etMemo.getText().toString());//??????

        long result = db.insert(MemoDBHelper.TABLE_NAME, null, row);
        Log.d("result", String.valueOf(result));
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSave:
//                DB??? ????????? ????????? ?????? ?????? ??? ?????? ??????
                savePhoto();
                helper.close();
                finish();
                Toast.makeText(this, "Save!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnCancel:
                photoFile.delete();
                finish();
                break;
        }
    }
    /*?????? ?????? ?????? ??????*/
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){

            try{
                photoFile = createImageFile(); //?????????????????? ????????? ?????????
            }catch (IOException e){
                e.printStackTrace();
            }

            if(photoFile != null){
                Uri photoUri = FileProvider.getUriForFile(this,
                        "ddwucom.mobileapplication.ma02_20181030.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                //camera?????? fileprovider??? ?????? ??? ??????path??? ????????? ????????? ??? ?????????
                //intent????????????, uri??? ?????? ???????????????
                startActivityForResult(takePictureIntent,REQUEST_TAKE_PHOTO);
            }
        }

    }
    /*?????? ?????? ????????? ???????????? ?????? ?????? ??????*/
    //??? ?????? ??? ?????? ????????? ??????????????? mCurrentPhotoPath
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("ADD", mCurrentPhotoPath);
        //??????????????? ????????? ?????? path
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic();
            //????????? ????????? ?????? mCurrentPhotoPath??? ??? ????????? ?????????????????? ?????? ???????????? ????????? ????????????
            //mnt->sdcard->android->data->????????????->files->picture??? ????????????
            //camera?????? fileprovider??? ?????? ??? ??????path??? ????????? ????????? ??? ?????????
        }
    }
    /*????????? ????????? ImageView?????? ????????? ??? ?????? ????????? ??????*/
    private void setPic() {
        // Get the dimensions of the View
        int targetW = ivPhoto.getWidth();
        int targetH = ivPhoto.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        ivPhoto.setImageBitmap(bitmap);
    }

}
