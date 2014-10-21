package com.example.bogglegame;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Random;

import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.json.JSONObject;

import com.shephertz.app42.gaming.multiplayer.client.WarpClient;
import com.shephertz.app42.gaming.multiplayer.client.command.WarpResponseResultCode;
import com.shephertz.app42.gaming.multiplayer.client.events.ChatEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.LiveRoomInfoEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.LobbyData;
import com.shephertz.app42.gaming.multiplayer.client.events.MoveEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.RoomData;
import com.shephertz.app42.gaming.multiplayer.client.events.RoomEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.UpdateEvent;
import com.shephertz.app42.gaming.multiplayer.client.listener.NotifyListener;
import com.shephertz.app42.gaming.multiplayer.client.listener.RoomRequestListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class PrepareMultiplayerActivity extends Activity implements OnClickListener, IOnSceneTouchListener, RoomRequestListener, NotifyListener {

	Button myButton;
	char[] letters;
	private String roomId = "";
	private String mywords = "";
	private WarpClient theClient;
	private HashMap<String, User> userMap = new HashMap<String, User>();
	private int myMode = 0;
	private int START = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		START = 0;
		Log.d("Prepare", "start prepare");
		setContentView(R.layout.activity_prepare_multiplayer);
		myButton = (Button)findViewById(R.id.mybutton);
		Intent intent = getIntent();
		myMode = intent.getIntExtra("mode", 0); 
		Log.d("PrepareMultiplayerActivity", "mode = " + myMode);
		roomId = intent.getStringExtra("roomId");
		
		Log.d("testing","stop at 1");
		char[] mySet = { 'A', 'B', 'C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W',
	            'X','Y','Z','A','E','I','O','U','A','E','I','O','U','A','E','I','O','U'};
		letters = new char[16];
		for(int i=0;i<16;i++){
			int random = (int) (Math.random()*41);
			letters[i] = mySet[random];
			mywords += mySet[random];
		}	
		Log.d("my words", mywords);
		try{
			theClient = WarpClient.getInstance();
		}catch(Exception e){
			e.printStackTrace();
		}
		init(roomId);
		myButton.setEnabled(false);
		myButton.setOnClickListener(new View.OnClickListener() {
			@Override
	        public void onClick(View view) {
				if (START == 0)
					sendUpdateEvent();
				Log.d("my words", mywords);
	           Intent multiScreen = new Intent(getApplicationContext(),  Boggle.class);
	           multiScreen.putExtra("roomId", roomId);
	           multiScreen.putExtra("board", letters);
	           multiScreen.putExtra("mode", myMode);
	           startActivity(multiScreen);
	        }
	    });	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.prepare_multiplayer, menu);
		return true;
	}
	
	private void init(String roomId) {
		if(theClient!=null){
			theClient.addRoomRequestListener(this);
			theClient.addNotificationListener(this);
			theClient.joinRoom(roomId);
		}
	}

	private HashMap<String, Object> properties;
	
	public void addMorePlayer(boolean isMine, String userName){
		if(userMap.get(userName)!=null){// if already in room
			return;
		}
		Log.d("userNameGame", userName);
		Log.d("user map size", "" + userMap.size());
		//char index =  userName.charAt(userName.length()-1);
		User user = new User(0, 0);
		userMap.put(userName, user);
		if(userMap.size() == 2){
			runOnUiThread(new Runnable() {
			     @Override
			     public void run() {
			    	myButton.setText("Start"); 
			    	myButton.setEnabled(true);
			    }
			});
		}
	}
	public void handleLeave(String name) {
		if(name.length()>0 && userMap.get(name)!=null){
			userMap.remove(name);
		}
	}
	
	@Override
	public void onBackPressed() {
		if(theClient!=null){
			handleLeave(Utils.userName);
			theClient.leaveRoom(roomId);
			theClient.unsubscribeRoom(roomId);
			theClient.removeRoomRequestListener(this);
			theClient.removeNotificationListener(this);
			//clearResources();
		}
		super.onBackPressed();
	}
	
	void sendUpdateEvent(){
		try{
			Log.d("Chat","sending....");
			theClient.sendChat("0"+ mywords.toString());
		}catch(Exception e){
			Log.d("sendUpdateEvent", e.getMessage());
		}
	}
	
	public void onChatReceived(final ChatEvent event) {
		String sender = event.getSender();
		if(sender.equals(Utils.userName)==false){// if not same user
			try{
				Log.d("Chat","receiving....");
				mywords = event.getMessage();
				Log.d("Chat","word = " + mywords);
				if (mywords.charAt(0) == '0') {
					mywords = mywords.substring(1);
					letters = new char[16];
					letters = mywords.toCharArray();
					Log.d("my change words",mywords);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (START == 0) {
								START = 1;
								myButton.performClick();
							}
						}
					});	
				}
				if (mywords.charAt(0) == '1') {
					mywords = mywords.substring(1);
					runOnUiThread(new Runnable() {
						  public void run() {
							  if (myMode == 1)
								  mywords = ".";
							  Toast.makeText(getApplicationContext(), "The second user found a word " + mywords, Toast.LENGTH_LONG).show();
						  }
						});
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onGameStarted(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGameStopped(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMoveCompleted(MoveEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPrivateChatReceived(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRoomCreated(RoomData arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRoomDestroyed(RoomData arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdatePeersReceived(UpdateEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUserChangeRoomProperty(RoomData roomData, String userName,
			HashMap<String, Object> tableProperties, HashMap<String, String> lockProperties) {
		
	}

	@Override
	public void onUserJoinedLobby(LobbyData arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUserJoinedRoom(RoomData roomData, String name) {
		// TODO Auto-generated method stub
		addMorePlayer(true, name);
		
	}

	@Override
	public void onUserLeftLobby(LobbyData arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUserLeftRoom(RoomData roomData, String name) {
		// TODO Auto-generated method stub
		handleLeave(name);
		
	}

	@Override
	public void onUserPaused(String arg0, boolean arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUserResumed(String arg0, boolean arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetLiveRoomInfoDone(LiveRoomInfoEvent event) {
		// TODO Auto-generated method stub
		if(event.getResult()==WarpResponseResultCode.SUCCESS){
			String[] joinedUser = event.getJoinedUsers();
			if(joinedUser!=null){
				for(int i=0;i<joinedUser.length;i++){
					if(joinedUser[i].equals(Utils.userName)){
						addMorePlayer(true, joinedUser[i]);
					}else{
						addMorePlayer(false, joinedUser[i]);
					}
				}
			}
			properties = event.getProperties();
			for (Map.Entry<String, Object> entry : properties.entrySet()) { 
	            if(entry.getValue().toString().length()>0){
					int fruitId = Integer.parseInt(entry.getValue().toString());
					//placeObject(fruitId, entry.getKey(), null, false);
				}
	        }
		}else{
			//.showToastOnUIThread(this, "onGetLiveRoomInfoDone: Failed "+event.getResult());
		}
	}

	@Override
	public void onJoinRoomDone(RoomEvent event) {
		// TODO Auto-generated method stub
		if(event.getResult()==WarpResponseResultCode.SUCCESS){
			theClient.subscribeRoom(roomId);
		}else{
			//Utils.showToastOnUIThread(this, "onJoinRoomDone: Failed "+event.getResult());
		}
	}

	@Override
	public void onLeaveRoomDone(RoomEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLockPropertiesDone(byte arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSetCustomRoomDataDone(LiveRoomInfoEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSubscribeRoomDone(RoomEvent event) {
		// TODO Auto-generated method stub
		if(event.getResult()==WarpResponseResultCode.SUCCESS){
			theClient.getLiveRoomInfo(roomId);
		}else{
			//Utils.showToastOnUIThread(this, "onSubscribeRoomDone: Failed "+event.getResult());
		}
	}

	@Override
	public void onUnSubscribeRoomDone(RoomEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnlockPropertiesDone(byte arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdatePropertyDone(LiveRoomInfoEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSceneTouchEvent(Scene arg0, TouchEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
