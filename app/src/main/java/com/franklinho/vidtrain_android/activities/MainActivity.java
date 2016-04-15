package com.franklinho.vidtrain_android.activities;

import android.content.Intent;
import android.support.annotation.BinderThread;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.fragments.PopularFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final int VIDEO_CAPTURE = 101;
    public static final String UNIQUE_ID_INTENT = "UNIQUE_ID";
    public static final String SHOW_CONFIRM = "SHOW_CONFIRM";

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.conversations_fragment) FrameLayout conversationsFragment;

    private String uniqueId = Long.toString(System.currentTimeMillis());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.conversations_fragment, PopularFragment.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.miProfile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.button_create)
    public void startCreateFlow(View view) {
        Intent intent = new Intent(getBaseContext(), VideoCaptureActivity.class);
        intent.putExtra(UNIQUE_ID_INTENT, uniqueId);
        intent.putExtra(SHOW_CONFIRM, false);
        startActivityForResult(intent, VIDEO_CAPTURE);
    }
}