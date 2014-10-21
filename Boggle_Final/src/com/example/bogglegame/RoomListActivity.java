package com.example.bogglegame;

import java.util.HashMap;
import java.util.Hashtable;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.shephertz.app42.gaming.multiplayer.client.WarpClient;
import com.shephertz.app42.gaming.multiplayer.client.command.WarpResponseResultCode;
import com.shephertz.app42.gaming.multiplayer.client.events.AllRoomsEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.AllUsersEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.LiveUserInfoEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.MatchedRoomsEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.RoomData;
import com.shephertz.app42.gaming.multiplayer.client.events.RoomEvent;
import com.shephertz.app42.gaming.multiplayer.client.listener.ZoneRequestListener;


public class RoomListActivity extends Activity implements ZoneRequestListener {
	
	private WarpClient theClient;
	private RoomListAdapter roomlistAdapter;
	private ListView listView;
	private ProgressDialog progressDialog = null;
	private int roomNumber = 0;
	private int myMode = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Debugging", "Start room list");
		setContentView(R.layout.room_list);
		listView = (ListView)findViewById(R.id.roomList);
		roomlistAdapter = new RoomListAdapter(this);
		Intent myintent = getIntent();
		myMode = myintent.getIntExtra("mode", 0); 
		init();
	}
	private void init(){
        try {
            theClient = WarpClient.getInstance();
        } catch (Exception ex) {
        	Utils.showToastAlert(this, "Exception in Initilization");
        }
    }
	
	public void onStart(){
		super.onStart();
		theClient.addZoneRequestListener(this);
		theClient.getRoomInRange(1, 1);// trying to get room with at least one user
	}
	
	public int getRoomNumber () {
		return roomNumber;
	}
	
	public void onStop(){
		super.onStop();
		theClient.removeZoneRequestListener(this);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		theClient.disconnect();
	}
	
	public void joinRoom(String roomId){
		if(roomId!=null && roomId.length()>0){
			goToGameScreen(roomId);
		}else{
			Log.d("joinRoom", "failed:"+roomId);
		}
	}
	
	public void onJoinNewRoomClicked(View view){
		Log.d("Debugging", "Create a room");
		progressDialog = ProgressDialog.show(this,"","Pleaes wait...");
		progressDialog.setCancelable(true);
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put("topLeft", "");
		properties.put("topRight", "");
		properties.put("bottomLeft", "");
		properties.put("bottomRight", "");
		//theClient.createRoom(""+System.currentTimeMillis(), "Saurav", 4, properties);
		theClient.createRoom(""+System.currentTimeMillis(), Utils.userName, 4, properties);
	}
	
	@Override
	public void onCreateRoomDone(final RoomEvent event) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(progressDialog!=null){
					progressDialog.dismiss();
					progressDialog = null;
				}
				if(event.getResult()==WarpResponseResultCode.SUCCESS){// if room created successfully
					String roomId = event.getData().getId();
					Log.d("Debugging", "roomid = " + roomId);
					joinRoom(roomId);
					Log.d("onCreateRoomDone", event.getResult()+" "+roomId);
				}else{
					Utils.showToastAlert(RoomListActivity.this, "Room creation failed...");
				}
			}
		});
	}
	
	@Override
	public void onDeleteRoomDone(RoomEvent event) {
		
	}
	
	@Override
	public void onGetAllRoomsDone(AllRoomsEvent event) {
		
	}
	
	@Override
	public void onGetLiveUserInfoDone(LiveUserInfoEvent event) {
		
	}
	
	@Override
	public void onGetMatchedRoomsDone(final MatchedRoomsEvent event) {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Log.d("testing", Utils.userName);
				RoomData[] roomDataList = event.getRoomsData();
				Log.d("Debugging","roomDataList\n" + roomDataList.length);
				roomNumber = roomDataList.length;
				if(roomDataList!=null && roomDataList.length>0){
					Log.d("Debugging","check list");
					roomlistAdapter.setData(roomDataList);
					listView.setAdapter(roomlistAdapter);
				}else{
					roomlistAdapter.clear();
				}
			}
		});
	}
	
	@Override
	public void onGetOnlineUsersDone(AllUsersEvent arg0) {
		
	}
	
	@Override
	public void onSetCustomUserDataDone(LiveUserInfoEvent arg0) {
		
	}
	
	private void goToGameScreen(String roomId){
		Intent intent = new Intent(this, PrepareMultiplayerActivity.class);
		intent.putExtra("mode", myMode);
		intent.putExtra("roomId", roomId);
		intent.putExtra("order", roomNumber);
		startActivity(intent);
	}
	
}
