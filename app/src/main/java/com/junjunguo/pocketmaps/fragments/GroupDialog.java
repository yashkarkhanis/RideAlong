package com.junjunguo.pocketmaps.fragments;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.activities.ui.login.LoginActivity;

public class GroupDialog {
    private static final String TAG = "GROUP";
    private Activity activity;
    private ViewGroup groupCreateVP, groupJoinVP;

    public GroupDialog (Activity activity) {
        this.activity = activity;
        groupCreateVP = (ViewGroup) activity.findViewById(R.id.group_create_layout);
    }

    public void showGroupCreate(final ViewGroup calledFromVP){
        initCancelBtn(groupCreateVP, calledFromVP);
        groupCreateVP.setVisibility(View.VISIBLE);
    }

    /**
     * init clear btn
     */
    private void initCancelBtn(final ViewGroup groupCreateVP, final ViewGroup calledFromVP) {
        Button groupCreateCancelBtn = (Button) activity.findViewById(R.id.group_create_cancel);
        groupCreateCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                //groupCreateVP.setVisibility(View.INVISIBLE);
                //calledFromVP.setVisibility(View.VISIBLE);
            }
        });
    }
}
