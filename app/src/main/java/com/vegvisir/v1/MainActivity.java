package com.vegvisir.v1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vegvisir.VegvisirCore;
import com.vegvisir.core.datatype.proto.Block;
import com.vegvisir.core.reconciliation.ReconciliationV1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    VegvisirCore core;
    Thread coreThread;
    AndroidNetworkAdapter adapter;
    private long lastBlock = new Date().getTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Get Permission */
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        } else {
        }

        Log.i(TAG, "onCreate: Pass Permission");

        /* Create a network adapter for android */
        adapter = new AndroidNetworkAdapter(getApplicationContext(), "id"+this.hashCode());

        Log.i(TAG, "onCreate: Adpater Created!");

        /* Instantiate a block dag and run it */
        core = new VegvisirCore(adapter, ReconciliationV1.class, createBlock("Admin"));
        coreThread = new Thread(core);
        coreThread.start();

        Log.i(TAG, "onCreate: Core started!");


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
//            Log.d(TAG, "onCreate: Rendering Block list");
            runOnUiThread(this::updateBlockView);
        }, 0, 1, TimeUnit.SECONDS);

        initActions();

        Log.i(TAG, "onCreate: Action Initialized!");

        updateBanner();
    }

    /**
     * Update block list view if a change of blocks happened.
     */
    private void updateBlockView() {
        LinearLayout layout = findViewById(R.id.blockList);
        layout.removeAllViews();

        List<com.isaacsheff.charlotte.proto.Block> blockList = new ArrayList<>();
        blockList.addAll(core.getDag().getAllBlocks());
        Collections.sort(blockList, (a, b) -> Long.compare(a.getVegvisirBlock().getBlock().getTimestamp().getUtcTime(), b.getVegvisirBlock().getBlock().getTimestamp().getUtcTime()));
        for (int i = 0; i < blockList.size(); i++) {
            com.isaacsheff.charlotte.proto.Block b = blockList.get(i);
            String id = b.getVegvisirBlock().getBlock().getUserid();
            Date d = new Date(b.getVegvisirBlock().getBlock().getTimestamp().getUtcTime());
            TextView view = new TextView(layout.getContext());
            view.setText("[" + i + "] " + id + " : " + d.toString());
            layout.addView(view);

        }
    }

    private void updateBanner() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            String banner = !adapter.getConnectionHistory().isEmpty() ? adapter.getConnectionHistory().getLast() : "No Peer Available";
//            Log.i(TAG, "updateBanner: Banner updating with "+banner);
            runOnUiThread(() -> {
                TextView view = findViewById(R.id.banner);
                view.setText(banner);
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Add a new block to the block dag. This is a really simple block. It only contains a user id and a timestamp.
     */
    private void addBlock() {
        EditText view = findViewById(R.id.userid);
        if (view.getText().toString().equals(""))
            return;
        Log.i(TAG, "addBlock: Get into add block with user id "+ view.getText().toString());
        core.getDag().addBlock(createBlock(view.getText().toString()));
    }

    /**
     * Build a charlotte block having vegvisir block inside.
     * @param userid
     * @return
     */
    private com.isaacsheff.charlotte.proto.Block createBlock(String userid) {
        com.vegvisir.core.datatype.proto.Block block = com.vegvisir.core.datatype.proto.Block.newBuilder()
                .setBlock(Block.UserBlock.newBuilder().setTimestamp(com.vegvisir.common.datatype.proto.Timestamp.newBuilder().setUtcTime(new Date().getTime()).build())
                        .setUserid(userid)
                        .build())
                .build();
        return com.isaacsheff.charlotte.proto.Block.newBuilder().setVegvisirBlock(block).build();
    }

    /**
     * Set up buttons' actions.
     */
    private void initActions() {
        Button addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener((v) -> {
            addBlock();
        });

        findViewById(R.id.clearBtn).setOnClickListener((v) -> {
            EditText text = findViewById(R.id.userid);
            text.getText().clear();
        });
    }
}
