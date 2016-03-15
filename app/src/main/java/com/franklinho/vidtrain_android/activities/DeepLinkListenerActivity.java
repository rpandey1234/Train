package com.franklinho.vidtrain_android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.franklinho.vidtrain_android.R;

public class DeepLinkListenerActivity extends AppCompatActivity {
    public static final String VIDTRAIN_DEEP_LINK = "/vidtrain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link_listener);
        Intent intent = getIntent();
        if (intent == null || intent.getData() == null) {
            finish();
        }
        openDeepLink(intent.getData());
        finish();


    }

    private void openDeepLink(Uri deepLink) {

        String path = deepLink.getPath();


        if (VIDTRAIN_DEEP_LINK.equals(path)) {
            // Launch preferences
            String objectId = deepLink.getQueryParameter("objectid");
            Intent i = new Intent(this, VidTrainDetailActivity.class);
            i.putExtra(VidTrainDetailActivity.VIDTRAIN_KEY, objectId);
            startActivity(i);
        }
    }
}
