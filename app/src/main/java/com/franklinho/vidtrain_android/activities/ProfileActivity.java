package com.franklinho.vidtrain_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.fragments.PopularFragment;
import com.franklinho.vidtrain_android.fragments.UserCreationsFragment;
import com.franklinho.vidtrain_android.fragments.UserInfoFragment;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {

    public static final String USER_ID = "userId";
    UserCreationsFragment userProfileFragment;
    @Bind(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String userId = getIntent().getStringExtra(USER_ID);
        if (userId == null) {
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser == null) {
                Intent intent = new Intent(this, LogInActivity.class);
                startActivity(intent);
                return;
            }
            userId = currentUser.getObjectId();
        }
        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            UserInfoFragment userInfoFragment = UserInfoFragment.newInstance(userId);
            ft.replace(R.id.flUserInfo, userInfoFragment);

            userProfileFragment = UserCreationsFragment.newInstance(userId);
            ft.replace(R.id.flUserContent, userProfileFragment);

            ft.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionLogout) {
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                    startActivity(intent);
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
