package com.trainapp.utilities;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.trainapp.models.User;

import java.util.List;

/**
 * A helper class to interact with the Facebook graph API.
 */
public class FacebookUtility {
    public static void getFacebookFriendsUsingApp(final FriendLoaderCallback friendLoader) {
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        List<String> facebookFriends = Utility.getFacebookFriends(response, "id");
                        ParseQuery<User> userQuery = ParseQuery.getQuery("_User");
                        userQuery.whereContainedIn("fbid", facebookFriends)
                                .findInBackground(new FindCallback<User>() {
                                    public void done(List<User> users, ParseException e) {
                                        if (e == null) {
                                            friendLoader.setUsers(users);
                                        } else {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    }
                }
        ).executeAsync();
    }
}
