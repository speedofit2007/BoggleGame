package com.example.bogglegame;


import android.app.Activity;


import java.util.*;



public class BoggleSolver extends Activity{

    public static NavigableSet<String> dictionary;
    public static int imain;
    public static int jmain;
    public static int ivisited;
    public static int jvisited;
    public static  ArrayList<Integer> ivar;
    public static ArrayList<Integer> jvar;
   // private Map<Character, List<Character>> graph = new HashMap<Character, List<Character>>();
    
    public BoggleSolver(NavigableSet<String> dictionary) {
    	BoggleSolver.dictionary=dictionary;
    }

	public static List<String> boggleSolve(char[][] m) {
	    if (m == null) {
	        throw new NullPointerException("The matrix cannot be null");
	    }
	     List<String> validWords = new ArrayList<String>();
	
	    for (int i = 0; i < m.length; i++) {
	        for (int j = 0; j < m[0].length; j++) {
	        	 imain=i;
	             jmain=j;
	             ivar= new ArrayList<Integer>();
	             jvar= new ArrayList<Integer>();
	            solve(m, i, j, m[i][j] + "", validWords);  
	        }
	    }
	    
	    LinkedHashSet<String> listToSet = new LinkedHashSet<String>(validWords);
	    List<String> listWithoutDuplicates = new ArrayList<String>(listToSet);
	    
	
	    return listWithoutDuplicates;
	
	}

	private static void solve(char[][] m, int i, int j, String prefix, List<String> validWords) {
	    assert m != null;
	    assert validWords != null;
	   
	 // Iterate through the neighbours of tile (i, j)
	    for (int i1 = Math.max(0, i - 1); i1 < Math.min(m.length, i + 2); i1++) 
	    {
	        for (int j1 = Math.max(0, j - 1); j1 < Math.min(m[0].length, j + 2); j1++)
	        {
	        	
	            // Skip the tile (i, j) itself
	
	            if ((i1 == imain && j1 == jmain) || (i1==i && j1==j) || (ivar.contains(i1) && jvar.contains(j1)) ) continue;
	
	            
	            String word = prefix+ m[i1][j1];
	        	ivar.add(i1);
	         	jvar.add(j1);
	            if (!dictionary.subSet(word, word + Character.MAX_VALUE).isEmpty()) {
	
	            			if (dictionary.contains(word))
	            			{ 	
	                    validWords.add(word);
	                }
	                solve(m, i1, j1, word, validWords);
	            }                
	            ivar.remove(ivar.indexOf(i1));
	            jvar.remove(jvar.indexOf(j1));            
	        }        
	    }
	}
}

    /*
    public static void main (String[] args) 
    {
          char[][] board = { {'b', 'z', 'q', 'w' },
                             {'i', 's', 'i', 's' },
                             {'m', 'l', 'm', 'v' },
                             {'h', 'o', 'f', 'l' },
                          };

          List<String> list = Boggle.boggleSolve(board);
          for (String str :  list) {
              System.out.println(str);
          }
    }
    */
