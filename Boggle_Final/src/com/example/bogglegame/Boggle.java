package com.example.bogglegame;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Random;
import java.util.TreeSet;

import org.andengine.engine.Engine.EngineLock;
import org.andengine.engine.options.EngineOptions;
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

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class Boggle extends Activity implements OnClickListener, IOnSceneTouchListener, RoomRequestListener, NotifyListener {

	private static final String TAG = "Boggle";
	   public NavigableSet<String> dictionary;
	// dimensions of tiles on the board
	public static final int DICE_WIDTH = 4;
	public static final int DICE_HEIGHT = 4;
	private static final int ONGOING = 0;
	private static final int PAUSE = 1;
	private static int STATUS;
	private int GAME_MODE = 0;
	private static String SCORE_BASE = "Score: ";
	private static String SECOND_SCORE_BASE = "Opponent's Score: ";

	private Tile[] tiles;
	private ArrayList<Tile> selectedTiles;
	private String selectedWords;
	private long remainingTime;
	private int tileAmount = 16;
	private int oldx = -1;
	private int oldy = -1;

	//private TextView playerName;
	private TextView boggleTimerView;
	private TextView boggleScoreView;
	private TextView boggleScoreView2;
	private TextView displaywordsview;
	private TextView displaywordsview2;
	private ScrollView WordsScroll2;
	private Button quitButton, resetButton;

	private Music music;
	private BoggleGame game;
	private Clock timer;

	private WarpClient theClient;
	private HashMap<String, User> userMap = new HashMap<String, User>();
	private String roomId = "";
	public static String receivedWord = "";
	private int receiveScore = 0;
	private char[] myboard;
	
	public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;
	/*
	 * initialize the game read wordlist from resource initialize none UI
	 * parameters
	 */
	private void initialize() {
		Log.d(TAG, "Boggle game initializeing");

//	 InputStream is = getResources().openRawResource(R.raw.a11);
		Log.d(TAG, "got inputstream");
		game = BoggleGame.GetInstance();

		tiles = new Tile[tileAmount];
		selectedTiles = new ArrayList<Tile>();
		remainingTime = 180000;
		selectedWords = "";
		music = new Music(this, R.raw.audio);
	}

	void setDefaultXY() {
		oldx = -1;
		oldy = -1;
	}
	
	int getGameStatus() {
		return STATUS;
	}
	
	int getSecondUserScore() {
		return receiveScore;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.boggle_main);
		solver();
		Log.d(TAG, "onCreate");
		Intent intent = getIntent();
		GAME_MODE = intent.getIntExtra("mode", 0);
		Log.d(TAG, "mode = " + GAME_MODE);
		initialize();
		myboard = new char[16];
		if(GAME_MODE != 0) {
			try{
				theClient = WarpClient.getInstance();
			}catch(Exception e){
				e.printStackTrace();
			}
			userMap.clear();
			roomId = intent.getStringExtra("roomId");
			myboard = intent.getCharArrayExtra("board");
			init(roomId);
		} else {
			Log.d(TAG, "" + getboard().length);
			char[] mySet = { 'A', 'B', 'C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W',
			            'X','Y','Z','A','E','I','O','U','A','E','I','O','U','A','E','I','O','U'};
			for(int i=0;i<16;i++){
				int random = (int) (Math.random()*41);
				myboard[i] = mySet[random];
			}
			printboard();
		}
		renderTiles();
		game.NewMultiGame(dictionary, getboard());
		
		STATUS = ONGOING;
		Log.d(TAG, "possible word amount " + game.GetAnswerHistory().size());

		// set view
		setTitle(R.string.boggle_name);
		setContentView(R.layout.boggle_game);

		boggleTimerView = (TextView) findViewById(R.id.boggle_time);

		timer = new Clock(remainingTime, 1000);
		timer.setView(this);
		timer.start();

		//playerName = (TextView) findViewById(R.id.textName);
		//playerName.setText("Welcome To Our Boogle Game, " + name);  
		boggleScoreView = (TextView) findViewById(R.id.boggle_score);
		boggleScoreView2 = (TextView) findViewById(R.id.boggle_score2);
		displaywordsview = (TextView) findViewById(R.id.ListView1);
		displaywordsview2 = (TextView) findViewById(R.id.ListView2);
		WordsScroll2=(ScrollView) findViewById(R.id.scrollView2);
		boggleScoreView.setText(SCORE_BASE + game.GetScore());

		
		quitButton = (Button) findViewById(R.id.boggle_quit_button);
		quitButton.setOnClickListener (new View.OnClickListener() {
		   public void onClick(View v) {

		    Intent mStartActivity = new Intent(getBaseContext(), MainActivity.class);
		    int mPendingIntentId = 123456;
		    PendingIntent mPendingIntent = PendingIntent.getActivity(getBaseContext(), mPendingIntentId,mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		    AlarmManager mgr =		(AlarmManager)getBaseContext().getSystemService(Context.ALARM_SERVICE);
		    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
		    System.exit(0);

		       finish();

		   }
		});
		
		
		resetButton = (Button) findViewById(R.id.reset);
		
		if (GAME_MODE == 1)
		{
			
			resetButton.setVisibility(View.GONE);
			displaywordsview2.setVisibility(View.INVISIBLE);
			WordsScroll2.setVisibility(View.INVISIBLE);
		}
		else
			if(GAME_MODE == 2)
		{
				resetButton.setVisibility(View.GONE);
		}
			else
			{
				boggleScoreView2.setVisibility(View.INVISIBLE);
				displaywordsview2.setVisibility(View.INVISIBLE);
				WordsScroll2.setVisibility(View.INVISIBLE);
			}
				
		resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Intent intent = getIntent();
            	overridePendingTransition(0, 0);
            	intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            	finish();
            	overridePendingTransition(0, 0);
            	startActivity(intent);
            }
        });
	}

	public char[] getboard(){
		return myboard;
	}
	public void setboard(char[] mywords){
		for(int i=0;i<16;i++)
			myboard[i] = mywords[i];
	}
	public void printboard(){
		for(int i=0;i<16;i++)
			Log.d("printing", ""+myboard[i]);
	}
	
	private void init(String roomId) {
		if(theClient!=null){
			theClient.addRoomRequestListener(this);
			theClient.addNotificationListener(this);
			theClient.joinRoom(roomId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	/*public void onClick(View v) {
		switch (v.getId()) {
		case R.id.boggle_quit_button:
			try {
				theClient.getInstance().disconnect();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(theClient!=null){
				handleLeave(Utils.userName);
				theClient.leaveRoom(roomId);
				theClient.unsubscribeRoom(roomId);
				theClient.removeRoomRequestListener(this);
				theClient.removeNotificationListener(this);
				//clearResources();
			}
			Intent singleScreen = new Intent(getApplicationContext(),  MainActivity.class);
	        startActivity(singleScreen);
		}
	}*/

	
	// /////////////////////////////////////////////////////
	// /functions allowed calling from outside
	// /////////////////////////////////////////////////////
	protected void selectTile(int x, int y) {
		if (oldx != x || oldy != y) {
			Tile t = getTile(x, y);
			selectedWords += t.getLetter();

			if (selectedTiles.contains(t)) {
				// if meet an intersection, regard the former selected letters
				// as a whole word
				makeAGuess();
				Log.d(TAG, "REMOVING TILE: " + x + ", " + y);
			} else {
				// otherwise, add the letter to current word
				selectedTiles.add(t);
				t.setSelected(true);
				Log.d(TAG, "ADDING TILE: " + x + ", " + y);
			}
		}

		oldx = x;
		oldy = y;
	}

	/** Return the tile at the given coordinates */
	protected Tile getTile(int x, int y) {
		return tiles[y * DICE_HEIGHT + x];
	}

	/*
	 * update the timer text
	 * 
	 * @str the string to render on timer
	 */
	protected void updateTimer(String str) {
		boggleTimerView.setText(str);
	}

	/*
	 * update the remaining time in this game
	 * 
	 * @millisUntilFinished the value to set for remainingTime
	 */
	protected void updateRemainingTime(long millisUntilFinished) {

		remainingTime = millisUntilFinished;
	//	Log.v(TAG, "update time " + Long.toString(remainingTime));
	}

	/*
	 * time up and game over clean up the screen for new game
	 */
	protected void GameOver() {

		for (String str :  game.GetWords()) 
        {
			if(!game.getFirstUserWord().contains(str))
				displaywordsview.append(str+"\n");
             
        }
		// in cutthroat mode
		if(GAME_MODE != 0) {
			// display first User 
			final Dialog dialog = new Dialog(this);
			dialog.setContentView(R.layout.popup);
			 Button btnCancel=(Button)dialog.findViewById(R.id.OK);
	      
	      btnCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
	
	        if (game.GetScore()<game.getSecondScore())
	        dialog.setTitle("You Lost");
	        else if
	         (game.GetScore()>game.getSecondScore())
		        dialog.setTitle("You Won");
	        else
	        dialog.setTitle("Game Tied");
	        
	        dialog.show();
	        /*
			Log.d("first user", "Score = " + game.GetScore());
			Log.d("first user", "Found Word =  -------------");
			for (String str: game.getFirstUserWord())
			
				Log.d("first user", str);
			
			// display second User	
			Log.d("second user", "Score = " + game.getSecondScore());
			Log.d("second user", "Found Word =  -------------");
			for (String str: game.getSecondUserWord())
				Log.d("second user", str);
				 */
		}
		
		STATUS = PAUSE;
		selectedTiles.clear();
	}

	/*
	 * make a boggle guess
	 */
	protected void makeAGuess() {
		Log.d(TAG, "test with word " + selectedWords);
		if(selectedWords.length() != 0) {
			BoggleGame.TestResult result = game.Test(selectedWords);
			if (BoggleGame.TestResult.HIT == result) {
				// update the score
				Log.d(TAG, "hit a word " + selectedWords);
				if (GAME_MODE > 0)
					sendUpdateEvent();
				displaywordsview.append(selectedWords+"-"+ game.getwordscore(selectedWords)+"\n");
				boggleScoreView.setText(SCORE_BASE + game.GetScore());
			}
			if (BoggleGame.TestResult.ALREADY_TRIED == result) {
	
				Log.d(TAG, "already tried with " + selectedWords);
				boggleScoreView.setText(SCORE_BASE + game.GetScore());
			}
			if (BoggleGame.TestResult.NOT_EXISTS == result) {
				Log.d(TAG, "not exists " + selectedWords);
				boggleScoreView.setText(SCORE_BASE + game.GetScore());
			}
		}
		clearSelection();
	}
	
	
	public void updateFoundWord (final String word) {
		Log.d("Chat", "updateFoundWord");
		runOnUiThread(new Runnable() {
			  public void run() {
				if(word.length() != 0) {
					receivedWord = word;
					Log.d("second player", "other hits a word " + receivedWord);
					game.updateSecondUserHit(receivedWord, GAME_MODE);
					Log.d("second player", "score = " + game.getSecondScore());
					boggleScoreView2.setText(SECOND_SCORE_BASE + game.getSecondScore());
					if (GAME_MODE == 2)
						displaywordsview2.append(receivedWord+"-"+ game.getwordscore(receivedWord)+"\n");
				}
			  }
			  
		});
	}

	/*
	 * audio effect
	 */
	protected void playSound() {
		music.play();
	}

	// /////////////////////////////////////////////////////
	// /Helper functions
	// /////////////////////////////////////////////////////
	/*

	/*
	 * read game status from internal storage
	 */

	/*
	 * render tiles
	 * 
	 * @words the list of characters to render on tiles
	 */
	private void renderTiles() {
		Log.d("testing","step renderTiles");
		for (int i = 0; i < myboard.length; i++) {
			tiles[i] = new Tile(myboard[i], false);
		}
	}

	/*
	 * clear words on tiles
	 */
//		findViewById(R.id.boggle_row_3).postInvalidate();
	

	/*
	 * resume the words on tiles
	 * 
	 * @letters the words to be render on tiles
	 */

	public List<Tile> getSelectedTiles() {
		return selectedTiles;
	}
	public NavigableSet<String> solver()
	{
		dictionary = new TreeSet<String>();
	    try {
	   // 	File file = new File("file://" + "com.example.bogglegame"+ "/" +R.raw.text);
	    
	    //	fr=this.getResources().openRawResource(R.raw.text);
	    //	fr = am.open("text.txt");
	    	
	    	InputStream fr= this.getResources().openRawResource(R.raw.text);
	    	InputStreamReader ir = new InputStreamReader(fr);
	        BufferedReader br = 
	        		new BufferedReader(ir);
	        String line;
	  
	        while ((line = br.readLine()) != null) 
	        {
	            dictionary.add(line.split(":")[0]);
	        }
	        br.close();
	        return dictionary;
	}    catch (Exception e) {
        throw new RuntimeException("Error while reading dictionary");
    }
	}

	/*
	 * clear the selection
	 */
	private void clearSelection() {
		for (int i = 0; i < tileAmount; i++) {
			tiles[i].setSelected(false);
		}
		selectedTiles.clear();
		selectedWords = "";
	}
	

	private HashMap<String, Object> properties;
	
	public void addMorePlayer(boolean isMine, String userName){
		if(userMap.get(userName)!=null){// if already in room
			return;
		}
		Log.d("userNameGame", userName);
		//char index =  userName.charAt(userName.length()-1);
		User user = new User(0, 0);
		userMap.put(userName, user);
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
			
			Intent mStartActivity = new Intent(getBaseContext(), MainActivity.class);
		    int mPendingIntentId = 123456;
		    PendingIntent mPendingIntent = PendingIntent.getActivity(getBaseContext(), mPendingIntentId,mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		    AlarmManager mgr =		(AlarmManager)getBaseContext().getSystemService(Context.ALARM_SERVICE);
		    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
		    System.exit(0);

		       finish();
			//clearResources();
		}
		super.onBackPressed();
	}
	void sendUpdateEvent(){
		try{
			Log.d("in game", "sending found word");
			theClient.sendChat("1" + selectedWords);
		}catch(Exception e){
			Log.d("sendUpdateEvent", e.getMessage());
		}
	}
	@Override
	public void onChatReceived(ChatEvent event) {
		String sender = event.getSender();
		if(sender.equals(Utils.userName)==false){// if not same user
			receivedWord = event.getMessage();
			Log.d(TAG,"receiving ............." + receivedWord);
			try{
				if (receivedWord.charAt(0) != '1')
					return;
				else
					receivedWord = receivedWord.substring(1);
				//Utils.showToastAlert(Boggle.this, sender +" has found word: " + receivedWord);
				updateFoundWord(receivedWord);
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
			//Utils.showToastOnUIThread(this, "onGetLiveRoomInfoDone: Failed "+event.getResult());
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
