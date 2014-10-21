package com.example.bogglegame;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    TextView name;
    Button SingleBtn;
    Button CutthroatTwoPlayerBtn;
    Button BasicTwoPlayerBtn;
    Button Help;
  
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		
	}
	private Activity mCurrentActivity = null;
    public Activity getCurrentActivity(){
          return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
          this.mCurrentActivity = mCurrentActivity;
    }
    
    public void onBackPressed() {
        // TODO Auto-generated method stub
    	  
    	 
    	 finish();
    	 finish();
    	 finish();
    	 finish();
    	System.exit(0);
    	onBackPressed();
    	
    	
    	 
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		SingleBtn = (Button)findViewById(R.id.single_player_button);
		BasicTwoPlayerBtn = (Button)findViewById(R.id.basic_two_player_button);
		CutthroatTwoPlayerBtn = (Button)findViewById(R.id.cutthroat_two_player_button);
		Help = (Button) findViewById(R.id.Help);
		final Dialog dialog = new Dialog(this);
		
        Help.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			
			
				dialog.setContentView(R.layout.help);
				 Button btnCancel=(Button)dialog.findViewById(R.id.OK);
				   dialog.setTitle("Help");
		      btnCancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
		      dialog.show();
         		
			}        
        });

        SingleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Intent singleScreen = new Intent(getApplicationContext(),  Boggle.class);
            	singleScreen.putExtra("mode", 0);
 	           	startActivity(singleScreen);
            }
        });
        BasicTwoPlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent multiScreen = new Intent(getApplicationContext(),  MultiplayerActivity.class); 
                multiScreen.putExtra("mode", 1);
                startActivity(multiScreen);
            }
        });
        
        CutthroatTwoPlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent multiScreen = new Intent(getApplicationContext(),  MultiplayerActivity.class);
                multiScreen.putExtra("mode", 2);
                startActivity(multiScreen);
            }
        });
        
		return true;
	}

}
