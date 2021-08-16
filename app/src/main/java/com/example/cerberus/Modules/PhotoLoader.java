package com.example.cerberus.Modules;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.cerberus.FeedActivity;

public class PhotoLoader {
    private final FeedActivity feedActivity;
    public static final String TAG = "TAG";
    private final ActivityResultLauncher<String> requestPermissionLauncher;
    private final ActivityResultLauncher<Intent> pickPhotoLauncher;

    public PhotoLoader(FeedActivity feedActivity) {
        this.feedActivity = feedActivity;

        //These launchers need to be initialized here
        requestPermissionLauncher = feedActivity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (result) {
                        Log.d(TAG, "Permission granted.");
                        pickPhoto();
                    }
                });
        pickPhotoLauncher = feedActivity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        feedActivity.onPhotoReturned(getPhotoInfo(result.getData().getData()));
                    }
                });
    }

    public void init() {
        if (ContextCompat.checkSelfPermission(feedActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Already has permission");
            pickPhoto();
        }
        else {
            Log.d(TAG, "Asking permission...");
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickPhotoLauncher.launch(intent);
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

    public static class PhotoInfo {
        public Bitmap bitmap;
        public String path, name;

        public PhotoInfo(String path) {
            this.path = path;
            this.name = path.substring(path.lastIndexOf("/") + 1);
            this.bitmap = BitmapFactory.decodeFile(path);
        }
    }
}
