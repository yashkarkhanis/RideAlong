package com.junjunguo.pocketmaps.group;

import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.junjunguo.pocketmaps.R;

import java.util.HashMap;
import java.util.Map;

public class GroupHandler {

    private final static String TAG = "GROUP_HANDLER";

    private boolean isGrouped = false;
    private enum LeaderStateEnum{
        LEADER,
        NOT_LEADER,
        NOT_GROUPED,
    }
    private LeaderStateEnum leaderState = LeaderStateEnum.NOT_GROUPED;
    private String lastMessage = null;

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
        return true;
    }

    /**
     * Delete all information regarding group from firebase.
     * Somehow notify other members.
     * @return true if success, false otherwise.
     */
    public boolean deleteGroup() {
        return true;
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
    public boolean sendMessage(String message) {
        return true;
    }

    public LeaderStateEnum getLeaderState() {
        return leaderState;
    }

    public LeaderStateEnum setLeaderState(LeaderStateEnum leaderState) {
        this.leaderState = leaderState;
        return this.leaderState;
    }

    public boolean getIsGrouped() {
        return isGrouped;
    }

    public boolean setIsGrouped(boolean isGrouped) {
        this.isGrouped = isGrouped;
        return this.isGrouped;
    }
}
