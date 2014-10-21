package com.example.bogglegame;

import android.os.CountDownTimer;

public class Clock extends CountDownTimer{
	private Boggle boggle;
	
	/*
	 * set update target
	 */
	public void setView(Boggle boggle){
		this.boggle=boggle;
	}
	

	/*
	 * constructor
	 */
	public Clock(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
	}

	@Override
	/*
	 * time up
	 */
	public void onFinish() {
		boggle.updateTimer("Time's Up!");
		
		boggle.GameOver();
	}
	

	@Override
	/*
	 * every second update the timer on screen and update the remaining time
	 */
public void onTick(long millisUntilFinished) {
		
		String seconds;
		if((millisUntilFinished % 60000 / 1000)<10)
		{
			seconds="0"+String.valueOf(millisUntilFinished % 60000 / 1000);
		}
		else
			seconds =String.valueOf(millisUntilFinished % 60000 / 1000);
		
		boggle.updateTimer("Time :" + (millisUntilFinished / 60000)+":"+seconds);
		boggle.updateRemainingTime(millisUntilFinished);
	}
	

}
