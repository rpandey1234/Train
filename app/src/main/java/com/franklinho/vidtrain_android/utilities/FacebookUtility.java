package com.franklinho.vidtrain_android.utilities;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.franklinho.vidtrain_android.activities.CreationDetailActivity.FriendCallable;
import com.franklinho.vidtrain_android.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

/**
 * A helper class to interact with the Facebook graph API.
 */
public class FacebookUtility {

    public static void getFacebookFriendsUsingApp(final FriendCallable func) {
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
                                            try {
                                                func.setUsers(users);
                                                Integer call = func.call();
                                            } catch (Exception e1) {
                                                e1.printStackTrace();
                                            }
//                                            friends.addAll(users);
//                                            friendsAdapter.notifyDataSetChanged();
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
