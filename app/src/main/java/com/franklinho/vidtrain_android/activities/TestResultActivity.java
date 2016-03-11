package com.franklinho.vidtrain_android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;

import java.util.List;

import butterknife.ButterKnife;

public class TestResultActivity extends Activity {

    private String queryString;
    private User user;
    private String queryResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);
        Intent intent = getIntent();
        ButterKnife.bind(this);

        queryString = intent.getStringExtra("name");

        ParseQuery<User> query = ParseQuery.getQuery("User");
        query.whereEqualTo("name","Vimalathithan Rajasekaran");;
        //query.setLimit(1);
        //query.whereEqualTo("name", "vimalathithanit");
        //query.whereStartsWith("name", queryString);
        //query.whereContains("name", queryString);
        //query.whereEqualTo("objectId", vidTrainObjectID);
        //query.setLimit(1);
        Log.i("Entering", "Start");
        query.findInBackground(new FindCallback<User>() {

            @Override
            public void done(List<User> objects, ParseException e) {
                if (e == null) {
                    user = objects.get(0);
                    //user = objects.get(0).get("name").toString();
                    queryResult = objects.get(0).get("name").toString();
                    Log.i("Entering", user.toString());
                    Log.i("Entering", queryResult);
                    //queryResult = user.get("username").toString();
                } else{
                    Log.i("Entering", "Error");

                }
            }

//            @Override
//            public void done(List<User> objects, ParseException e) {
//                if (e == null) {
//                    //user = objects.get(0).get("name").toString();
//                    queryResult = objects.get(0).get("name").toString();
//                    //queryResult = user.get("username").toString();
//                }
//            }

        });
        Log.i("Entering", "Done");

        Toast.makeText(this, queryResult, Toast.LENGTH_LONG).show();

    }
}
