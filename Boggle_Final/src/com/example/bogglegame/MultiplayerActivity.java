package com.example.bogglegame;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.shephertz.app42.gaming.multiplayer.client.WarpClient;
import com.shephertz.app42.gaming.multiplayer.client.command.WarpResponseResultCode;
import com.shephertz.app42.gaming.multiplayer.client.events.ConnectEvent;
import com.shephertz.app42.gaming.multiplayer.client.listener.ConnectionRequestListener;

public class MultiplayerActivity extends Activity implements ConnectionRequestListener {

	private WarpClient theClient;
	private EditText nameEditText;
    private ProgressDialog progressDialog;
    private boolean isConnected = false;
	private int myMode = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multi_activity_main);
		nameEditText = (EditText)findViewById(R.id.nameEditText);
		Intent myintent = getIntent();
		myMode = myintent.getIntExtra("mode", 0); 
		Log.d("MultiplayerAcitivity", "mode = " + myMode);
		init();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		theClient.removeConnectionRequestListener(this);
		if(theClient!=null && isConnected){
			theClient.disconnect();
		}
		
	}
	
	public void onPlayGameClicked(View view){
		if(nameEditText.getText().length()==0){
			Utils.showToastAlert(this, getApplicationContext().getString(R.string.enterName));
			return;
		}

		String userName = "" + nameEditText.getText();
		Utils.userName = userName;
		Log.d("Name to Join", "" + userName);
		theClient.connectWithUserName(userName);
		progressDialog =  ProgressDialog.show(this, "", "connecting to appwarp");
	}
	
	private void init(){
		if (myMode == 1)
			WarpClient.initialize(Constants.basicApiKey, Constants.basicSecretKey);
		if (myMode == 2)
			WarpClient.initialize(Constants.cutthroatApiKey, Constants.cutthroatSecretKey);
        
		try {
            theClient = WarpClient.getInstance();
        } catch (Exception ex) {
        	Utils.showToastAlert(this, "Exception in Initilization");
        }
        theClient.addConnectionRequestListener(this); 
    }
	
	@Override
	public void onConnectDone(final ConnectEvent event) {
		Log.d("onConnectDone", event.getResult()+"");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(progressDialog!=null){
					progressDialog.dismiss();
					progressDialog=null;
				}
			}
		});
		if(event.getResult() == WarpResponseResultCode.SUCCESS){// go to room  list 
			isConnected = true;
			Intent intent = new Intent(MultiplayerActivity.this, RoomListActivity.class);
			intent.putExtra("mode", myMode);
			startActivity(intent);
		}else{
			isConnected = false;
			Utils.showToastOnUIThread(MultiplayerActivity.this, "connection failed");
		}
	}
	
	@Override
	public void onDisconnectDone(final ConnectEvent event) {
		Log.d("onDisconnectDone", event.getResult()+"");
		isConnected = false;
	}

    @Override
	public void onInitUDPDone(byte arg0) {
		
	}
    
}
