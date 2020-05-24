package com.junjunguo.pocketmaps.group;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.activities.MapActivity;
import com.junjunguo.pocketmaps.model.GroupMember;

import org.oscim.core.GeoPoint;

import java.lang.reflect.Array;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupHandler {

    private final static String TAG = "GROUP_HANDLER";

    private static boolean isGrouped = false;
    public enum LeaderStateEnum{
        LEADER,
        NOT_LEADER,
        NOT_GROUPED,
    }
    private static LeaderStateEnum leaderState = LeaderStateEnum.NOT_GROUPED;
    private static String lastMessage = null;
    private static String groupUID = "BLANK";
    private static DocumentSnapshot groupDocument = null;
    private static Map<String, ArrayList> groupData = null;
    private static Map<String, ArrayList> locations = null;
    private static ArrayList<Double> destination = null;

    /**
     * Called by MapActivity.updateCurrentLocation()
     * Retrieve relevant group document from firebase so it can be easily accessed.
     */
    public static void getGroupData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Groups").document(groupUID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    groupDocument = task.getResult();
                    if (groupDocument.exists()) {
                        Log.d(TAG, "Got GroupData");
                        Map<String, Object> data = groupDocument.getData();
                        groupData = new HashMap<String, ArrayList>();
                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            groupData.put(entry.getKey(), (ArrayList) entry.getValue());
                        }
                    } else {
                        Log.d(TAG, "No such document / Document deleted");
                        // TODO here quit group since document is not found.
                        isGrouped = false;
                        leaderState = LeaderStateEnum.NOT_GROUPED;
                        groupUID = "BLANK";
                        // This should be enough to leave group, I think?
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public static void setGroupUID(String UID) {
       GroupHandler.groupUID = UID;
    }

    public static String getGroupUID() {
        return GroupHandler.groupUID;
    }

    public static void setLocalDestination(double latitude, double longitude) {
        destination.set(0, latitude);
        destination.set(1, longitude);
    }

    /**
     * Post destination to firebase.
     */
    public static void postDestination(double latitude, double longitude) {

    }

    /**
     * Read destination from firebase.
     * @return destination geopoint.
     */
    public static ArrayList<Double> getDestination() {
        destination = (ArrayList<Double>) groupData.get("Destination");
        return destination;
    }

    /**
     * Read user locations from firebase.
     * @return array of UID and locations.
     */
    public static Map<String, ArrayList> getLocations() {
        // THIS NEEDS FIXING. STATIC VARIABLES WAS A STUPID IDEA.
        locations = groupData;
        try {
            locations.remove("Destination");
            locations.remove(FirebaseAuth.getInstance().getCurrentUser().getUid());
        } catch (Exception e) {
            //Probably needs to be changed to only check for null.
            // Do nothing, simply means no destination was uploaded to firebase yet.
            // Also remove user's own location.
        }
        return locations;
    }

    public static void postLocation(double latitude, double longitude) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        List<Double> geoPoint = new ArrayList<>();
        geoPoint.add(0, latitude);
        geoPoint.add(1, longitude);

        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put(user.getUid(), geoPoint);

        db.collection("Groups").document(groupUID)
                .update(locationMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    public void joinGroup(String groupUID) {
        // TODO Currently in GroupDialog.java, consider moving here.
    }

    public void createGroup() {
        // TODO Currently in GroupDialog.java, consider moving here.
    }

    /**
     * Remove member information from firebase.
     * @return true if successful, false otherwise.
     */
    public static boolean leaveGroup() {
        // TODO remove user's location field from firebase
        // TODO remove map markers

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docRef = db.collection("Groups").document(groupUID);

        // Remove the user location field from the document
        Map<String,Object> updates = new HashMap<>();
        updates.put(user.getUid(), FieldValue.delete());

        docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Field successfully deleted!");
                isGrouped = false;
                leaderState = LeaderStateEnum.NOT_GROUPED;
            }
        });
        return true;
    }

    /**
     * Delete all information regarding group from firebase.
     */
    public static void deleteGroup() {
        // TODO Remove map markers
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups").document(groupUID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        isGrouped = false;
                        leaderState = LeaderStateEnum.NOT_GROUPED;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    /**
     * Check message document on firebase.
     * @lastMessage contains last read message, or null.
     * @return String if new message found, null otherwise.
     */
    public String checkMessages() {
        return null;
    }

    /**
     * Post new message to firebase.
     * @param message
     */
    public void sendMessage(String message) {

        if (message == null) {return;}

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Long lTimeStamp = System.currentTimeMillis();
        final String timeStamp = Long.toString(lTimeStamp);

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(timeStamp, message);

        db.collection("Groups_messages").document(groupUID)
                .set(messageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    public static LeaderStateEnum getLeaderState() {
        return leaderState;
    }

    public static LeaderStateEnum setLeaderState(LeaderStateEnum leaderState) {
        GroupHandler.leaderState = leaderState;
        return GroupHandler.leaderState;
    }

    public static boolean getIsGrouped() {
        return isGrouped;
    }

    public static boolean setIsGrouped(boolean isGrouped) {
        GroupHandler.isGrouped = isGrouped;
        return GroupHandler.isGrouped;
    }
}
