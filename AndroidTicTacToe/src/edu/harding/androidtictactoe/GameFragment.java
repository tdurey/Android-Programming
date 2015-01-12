package edu.harding.androidtictactoe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class GameFragment extends Fragment {

	private final String LOGTAG = "TTT";
	
	// Represents the game board
	private BoardView mBoardView;
	
	// Indicates if game is currently over or not
	private boolean mGameOver = false;
	
	private AudioPlayer mAudioPlayer = new AudioPlayer(); 
	
	// Whose turn to go first
	private char mGoFirst = TicTacToeGame.HUMAN_PLAYER;
	
	// Whose turn is it
	private char mTurn = TicTacToeGame.COMPUTER_PLAYER;    
	
	private int mHumanWins = 0;
	private int mComputerWins = 0;
	private int mTies = 0;
	
	// Represents the internal state of the game
	private TicTacToeGame mGame;
	
	private TextView mInfoTextView; 
	private TextView mHumanScoreTextView;
	private TextView mComputerScoreTextView;
	private TextView mTieScoreTextView;
	
	private SharedPreferences mPrefs;
	
	private boolean mSoundOn;
	
	private String mInfoText;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(LOGTAG, "onCreate - GameFragment");
		
		// Cause onCreateOptionsMenu to trigger
		setHasOptionsMenu(true);
		
		// Retain this fragment across configuration changes
		// This fragment is explicitly storing/restoring its own state instead.
	    //setRetainInstance(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		// Load from xml file
		inflater.inflate(R.menu.game_options, menu);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_game, container, false);
		
		// Clear prefs for testing
		//mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity()); 
		//mPrefs.edit().clear().commit();
		
		Log.d(LOGTAG, "onCreateView");
		
		if (mGame == null) {
			Log.d(LOGTAG, "NEW mGame");
			mGame = new TicTacToeGame();
		}
		
        mBoardView = (BoardView) v.findViewById(R.id.board);
        mBoardView.setGame(mGame);
        
        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);
        
       	mInfoTextView = (TextView) v.findViewById(R.id.information);               	
        mHumanScoreTextView = (TextView) v.findViewById(R.id.player_score);
        mComputerScoreTextView = (TextView) v.findViewById(R.id.ai_score);
        mTieScoreTextView = (TextView) v.findViewById(R.id.tie_score);   
                
        loadPreferences();
    	
        
        if (savedInstanceState == null) {   
        	Log.d(LOGTAG, "NO savedInstanceState");
        	startNewGame();
        }
        else {        	
        	// Restore the game's state
        	// The same thing can be accomplished with onRestoreInstanceState
        	mGame.setBoardState(savedInstanceState.getCharArray("board"));
        	mGameOver = savedInstanceState.getBoolean("mGameOver");        	
        	updateInfoText(savedInstanceState.getCharSequence("info").toString());
        	mTurn = savedInstanceState.getChar("mTurn");
        	mGoFirst = savedInstanceState.getChar("mGoFirst");
        	
        	// If it's the computer's turn, the previous turn did not take, so go again  
        	if (!mGameOver && mTurn == TicTacToeGame.COMPUTER_PLAYER) {        		
        		int move = mGame.getComputerMove();
        		setMove(TicTacToeGame.COMPUTER_PLAYER, move);
        	}        	
        }       
        
        
        updateInfoText(mInfoText);
                
        displayScores();
		
		return v;
	}
	
	@Override
	public void onStop() {
       super.onStop();
              
       // Save the current score, but not the state of the current game        
       SharedPreferences.Editor ed = mPrefs.edit();
       ed.putInt("mHumanWins", mHumanWins);
       ed.putInt("mComputerWins", mComputerWins);
       ed.putInt("mTies", mTies);
       ed.commit(); 
	}
	
	
	// Warning: This func is called when fragment is retained (setRetainInstance), 
	// but the savedInstanceState value will always be null!
	@Override
	public void onSaveInstanceState(Bundle outState) {		
		super.onSaveInstanceState(outState);		
		Log.d(LOGTAG, "onSaveInstanceState");
		outState.putCharArray("board", mGame.getBoardState());		
		outState.putBoolean("mGameOver", mGameOver);	
		outState.putCharSequence("info", mInfoTextView.getText());
		outState.putChar("mGoFirst", mGoFirst);
		outState.putChar("mTurn", mTurn);		
	}
	
	// Handles menu item selections 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.new_game:
        	
        	Animation boardAnim = AnimationUtils
        		.loadAnimation(getActivity(), R.anim.board_animation);
        	mBoardView.startAnimation(boardAnim);
        	
        	// Start new game after animation completes
        	Handler handler = new Handler();     		
    		handler.postDelayed(new Runnable() {
                public void run() {
                	startNewGame();                             	
                } 
     		}, 1000); 
    		
            return true;
        case R.id.settings:         	
        	// Launch the Settings activity which should notify us when 
        	// the user returns
        	startActivityForResult(new Intent(getActivity(), Settings.class), 0);     	
        	return true;
       /* case R.id.reset_scores:
        	mHumanWins = 0;
        	mComputerWins = 0;
            mTies = 0;
            displayScores();
            return true;
        case R.id.about:
        	showDialog(DIALOG_ABOUT);
        	return true;
       */
        }
        return false;
    }
    
    private void loadPreferences() {
    	
    	// Restore the scores from the persistent preference data source
    	if (mPrefs == null)
    		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity()); 
        
    	mHumanWins = mPrefs.getInt("mHumanWins", 0);  
        mComputerWins = mPrefs.getInt("mComputerWins", 0);
        mTies = mPrefs.getInt("mTies", 0);
        
    	mSoundOn = mPrefs.getBoolean(Settings.SOUND_PREFERENCE_KEY, true);
    	
    	mBoardView.setBoardColor(mPrefs.getInt(Settings.BOARD_COLOR_PREFERENCE_KEY, Color.GRAY));
    	mBoardView.invalidate();    // Repaint with new color
    	
    	String difficultyLevel = mPrefs.getString(Settings.DIFFICULTY_PREFERENCE_KEY, 
    			getResources().getString(R.string.difficulty_harder));
    	
    	if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy)))
    		mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
    	else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder)))
    		mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
    	else
    		mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert); 
    	
    	//Toast.makeText(this.getActivity(), "Difficulty: " + difficultyLevel, Toast.LENGTH_LONG).show();
    	Log.d(LOGTAG, "Difficulty: " + difficultyLevel);
    	
    	String goes_first = mPrefs.getString(Settings.GOES_FIRST_PREFERENCE_KEY,
    			getResources().getString(R.string.goes_first_alternate));
    	if (!goes_first.equals(getResources().getString(R.string.goes_first_alternate))) {
    		// See if any moves have been made.  If not, start a new game
    		// which will use the selected setting
    		if (mGame.boardIsClear()) {    			
    			Handler handler = new Handler();     		
        		handler.postDelayed(new Runnable() {
                    public void run() {
                    	startNewGame();                             	
                    } 
         		}, 1000);       		
    		}
    	}    	
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		// See if Back button was pressed on Settings activity
        if (requestCode == Activity.RESULT_CANCELED) {
        	// Apply potentially new settings      
        	
        	loadPreferences();	    	
        }
	}
	
	// Show the scores
    private void displayScores() {
    	mHumanScoreTextView.setText(Integer.toString(mHumanWins));
    	mComputerScoreTextView.setText(Integer.toString(mComputerWins));
    	mTieScoreTextView.setText(Integer.toString(mTies));
    }
    
    // Set up the game board. 
    private void startNewGame() {   	
    	
    	mGame.newGame();    	
    	mBoardView.invalidate();   // Redraw the board    	

    	// Determine who should go first based on settings
    	String goesFirst = mPrefs.getString(Settings.GOES_FIRST_PREFERENCE_KEY, 
    			getResources().getString(R.string.goes_first_alternate));
    	
    	if (goesFirst.equals(getResources().getString(R.string.goes_first_alternate))) {
    		// Alternate who goes first
    		if (mGoFirst == TicTacToeGame.COMPUTER_PLAYER) {    		
        		mGoFirst = TicTacToeGame.HUMAN_PLAYER;
        		mTurn = TicTacToeGame.COMPUTER_PLAYER;        		
        	}
        	else {
        		mGoFirst = TicTacToeGame.COMPUTER_PLAYER;
        		mTurn = TicTacToeGame.HUMAN_PLAYER;
        	}	
    	}
    	else if (goesFirst.equals(getResources().getString(R.string.goes_first_human))) 
    		mTurn = TicTacToeGame.HUMAN_PLAYER;    	
    	else
    		mTurn = TicTacToeGame.COMPUTER_PLAYER;
    	
    	// Start the game
    	if (mTurn == TicTacToeGame.COMPUTER_PLAYER) {
    		updateInfoText(R.string.first_computer);
    		int move = mGame.getComputerMove();
    		setMove(TicTacToeGame.COMPUTER_PLAYER, move);
    	}
    	else
    		updateInfoText(R.string.first_human);    	
    	
    	mGameOver = false;
    } 
    
    private void updateInfoText(int textResourceId) {
    	mInfoTextView.setText(textResourceId);
    	mInfoText = mInfoTextView.getText().toString();
    	
    	// Fade-in the text 
    	Animation messageAnim = AnimationUtils
        		.loadAnimation(getActivity(), R.anim.message_animation);
        mInfoTextView.startAnimation(messageAnim);
    }
    
    private void updateInfoText(String text) {
    	mInfoText = text;
    	mInfoTextView.setText(mInfoText);
    	
    	// Fade-in the text 
    	Animation messageAnim = AnimationUtils
        		.loadAnimation(getActivity(), R.anim.message_animation);
        mInfoTextView.startAnimation(messageAnim);
    }
    
    // Make a move
    private boolean setMove(char player, int location) {
    	
    	if (player == TicTacToeGame.COMPUTER_PLAYER) {    		
    		// Make the computer move after a delay of 1 second
    		final int loc = location;
	    	Handler handler = new Handler();     		
    		handler.postDelayed(new Runnable() {
                public void run() {
                	mGame.setMove(TicTacToeGame.COMPUTER_PLAYER, loc);
                	mBoardView.invalidate();   // Redraw the board
                	
                	try {
                		if (mSoundOn)
                			mAudioPlayer.play(getActivity(), R.raw.sword);
                	}
                	catch (IllegalStateException e) {};  // Happens if orientation changed before playing
                	
                	int winner = mGame.checkForWinner();
                	if (winner == 0) {
                		mTurn = TicTacToeGame.HUMAN_PLAYER;	                                	
                		updateInfoText(R.string.turn_human);
                	}
                	else 
    	            	endGame(winner);                              	
                } 
     		}, 1000);     		
                
    		return true;
    	}
    	else if (mGame.setMove(TicTacToeGame.HUMAN_PLAYER, location)) { 
    		mTurn = TicTacToeGame.COMPUTER_PLAYER;
        	mBoardView.invalidate();   // Redraw the board
    	   	if (mSoundOn) 
    	   		mAudioPlayer.play(getActivity(), R.raw.swish);	   	
    	   	return true;
    	}
    		   	    	
    	return false;
    }
    
    // Game is over logic
    private void endGame(int winner) {
    	if (winner == 1) {
    		mTies++;
    		mTieScoreTextView.setText(Integer.toString(mTies));
    		updateInfoText(R.string.result_tie); 
    	}
    	else if (winner == 2) {
    		mHumanWins++;
    		mHumanScoreTextView.setText(Integer.toString(mHumanWins));
    		String defaultMessage = getResources().getString(R.string.result_human_wins);
    		updateInfoText(mPrefs.getString(Settings.VICTORY_MESSAGE_PREFERENCE_KEY, 
    				defaultMessage));
    		mAudioPlayer.play(getActivity(), R.raw.cheer);
    	}
    	else if (winner == 3) {
    		mComputerWins++;
    		mComputerScoreTextView.setText(Integer.toString(mComputerWins));
    		updateInfoText(R.string.result_computer_wins);
    		mAudioPlayer.play(getActivity(), R.raw.lose);
    	}
    	
    	mGameOver = true;
    }
    
    // Listen for touches on the board
    private OnTouchListener mTouchListener = new OnTouchListener() {
    	
    	@Override
        public boolean onTouch(View v, MotionEvent event) {
       	        	
        	// Determine which cell was touched	    	
	    	int col = (int) event.getX() / mBoardView.getBoardCellWidth();
	    	int row = (int) event.getY() / mBoardView.getBoardCellHeight();
	    	int pos = row * 3 + col;
	    		    	
	    	if (!mGameOver && mTurn == TicTacToeGame.HUMAN_PLAYER &&
	    			setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {        		
            	
            	// If no winner yet, let the computer make a move
            	int winner = mGame.checkForWinner();
            	if (winner == 0) { 
            		updateInfoText(R.string.turn_computer); 
            		int move = mGame.getComputerMove();
            		setMove(TicTacToeGame.COMPUTER_PLAYER, move);            		
            	} 
            	else
            		endGame(winner);            	
            }
	    	
	    	// So we aren't notified of continued events when finger is moved
	    	return false;   
        }
    };
	
}
