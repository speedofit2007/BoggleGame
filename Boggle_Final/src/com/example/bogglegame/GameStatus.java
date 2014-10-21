package com.example.bogglegame;

import java.io.Serializable;
import java.util.ArrayList;

public class GameStatus implements Serializable {
	private static final long serialVersionUID = 2087571681424799940L;

	public ArrayList<String> words;
	public int score;
	public long remainingTime;
	public char[] letters;

}