package com.example.cerberus;

import static com.example.cerberus.PhotoLoader.REQUEST_CODE_READ_STORAGE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.twitter.sdk.android.core.TwitterCore;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedActivity extends AppCompatActivity {

    public static final int AREA_CODE_GREECE = 23424833;
    public static final int AREA_CODE_WORLD = 1;
    public static final String TWITTER_TAG = "Twitter";

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

    private final PhotoLoader photoLoader = new PhotoLoader(this);
    private PhotoLoader.PhotoInfo loadedBitmapInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        findAllViews();
        setUpSearchView();
        setUpButtonListeners();
    }

    private void setUpSearchView() {
        int searchPlateId = getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = findViewById(searchPlateId);
        searchPlate.setBackgroundResource(0);
    }

    private void getTrendList() {
        Call<ResponseBody> call = getTwitterClient().getTrendsService().getTrends(AREA_CODE_GREECE);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        String resultString = response.body().string();
                        JSONArray trends = new JSONArray(resultString).getJSONObject(0).getJSONArray("trends");
                        List<String> trendList = new ArrayList<>();
                        for (int i = 0; i < trends.length(); i++) {
                            trendList.add(trends.getJSONObject(i).getString("name"));
                        }
                    }
                    else Log.d(TWITTER_TAG, response.errorBody().string());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TWITTER_TAG, "Failed getting trends");
                t.printStackTrace();
            }
        });
    }

    private CustomTwitterApiClient getTwitterClient() {
        return (CustomTwitterApiClient) TwitterCore.getInstance().getApiClient();
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
            photoLoader.pickPhoto();
        });

        deletePhotoButton.setOnClickListener(v -> {
            loadedBitmapInfo = null;
            photoButtonLayout.setVisibility(View.GONE);
            addPhotoButton.setVisibility(View.VISIBLE);
        });
    }

    private void updatePostButton() {
        //If at least one toggle is checked, set active
        if (fbPostToggle.isChecked() || twPostToggle.isChecked() || instaPostToggle.isChecked())
            postButton.setEnabled(true);
        else if (!fbPostToggle.isChecked() && !twPostToggle.isChecked() && !instaPostToggle.isChecked())
            postButton.setEnabled(false);
    }

    private void onPhotoReturned(Intent data) {
        photoButtonLayout.setVisibility(View.VISIBLE);
        addPhotoButton.setVisibility(View.INVISIBLE);
        loadedBitmapInfo = photoLoader.getPhotoInfo(data.getData());
        photoImageView.setImageBitmap(loadedBitmapInfo.bitmap);
        photoNameTextView.setText(loadedBitmapInfo.name);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQUEST_CODE_READ_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photoLoader.onPermissionGranted();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == PhotoLoader.PHOTO_RETURN_CODE) {
            onPhotoReturned(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}