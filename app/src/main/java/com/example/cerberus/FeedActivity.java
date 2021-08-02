package com.example.cerberus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class FeedActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_STORAGE = 101;
    private final int IMAGE_RETURN_CODE = 0x5656;

    private SearchView searchView = null;
    private ToggleButton fbPostToggle = null;
    private ToggleButton twPostToggle = null;
    private ToggleButton instaPostToggle = null;
    private Button addPhotoButton = null;
    private Button postButton = null;
    private ConstraintLayout photoButtonLayout = null;
    private ImageView photoImageView = null;
    private TextView photoNameTextView = null;
    private Button deletePhotoButton = null;

    private static boolean READ_STORAGE_PERM_GRANTED = false;
    private ImageInfo loadedBitmapInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        findAllViews();
        setUpButtonListeners();

        int searchPlateId = getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = findViewById(searchPlateId);
        searchPlate.setBackgroundResource(0);
    }

    private void findAllViews() {
        searchView = findViewById(R.id.searchView);
        fbPostToggle = findViewById(R.id.fbPostToggle);
        twPostToggle = findViewById(R.id.twPostToggle);
        instaPostToggle = findViewById(R.id.instaPostToggle);
        addPhotoButton = findViewById(R.id.addPhotoButton);
        postButton = findViewById(R.id.postButton);
        photoButtonLayout = findViewById(R.id.photoButtonLayout);
        photoImageView = findViewById(R.id.photoImageView);
        photoNameTextView = findViewById(R.id.photoName);
        deletePhotoButton = findViewById(R.id.deletePhotoButton);
    }

    private void setUpButtonListeners() {
        fbPostToggle.setOnClickListener(v -> {
            if (fbPostToggle.isChecked()) {
                fbPostToggle.setBackground(ContextCompat.getDrawable(this, R.drawable.button_fill));
                fbPostToggle.getBackground().setTint(ContextCompat.getColor(this, R.color.com_facebook_blue));
                fbPostToggle.setTextColor(ContextCompat.getColor(this, R.color.white));
                fbPostToggle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0);
                fbPostToggle.getCompoundDrawables()[2].setTint(ContextCompat.getColor(this, R.color.white));
            }
            else {
                fbPostToggle.setBackground(ContextCompat.getDrawable(this, R.drawable.button_stroke));
                fbPostToggle.getBackground().setTint(ContextCompat.getColor(this, R.color.com_facebook_blue));
                fbPostToggle.setTextColor(ContextCompat.getColor(this, R.color.com_facebook_blue));
                fbPostToggle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.uncheck, 0);
                fbPostToggle.getCompoundDrawables()[2].setTint(ContextCompat.getColor(this, R.color.com_facebook_blue));
            }
            updatePostButton();
        });

        twPostToggle.setOnClickListener(v -> {
            if (twPostToggle.isChecked()) {
                twPostToggle.setBackground(ContextCompat.getDrawable(this, R.drawable.button_fill));
                twPostToggle.getBackground().setTint(ContextCompat.getColor(this, R.color.tw__blue_default));
                twPostToggle.setTextColor(ContextCompat.getColor(this, R.color.white));
                twPostToggle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0);
                twPostToggle.getCompoundDrawables()[2].setTint(ContextCompat.getColor(this, R.color.white));
            }
            else {
                twPostToggle.setBackground(ContextCompat.getDrawable(this, R.drawable.button_stroke));
                twPostToggle.getBackground().setTint(ContextCompat.getColor(this, R.color.tw__blue_default));
                twPostToggle.setTextColor(ContextCompat.getColor(this, R.color.tw__blue_default));
                twPostToggle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.uncheck, 0);
                twPostToggle.getCompoundDrawables()[2].setTint(ContextCompat.getColor(this, R.color.tw__blue_default));
            }
            updatePostButton();
        });

        instaPostToggle.setOnClickListener(v -> {
            if (instaPostToggle.isChecked()) {
                instaPostToggle.setBackground(ContextCompat.getDrawable(this, R.drawable.button_fill));
                instaPostToggle.getBackground().setTint(ContextCompat.getColor(this, R.color.instagram_pink));
                instaPostToggle.setTextColor(ContextCompat.getColor(this, R.color.white));
                instaPostToggle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0);
                instaPostToggle.getCompoundDrawables()[2].setTint(ContextCompat.getColor(this, R.color.white));
            }
            else {
                instaPostToggle.setBackground(ContextCompat.getDrawable(this, R.drawable.button_stroke));
                instaPostToggle.getBackground().setTint(ContextCompat.getColor(this, R.color.instagram_pink));
                instaPostToggle.setTextColor(ContextCompat.getColor(this, R.color.instagram_pink));
                instaPostToggle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.uncheck, 0);
                instaPostToggle.getCompoundDrawables()[2].setTint(ContextCompat.getColor(this, R.color.instagram_pink));
            }
            updatePostButton();
        });

        addPhotoButton.setOnClickListener(v -> {
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_CODE_READ_STORAGE, () -> READ_STORAGE_PERM_GRANTED = true);
            pickImage();
        });

        deletePhotoButton.setOnClickListener(v -> {
            loadedBitmapInfo = null;
            photoButtonLayout.setVisibility(View.GONE);
            addPhotoButton.setVisibility(View.VISIBLE);
        });
    }

    private void pickImage() {
        if (READ_STORAGE_PERM_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_RETURN_CODE);
        }
    }

    private void updatePostButton() {
        //If at least one toggle is checked, set active
        if (fbPostToggle.isChecked() || twPostToggle.isChecked() || instaPostToggle.isChecked())
            postButton.setEnabled(true);
        else if (!fbPostToggle.isChecked() && !twPostToggle.isChecked() && !instaPostToggle.isChecked())
            postButton.setEnabled(false);
    }

    private void onImageReturned(Intent data) {
        photoButtonLayout.setVisibility(View.VISIBLE);
        addPhotoButton.setVisibility(View.INVISIBLE);
        loadedBitmapInfo = getPictureInfo(data.getData());
        photoImageView.setImageBitmap(loadedBitmapInfo.bitmap);
        photoNameTextView.setText(loadedBitmapInfo.name);
    }

    public ImageInfo getPictureInfo(Uri selectedImage) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return new ImageInfo(picturePath);
    }

    public void checkPermission(String permission, final int code, SetFlagInterface setFlagInterface) {
        int hasReadStoragePermission = ContextCompat.checkSelfPermission(FeedActivity.this, permission);
        if (hasReadStoragePermission == PackageManager.PERMISSION_GRANTED) {
            setFlagInterface.setFlag();
        }
        else {
            String[] permissionsToAsk = new String[]{permission};
            ActivityCompat.requestPermissions(FeedActivity.this, permissionsToAsk, code);
        }
    }

    //So that on calling checkPermission I can pass a lambda that sets a different flag every time
    //See addPhotoButton listener.
    public interface SetFlagInterface {void setFlag();}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQUEST_CODE_READ_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    READ_STORAGE_PERM_GRANTED = true;
                    pickImage();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_RETURN_CODE) {
            onImageReturned(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class ImageInfo {
        public Bitmap bitmap;
        public String path, name;

        public ImageInfo(String path) {
            this.path = path;
            this.name = path.substring(path.lastIndexOf("/") + 1);
            this.bitmap = BitmapFactory.decodeFile(path);
        }
    }
}