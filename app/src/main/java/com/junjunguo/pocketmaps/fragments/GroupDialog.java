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
import com.google.firebase.firestore.GeoPoint;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.activities.MapActivity;
import com.junjunguo.pocketmaps.activities.ui.login.RegisterActivity;
import com.junjunguo.pocketmaps.group.GroupHandler;
import com.junjunguo.pocketmaps.map.MapHandler;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            initJoinBtn();
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

    private void disableJoinBtn () {
        joinBtn.setEnabled(false);
        joinBtn.setBackgroundColor(activity.getResources().getColor(R.color.ridealong_grey));
    }

    private void generateGroupUID () {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if(user == null) {
            Log.d(TAG, "User equals NULL");
        }

        Long lTimeStamp = System.currentTimeMillis();
        final String groupUID = Long.toString(lTimeStamp);

        List<Double> geoPoint = new ArrayList<>();
        geoPoint.add(0, MapActivity.getmCurrentLocation().getLatitude());
        geoPoint.add(1, MapActivity.getmCurrentLocation().getLongitude());

        Map<String, Object> groupData = new HashMap<>();
        groupData.put(user.getUid(), geoPoint);

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
                        GroupHandler.setIsGrouped(true);
                        GroupHandler.setLeaderState(GroupHandler.LeaderStateEnum.LEADER);
                        GroupHandler.setGroupUID(groupUID);
                        Toast.makeText(activity, "Group created successfully.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Toast.makeText(activity, "Failed to create group.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void joinGroupWithUID () {
        TextView uidTextView = (TextView) activity.findViewById(R.id.gj_id_field);
        if (uidTextView.getText() != null) {
            final String groupUID = uidTextView.getText().toString();
            if (!groupUID.isEmpty()) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                List<Double> geoPoint = new ArrayList<>();
                geoPoint.add(0, MapActivity.getmCurrentLocation().getLatitude());
                geoPoint.add(1, MapActivity.getmCurrentLocation().getLongitude());

                Map<String, Object> groupData = new HashMap<>();
                groupData.put(user.getUid(), geoPoint);

                db.collection("Groups").document(String.valueOf(groupUID))
                        .update(groupData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                TextView uid = (TextView) activity.findViewById(R.id.gj_id_field);
                                uid.setEnabled(false);
                                uid.setText(groupUID);
                                disableJoinBtn();
                                GroupHandler.setIsGrouped(true);
                                GroupHandler.setLeaderState(GroupHandler.LeaderStateEnum.NOT_LEADER);
                                GroupHandler.setGroupUID(groupUID);
                                Toast.makeText(activity, "Joined group " + groupUID, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                                Toast.makeText(activity, "Failed to join group.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }
}
