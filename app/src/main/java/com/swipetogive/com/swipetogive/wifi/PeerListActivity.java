package com.swipetogive.com.swipetogive.wifi;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

public class PeerListActivity extends ListActivity
{
    stableArrayAdapter adapter;
    ListView lstView;
    ArrayList<String> peerArrayList = new ArrayList<String>();

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    private final IntentFilter intentFilter = new IntentFilter();
    private ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                showLoadingDialog();
            }

            @Override
            public void onFailure(int reasonCode) {
            }
        });

        lstView = getListView();
        lstView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lstView.setTextFilterEnabled(true);
        adapter = new stableArrayAdapter(this,android.R.layout.simple_list_item_1, peerArrayList);
        setListAdapter(adapter);
    }
    class stableArrayAdapter extends ArrayAdapter<String> {


        public stableArrayAdapter(Context context, int textViewResourceId, ArrayList<String> peerArrayList)
        {
            super(context, textViewResourceId, peerArrayList);

        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return peerArrayList.size();
        }

        @Override
        public String getItem(int position) {
            // TODO Auto-generated method stub
            return peerArrayList.get(position);
        }

        @Override
        public int getPosition(String item) {
            // TODO Auto-generated method stub
            return super.getPosition(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            // Out with the old, in with the new.
            peerArrayList.clear();

            for(int i = 0; i < peerList.getDeviceList().size(); i++) {
                peerArrayList.add("");
                WifiP2pDevice peer = (WifiP2pDevice) peerList.getDeviceList().toArray()[i];
                peerArrayList.set(i,peer.deviceName);

                Log.d("Peer" + i, "Status:" + peer.status + ", Name: " + peer.deviceName);
            }
            dismissLoadingDialog();
            adapter.notifyDataSetChanged();
            Log.d("Liste", peerArrayList.size() + "");
        }
    };

    public void showLoadingDialog() {

        if (progress == null) {
            progress = new ProgressDialog(this);
            progress.setTitle("Loading");
            progress.setMessage("Please wait...");
        }
        progress.show();
    }

    public void dismissLoadingDialog() {

        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }

    // register the broadcast receiver with the intent values to be matched
    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this, peerListListener);
        registerReceiver(mReceiver, intentFilter);
    }

    // unregister the broadcast receiver
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}