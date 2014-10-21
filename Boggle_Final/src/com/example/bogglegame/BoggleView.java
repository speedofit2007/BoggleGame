package com.example.bogglegame;

import com.example.bogglegame.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;

@SuppressLint("WrongCall")
public class BoggleView extends View {

	private String TAG = "BoggleView";

	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int CLICK = 2;
	private int mode = NONE;


	private static final int PAUSE = 1;
	// ---------------------------------------
	// GUI NOTES:
	// SCORE and TIME - start at top no matter what
	// - may be to the left of dice
	//
	// WORD DISPLAY - starts below score and time
	// BOARD - below word display in vertical
	// - right in display if horizontal
	// QUIT and PAUSE - below board in vertical
	// - Below word if horizontal
	// ---------------------------------------

	private static final int ID = 42;
	private Boggle game;

	private float diceWidth;
	private float diceHeight;
	private float dicePadding;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	public BoggleView(Context context) {
		super(context);
		createBoggleView(context);
	}

	public BoggleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		createBoggleView(context);
	}

	public BoggleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		createBoggleView(context);
	}

	private void createBoggleView(Context context) {
		game =  (Boggle) context;
		setId(ID);
		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	// -------------------------------------------------------------------------
	// Override methods
	// -------------------------------------------------------------------------
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.d("on size changed",
				Integer.toString(w) + " " + Integer.toString(h) + " "
						+ Integer.toString(oldw) + " " + Integer.toString(oldh)
						+ " ");
		if (w > h) {
			diceWidth = diceHeight = h / (Boggle.DICE_HEIGHT + 1f);
			LayoutParams params = new LayoutParams(h, h);
			setLayoutParams(params);
			h = w;
		} else {
			diceWidth = diceHeight = w / (Boggle.DICE_WIDTH + 1f);
			LayoutParams params = new LayoutParams(w, w);
			setLayoutParams(params);
			w = h;
		}

		dicePadding = diceWidth / (Math.max(Boggle.DICE_WIDTH, Boggle.DICE_HEIGHT) + 1);
		Log.d("diceWidth", Float.toString(diceWidth));
		Log.d("diceHeight", Float.toString(diceHeight));
		Log.d("dicePadding", Float.toString(dicePadding));
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public void onDraw(Canvas canvas) {
		drawBackground(canvas);
		Log.d(TAG + "ondraw", "ondraw");
		drawTiles(canvas);
	}

	private void drawBackground(Canvas canvas) {
		Paint background = new Paint();
		background.setColor(getResources().getColor(R.color.background));
		canvas.drawRect(0, 0, getWidth(), getHeight(), background);
	}

	private void drawTiles(Canvas canvas) {
		super.onDraw(canvas);
		Log.d(TAG, "drawtiles");
		Rect imageBounds = new Rect();

		for (int x = 0; x < Boggle.DICE_WIDTH; x++) {
			for (int y = 0; y < Boggle.DICE_HEIGHT; y++) {
				getRect(x, y, imageBounds);

				game.getTile(x, y).draw(this, canvas, imageBounds, diceHeight,
						diceWidth);
			}
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (game.getGameStatus() == PAUSE) {
			Log.d("Status", "Status = PAUSE");
			return false;	
		}
		int x = (int) (event.getX() / (diceWidth + dicePadding));
		int y = (int) (event.getY() / (diceHeight + dicePadding));
		x = Math.min(Math.max(0, x), Boggle.DICE_WIDTH - 1);
		y = Math.min(Math.max(0, y), Boggle.DICE_HEIGHT - 1);

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mode = DRAG;
			Log.d("actiondown", "mode=drag");
			if (isSelectInPadding(event) != false) {
				game.playSound();
				select(x, y);
			} 
			break;
		case MotionEvent.ACTION_UP:
			game.setDefaultXY();	
		case MotionEvent.ACTION_POINTER_UP:
			// TODO: TEST WORD
			if(mode == DRAG) {
				mode = NONE;
				guess();
				Log.d("mode", "mode=none");
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (isSelectInPadding(event) == false) break;
			if (mode == DRAG) {
				game.playSound();
				select(x, y);
			}
			break;
		}
		return true;
	}

	boolean isSelectInPadding(MotionEvent e) {
		float max = (dicePadding * 4) + (diceWidth * 4);
		float changeMin = 0;
		float changeMax = 0;
		if (e.getX() < dicePadding || e.getY() < dicePadding || e.getX() > max || e.getY() > max)
			return false;
		changeMin = dicePadding + diceWidth;
		changeMax = changeMin + dicePadding;
		if ((e.getX() > changeMin && e.getX() < changeMax) || (e.getY() > changeMin && e.getY() < changeMax))
			return false;
		changeMin = changeMax + diceWidth;
		changeMax = changeMin + dicePadding;
		if ((e.getX() > changeMin && e.getX() < changeMax) || (e.getY() > changeMin && e.getY() < changeMax))
			return false;
		changeMin = changeMax + diceWidth;
		changeMax = changeMin + dicePadding;
		if ((e.getX() > changeMin && e.getX() < changeMax) || (e.getY() > changeMin && e.getY() < changeMax))
			return false;
		return true; 
	}
	
	// -------------------------------------------------------------------------
	// Helper methods
	// -------------------------------------------------------------------------
	private void guess() {
		game.makeAGuess();
		invalidate();
	}

	private void select(int x, int y) {
		game.selectTile(x, y);
		Rect selectedArea = new Rect();
		getRect(x, y, selectedArea);
		invalidate(selectedArea);
	}

	private void getRect(int x, int y, Rect rect) {
		float paddingX = (x + 1) * dicePadding;
		float paddingY = (y + 1) * dicePadding;
		float left = paddingX + x * diceWidth;
		float top = paddingY + y * diceHeight;

		rect.set((int) (left), (int) (top), (int) (left + diceWidth),
				(int) (top + diceHeight));
	}
}
