package com.example.bogglegame;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

@SuppressLint("DefaultLocale")
public class BoggleGame extends Activity{

	//private static Trie trie;
	private static BoggleGame game;
	private static Boggle multigame;
	private char[] letters;
	public static int score;
	public static int receivedScore;
	private Map<String,Boolean> possibleWords;
	public List<String> triedWords;
	//private Map<String,DAWG> tries;
	public static List<String> list;
	public static List<String> firstUserWords;
	public static List<String> secondUserWords;
	public Context context;
	  public NavigableSet<String> dictionary;
	public enum TestResult{
		NOT_EXISTS,			/* word not in dictionary */
		HIT,				/* hit a valid entry in dictionary */
		ALREADY_TRIED		/* word already been scored */
	}
	
	/*
	 * get an instance of BoggleGame
	 * @return instance of BoggleGame
	 */
	
	public static BoggleGame GetInstance(){
		if(null==game){
			game=new BoggleGame();
		}
		return game;
	}
	
	BoggleGame(int i){
		letters = new char[16];
		possibleWords = new HashMap<String, Boolean>();
		//tries = new HashMap<String,DAWG>();
		triedWords = new ArrayList<String>();
		firstUserWords = new ArrayList<String>();
		secondUserWords = new ArrayList<String>();
	}
	
	private BoggleGame(){
		Log.d("BoggleGame", "constructor");
//		Log.d("file path",BoggleGame.class.);
		
		letters = new char[16];
		possibleWords = new HashMap<String, Boolean>();
		//tries = new HashMap<String,DAWG>();
		triedWords = new ArrayList<String>();
		firstUserWords = new ArrayList<String>();
		secondUserWords = new ArrayList<String>();
	}	
	/*
	 * Start a new boggle game
	 */
	public char[] NewGame(NavigableSet<String> dictionary){
		score = 0;
		receivedScore = 0;
		char[] Letters = { 'A', 'B', 'C','D','E','F',
	            'G','H','I','J','K','L',
	            'M','N','O','P','Q','R','S','T','U','V','W',
	            'X','Y','Z',
	            'A','E','I','O','U','A','E','I','O','U','A','E','I','O','U'};
		Log.d("testing", "step4");
		letters = new char[16];
		for(int i=0;i<16;i++){
			letters[i]=Letters[(int)(Math.random()*41)];
		}
		Log.d("testing", "step5");
		char[][] board = { {letters[0], letters[1], letters[2], letters[3] },
		                             {letters[4], letters[5], letters[6], letters[7] },
		                             {letters[8], letters[9], letters[10], letters[11] },
		                             {letters[12], letters[13], letters[14], letters[15] },
		                          };
		BoggleSolver BoggleSolver= new BoggleSolver(dictionary);
		list = BoggleSolver.boggleSolve(board);
	    int count =0;
		for (String str :  list) 
		{
			Log.i ("Strings", "" + str);
			count++;
		}
		//Log.i ("info", "" + letters[i]);
		System.out.println(count);	
		System.out.println();
		possibleWords.clear();
		//	computePossibleWords();		
		return letters;
	}
	
	public void NewMultiGame(NavigableSet<String> dictionary, char[] mywords){
		score = 0;
		letters = new char[16];
		for(int i=0;i<16;i++){
			letters[i]= mywords[i];
		}
		char[][] board = { {letters[0], letters[1], letters[2], letters[3] },
		                             {letters[4], letters[5], letters[6], letters[7] },
		                             {letters[8], letters[9], letters[10], letters[11] },
		                             {letters[12], letters[13], letters[14], letters[15] },
		                          };
		BoggleSolver BoggleSolver= new BoggleSolver(dictionary);
		
		list = BoggleSolver.boggleSolve(board);
	    int count =0;
		     for (String str :  list) 
		     {
		              Log.i ("Strings", "" + str);
		              count++;
		          }
		     //Log.i ("info", "" + letters[i]);
		     System.out.println(count);
			
		System.out.println();
		possibleWords.clear();
	//	computePossibleWords();		
	}
	
	public List<String> GetWords()
	{
		return list;
	}
	
	public List<String> getFirstUserWord() {
		return firstUserWords;
	}
	public List<String> getSecondUserWord() {
		return secondUserWords;
	}
	
	/*
	 * get standard solution and user's answer history in this round
	 * @return answer history and the standard solution
	 */
	public Map<String,Boolean> GetAnswerHistory(){
		return possibleWords;
	}

	/*
	 * get letters in the 4 by 4 grid
	 * @return letters
	 */
	public char[] GetLetters(){
		return letters;
	}	
	
	public GameStatus GetStatus(){
		GameStatus stat = new GameStatus();
		stat.words=new ArrayList<String>();
	//	stat.map=this.possibleWords;
		stat.letters=this.letters;
		stat.score=this.score;
		return stat;
	}
	
	/*
	 * resume the game by supplying game status
	 */
	public void Resume(GameStatus stat){
		
		this.letters=stat.letters;
		this.score=stat.score;
		this.possibleWords=new HashMap<String,Boolean>();
//		this.possibleWords=stat.map;
		
	}
	public int getwordscore(String word)
	{
		int wordscore = 0;
		switch(word.length()){
		case 3: 
			wordscore=1;
			break;
		case 4: 
			wordscore=1;
			break;
		case 5: 
			wordscore=2;
			break;
		case 6: 
			wordscore=3;
			break;
		case 7: 
			wordscore=5;
			break;
		default:
			wordscore=11;
		}

			
		return wordscore;
	}
	
	/*
	 * Test whether word is in dictionary and not being tried
	 * at the mean time also computing the score
	 * @word the word to be test
	 * @return TestResult
	 */
	public TestResult Test(String word){
		String prefix = Integer.toString(word.length())+word.charAt(0);
		Log.v("Boggle GAME","test prefix" +prefix.toLowerCase());
		List<String> trie= list;
	//	DAWG trie=tries.get(prefix.toLowerCase());
		
		if(trie==null){
			return TestResult.NOT_EXISTS;
		}
		else{
			if(trie.contains(word))
			{
				System.out.println(word);
				if(triedWords.contains(word)){
					return TestResult.ALREADY_TRIED;
				}
				triedWords.add(word);
				Log.d("first User","first user update word");
				firstUserWords.add(word);
				score += getwordscore(word);
				return TestResult.HIT;
			}
			return TestResult.NOT_EXISTS;
			
		}
	}
	public void updateSecondUserHit (String word, int mode){
		if (mode == 2)
			triedWords.add(word);
		secondUserWords.add(word);
		receivedScore += getwordscore(word);
	}
	
	/*
	 * get user's score
	 * return score
	 */
	public int GetScore(){
		return score;
	}
	
	public int getSecondScore() {
		return receivedScore;
	}
}
