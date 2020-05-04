package com.junjunguo.pocketmaps.group;

import android.util.Log;
import android.widget.TextView;

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
import com.junjunguo.pocketmaps.model.GroupMember;

import org.oscim.core.GeoPoint;

import java.security.acl.Group;
import java.util.HashMap;
import java.util.Map;

public class GroupHandler {

    private final static String TAG = "GROUP_HANDLER";

    private static boolean isGrouped = false;
    private enum LeaderStateEnum{
        LEADER,
        NOT_LEADER,
        NOT_GROUPED,
    }
    private static LeaderStateEnum leaderState = LeaderStateEnum.NOT_GROUPED;
    private static String lastMessage = null;
    private static String groupUID = "BLANK";
    private static DocumentSnapshot groupDocument = null;
    private static Map<String, Object> groupData = null;
    private static Map<String, Object> locations = null;
    private static GeoPoint destination = null;

    /**
     * Called by MapActivity.updateCurrentLocation()
     * Retrieve relevant group document from firebase so it can be easily accessed.
     */
    public void getGroupData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Groups").document(groupUID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    groupDocument = task.getResult();
                    if (groupDocument.exists()) {
                        Log.d(TAG, "DocumentSnapshot data");
                        groupData = groupDocument.getData();
                    } else {
                        Log.d(TAG, "No such document / Document deleted");
                        // TODO here quit group since document is not found.
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public String setGroupUID(String UID) {
        // TODO Consider if needed.
        return null;
    }

    public String getGroupUID() {
        return null;
    }

    /**
     * Post destination to firebase.
     * @return GeoPoint of posted destination.
     */
    public GeoPoint setDestination() {
        if(leaderState == LeaderStateEnum.LEADER)
        {
            // Post to firebase.
        }
        else
        {
            // Not leader cannot post destination.
            // Added as extra safety measure, shouldn't be called in the first place if user isn't leader.
        }
        return null;
    }

    /**
     * Read destination from firebase.
     * @return destination geopoint.
     */
    public GeoPoint getDestination() {
        destination = (GeoPoint) groupData.get("Destination");
        return destination;
    }

    /**
     * Read user locations from firebase.
     * @return array of UID and locations.
     */
    public static Map<String, Object> getLocations() {
        locations = groupData;
        locations.remove("Destination");
        return locations;
    }

    public void joinGroup(final String groupUID) {
        // TODO Currently in GroupDialog.java, consider moving here.
    }

    public void createGroup() {
        // TODO Currently in GroupDialog.java, consider moving here.
    }

    /**
     * Remove member information from firebase.
     * @return true if successful, false otherwise.
     */
    public boolean leaveGroup() {
        // TODO remove user's location field from firebase

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
     * Somehow notify other members.
     */
    public void deleteGroup() {
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
     * @return true if success, false otherwise.
     */
    public void sendMessage(String message) {

        if (message == null) {return;}

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Long lTimeStamp = System.currentTimeMillis();
        final String timeStamp = Long.toString(lTimeStamp);

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(timeStamp, "Test message.");

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

    public LeaderStateEnum getLeaderState() {
        return leaderState;
    }

    public LeaderStateEnum setLeaderState(LeaderStateEnum leaderState) {
        this.leaderState = leaderState;
        return this.leaderState;
    }

    public static boolean getIsGrouped() {
        return isGrouped;
    }

    public boolean setIsGrouped(boolean isGrouped) {
        this.isGrouped = isGrouped;
        return this.isGrouped;
    }
}
