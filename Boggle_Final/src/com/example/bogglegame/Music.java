package com.example.bogglegame;

import android.content.Context;
import android.media.MediaPlayer;

public class Music {
	   private MediaPlayer mp = null;
	   private boolean paused = false;
	   
	   public Music(Context context, int resource){
		   mp=MediaPlayer.create(context, resource);
		   mp.setLooping(false);
	   }
	   
	   /*
	    * if not in pause mode, play the audio effect
	    */
	   public void play(){
		   if(!paused)
			   mp.start();
	   }

	   /*
	    * set the music player in pause mode
	    */
	   public void pause(){
		   paused=true;
	   }
	   
	   /*
	    * resume music player from pause mode
	    */
	   public void resume(){
		   paused=false;
	   }

}
