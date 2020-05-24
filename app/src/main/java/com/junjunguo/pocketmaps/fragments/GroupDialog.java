package com.junjunguo.pocketmaps.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
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
    private SharedPreferences sharedPreferences;

    public GroupDialog (Activity activity) {
        this.activity = activity;
        //sharedPreferences = MapActivity.getSharedPreferences();
        groupCreateVP = (ViewGroup) activity.findViewById(R.id.group_create_layout);
        groupJoinVP = (ViewGroup) activity.findViewById(R.id.group_join_layout);
        initJoinBtn();
        initCreateBtn();
    }

    public void showGroupDialog (final ViewGroup calledFromVP, final int target) {
        if(target == RA_GROUP_CREATE) {
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
                joinGroupWithUID(null);
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
                        disableJoinBtn();
                        GroupHandler.setIsGrouped(true);
                        GroupHandler.setLeaderState(GroupHandler.LeaderStateEnum.LEADER);
                        GroupHandler.setGroupUID(groupUID);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isGrouped", true);
                        editor.putBoolean("isLeader", true);
                        editor.putString("groupUID", groupUID);
                        editor.commit();

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

    private void joinGroupWithUID (String restoreUID) {
        TextView uidTextView = (TextView) activity.findViewById(R.id.gj_id_field);
        //Log.w(TAG, restoreUID);
        if(restoreUID != null) {
            uidTextView.setText(restoreUID);
        }
        //else if (restoreUID == null || restoreUID.equals("BLANK")) {
        //    return;
        //}

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
                                disableCreateBtn();
                                GroupHandler.setIsGrouped(true);
                                GroupHandler.setLeaderState(GroupHandler.LeaderStateEnum.NOT_LEADER);
                                GroupHandler.setGroupUID(groupUID);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("isGrouped", true);
                                editor.putBoolean("isLeader", false);
                                editor.putString("groupUID", groupUID);
                                editor.commit();

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

    public void restoreGroupStatus(String restoreUID) {
        joinGroupWithUID(restoreUID);
        disableCreateBtn();
    }

    public void setPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void deleteGroup() {
        // TODO Remove map markers
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups").document(GroupHandler.getGroupUID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        Toast.makeText(activity, "Group deleted.", Toast.LENGTH_SHORT).show();

                        GroupHandler.setIsGrouped(false);
                        GroupHandler.setLeaderState(GroupHandler.LeaderStateEnum.NOT_GROUPED);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.commit();

                        createBtn.setEnabled(true);
                        joinBtn.setEnabled(true);

                        createBtn.setBackgroundColor(activity.getResources().getColor(R.color.ridealong_blue));
                        joinBtn.setBackgroundColor(activity.getResources().getColor(R.color.ridealong_blue));

                        TextView create_uid = (TextView) activity.findViewById(R.id.gp_id_field);
                        TextView join_uid = (TextView) activity.findViewById(R.id.gj_id_field);

                        create_uid.setText("");
                        join_uid.setText("");

                        join_uid.setEnabled(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                        Toast.makeText(activity, "Failed to delete group.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void leaveGroup() {
        // TODO remove map markers

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docRef = db.collection("Groups").document(GroupHandler.getGroupUID());

        // Remove the user location field from the document
        Map<String,Object> updates = new HashMap<>();
        updates.put(user.getUid(), FieldValue.delete());

        docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Field successfully deleted!");
                Toast.makeText(activity, "Group left.", Toast.LENGTH_SHORT).show();

                GroupHandler.setIsGrouped(false);
                GroupHandler.setLeaderState(GroupHandler.LeaderStateEnum.NOT_GROUPED);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();

                createBtn.setEnabled(true);
                joinBtn.setEnabled(true);

                createBtn.setBackgroundColor(activity.getResources().getColor(R.color.ridealong_blue));
                joinBtn.setBackgroundColor(activity.getResources().getColor(R.color.ridealong_blue));

                TextView create_uid = (TextView) activity.findViewById(R.id.gp_id_field);
                TextView join_uid = (TextView) activity.findViewById(R.id.gj_id_field);

                create_uid.setText("");
                join_uid.setText("");

                join_uid.setEnabled(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                        Toast.makeText(activity, "Failed to leave group.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
