package ddwucom.mobileapplication.ma02_20181030.record_memo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ddwucom.mobileapplication.ma02_20181030.R;

public class UpdateActivity extends AppCompatActivity {

    final static String TAG = "UpdateActivity";
    private static final int REQUEST_TAKE_PHOTO = 200;

    private String PhotoPath;

    int click_flag = 0;

    ImageView iv_update_Photo;
    EditText et_update_CafeName;
    EditText et_update_Location;
    RatingBar update_ratingbar;
    EditText et_update_Memo;

    MemoDBHelper helper;
    MemoDto memoDto;

    File photoFile = null;
    String id;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        helper = new MemoDBHelper(this);
        intent = getIntent();
        id = String.valueOf(getIntent().getSerializableExtra("id"));
        memoDto = findRecord(id);

        iv_update_Photo = (ImageView)findViewById(R.id.iv_updatephoto);
        et_update_Memo = (EditText)findViewById(R.id.et_update_Memo);
        et_update_CafeName = (EditText)findViewById(R.id.et_update_CafeName);
        et_update_Location = (EditText)findViewById(R.id.et_update_LocationName);
        update_ratingbar = findViewById(R.id.update_ratingbar);

        et_update_Memo.setText(memoDto.getMemo());
        et_update_CafeName.setText(memoDto.getCafeName());
        et_update_Location.setText(memoDto.getLocation());
        update_ratingbar.setRating(Float.valueOf(memoDto.getRatingbar()));
        setPic(memoDto.getPhotoPath());

        iv_update_Photo.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    ?????? ????????? ??????
                    dispatchTakePictureIntent();
                    click_flag = 1;
                    return true;
                }
                return false;
            }
        });
    }

    /*????????? ????????? ImageView?????? ????????? ??? ?????? ????????? ??????*/
    private void setPic(String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = 150;
        int targetH = 150;

        Log.d("width", String.valueOf(targetW));

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
        iv_update_Photo.setImageBitmap(bitmap);
    }

    public MemoDto findRecord(String _id) {
        //			DB ?????? ?????? ??????
        MemoDto dto = new MemoDto();
        SQLiteDatabase myDB = helper.getReadableDatabase(); //select
        String selection = helper.ID+ "=?";
        String[] selectArgs = new String[]{_id};
        Cursor cursor =
                myDB.query(helper.TABLE_NAME, null, selection, selectArgs,
                        null, null, null, null);
        if (cursor.getCount() > 0) {
            long id = 0;
            String photoPath = null;
            String memo = null;
            String location = null;
            String cafeName = null;
            String rate = null;
            String date = null;

            while (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndex(helper.ID));
                photoPath = cursor.getString(cursor.getColumnIndex(helper.PATH));
                memo = cursor.getString(cursor.getColumnIndex(helper.MEMO));
                date = cursor.getString(cursor.getColumnIndex(helper.DATE));
                location = cursor.getString(cursor.getColumnIndex(helper.LOCATION));
                cafeName = cursor.getString(cursor.getColumnIndex(helper.CAFENAME));
                rate = cursor.getString(cursor.getColumnIndex(helper.RATINGBAR));
            }
            dto.set_id(id);
            dto.setMemo(memo);
            dto.setPhotoPath(photoPath);
            dto.setCafeName(cafeName);
            dto.setDate(date);
            dto.setLocation(location);
            dto.setRatingbar(rate);
        }
        return dto;
    }

    public void savePhoto() {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues row = new ContentValues();
        if(click_flag == 1){
            row.put(MemoDBHelper.PATH, PhotoPath);//????????? ?????? ??????????????? ????????? ????????????
        } else{
            row.put(MemoDBHelper.PATH, memoDto.getPhotoPath()); //?????????????????? ?????? ?????? ????????????
        }
        Log.d("ADD", "path : " + PhotoPath);
        row.put(MemoDBHelper.DATE, new SimpleDateFormat("yyyyMMdd").format(new Date()));//??????
        row.put(MemoDBHelper.CAFENAME, et_update_CafeName.getText().toString());//??????
        row.put(MemoDBHelper.LOCATION, et_update_Location.getText().toString());//??????
        row.put(MemoDBHelper.RATINGBAR, Float.toString(update_ratingbar.getRating()));//rate
        row.put(MemoDBHelper.MEMO, et_update_Memo.getText().toString());//??????

        String whereClause = MemoDBHelper.ID + "=?";
        String[] whereArgs = new String[]{id};
        int result = db.update(MemoDBHelper.TABLE_NAME, row, whereClause, whereArgs);
        helper.close();
        Log.d("result", String.valueOf(result));
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_update_Update:
//                DB??? ????????? ????????? ?????? ?????? ??? ?????? ??????
                savePhoto();
                helper.close();
                setResult(RESULT_OK);
                finish();
                Toast.makeText(this, "Update!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_update_Cancel:
                photoFile.delete();
                setResult(RESULT_CANCELED);
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        PhotoPath = image.getAbsolutePath();
        Log.d("ADD", PhotoPath);
        //??????????????? ????????? ?????? path
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic(PhotoPath);
            //????????? ????????? ?????? mCurrentPhotoPath??? ??? ????????? ?????????????????? ?????? ???????????? ????????? ????????????
            //mnt->sdcard->android->data->????????????->files->picture??? ????????????
            //camera?????? fileprovider??? ?????? ??? ??????path??? ????????? ????????? ??? ?????????
        }
    }

}