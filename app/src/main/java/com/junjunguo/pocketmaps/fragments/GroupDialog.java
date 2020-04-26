package com.junjunguo.pocketmaps.fragments;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.activities.ui.login.RegisterActivity;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GroupDialog {
    private final static int RA_GROUP_CREATE = 0;
    private final static int RA_GROUP_JOIN = 1;
    private final static int RANDOM_MAX = 10000;
    private final static int RANDOM_MIN = 0;

    private final static String TAG = "GROUP_DIALOG";

    private Activity activity;
    private ViewGroup groupCreateVP, groupJoinVP, title;
    private Button createBtn, joinBtn;

    public GroupDialog (Activity activity) {
        this.activity = activity;
        groupCreateVP = (ViewGroup) activity.findViewById(R.id.group_create_layout);
        groupJoinVP = (ViewGroup) activity.findViewById(R.id.group_join_layout);
    }

    public void showGroupDialog (final ViewGroup calledFromVP, final int target) {
        if(target == RA_GROUP_CREATE) {
            initCreateBtn();
            initCancelBtn(groupCreateVP, calledFromVP, RA_GROUP_CREATE);
            groupCreateVP.setVisibility(View.VISIBLE);
        } else if (target == RA_GROUP_JOIN) {
            initCancelBtn(groupJoinVP, calledFromVP, RA_GROUP_JOIN);
            groupJoinVP.setVisibility(View.VISIBLE);
        }
        calledFromVP.setVisibility(View.INVISIBLE);
    }

    private void initCancelBtn (final ViewGroup groupDialogVP, final ViewGroup calledFromVP, final int target) {
        Button cancelBtn = null;
        if(target == RA_GROUP_CREATE) {
            cancelBtn = (Button) activity.findViewById(R.id.gp_button_cancel);
        } else if (target == RA_GROUP_JOIN) {
            cancelBtn = (Button) activity.findViewById(R.id.gj_button_cancel);
        }
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupDialogVP.setVisibility(View.INVISIBLE);
                calledFromVP.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initCreateBtn () {
        createBtn = (Button) activity.findViewById(R.id.gp_button_create);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //int randomNum = ThreadLocalRandom.current().nextInt(RANDOM_MIN, RANDOM_MAX + 1);
                //TODO hash
                generateGroupUID();
            }
        });
    }

    private void disableCreateBtn () {
        createBtn.setEnabled(false);
        createBtn.setBackgroundColor(activity.getResources().getColor(R.color.ridealong_grey));
    }

    private void initJoinBtn () {
        joinBtn = (Button) activity.findViewById(R.id.gj_button_join);
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinGroupWithUID();
            }
        });
    }

    private void generateGroupUID () {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if(user == null) {
            Log.d(TAG, "User equals NULL");
        }

        /**
         * Group UID:
         * Get local time as int / long
         * Get int value of username or fb user
         * Combine and reduce to 8 digits
         */

        Long lTimeStamp = System.currentTimeMillis();


        //int groupUID = ThreadLocalRandom.current().nextInt(RANDOM_MIN, RANDOM_MAX + 1);
        final String groupUID = Long.toString(lTimeStamp);


        Map<String, Object> groupData = new HashMap<>();
        groupData.put(user.getUid(), "Location Geopoint");

        // Add a new document with a generated ID
        db.collection("Groups").document(String.valueOf(groupUID))
                .set(groupData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        TextView uid = (TextView) activity.findViewById(R.id.gp_id_field);
                        uid.setText(groupUID);
                        disableCreateBtn();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void joinGroupWithUID () {
        //TODO
    }
}
