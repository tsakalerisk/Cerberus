package com.example.cerberus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PhotoLoader {
    private final FeedActivity feedActivity;
    public static boolean READ_STORAGE_PERM_GRANTED = false;
    public static final int REQUEST_CODE_READ_STORAGE = 101;
    public static final int PHOTO_RETURN_CODE = 102;
    public static final String LOAD_PHOTO_TAG = "Load_photo";

    public PhotoLoader(FeedActivity feedActivity) {
        this.feedActivity = feedActivity;
    }

    public void pickPhoto() {
        checkPermission();
        if (READ_STORAGE_PERM_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            feedActivity.startActivityForResult(intent, PHOTO_RETURN_CODE);
        }
    }

    public void checkPermission() {
        int hasReadStoragePermission = ContextCompat.checkSelfPermission(feedActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasReadStoragePermission == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOAD_PHOTO_TAG, "Already has permission");
            READ_STORAGE_PERM_GRANTED = true;
        }
        else {
            Log.d(LOAD_PHOTO_TAG, "Asking permission...");
            String[] permissionsToAsk = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(feedActivity, permissionsToAsk, REQUEST_CODE_READ_STORAGE);
        }
    }

    public void onPermissionGranted() {
        Log.d(LOAD_PHOTO_TAG, "Permission granted.");
        READ_STORAGE_PERM_GRANTED = true;
        pickPhoto();
    }

    public PhotoInfo getPhotoInfo(Uri selectedImage) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = feedActivity.getContentResolver().query(selectedImage, filePathColumn,
                null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String photoPath = cursor.getString(columnIndex);
        cursor.close();
        return new PhotoInfo(photoPath);
    }

    public class PhotoInfo {
        public Bitmap bitmap;
        public String path, name;

        public PhotoInfo(String path) {
            this.path = path;
            this.name = path.substring(path.lastIndexOf("/") + 1);
            this.bitmap = BitmapFactory.decodeFile(path);
        }
    }
}
