package edu.harding.androidtictactoe;

import android.view.View;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

public class BoardView extends View  {
	
	// Dimensions are set in onSizeChanged
	private int mBoardWidth;
	private int mBoardHeight;
	private int mBoardCellWidth;
	private int mBoardCellHeight;
	
	private int mBoardGridWidth = 6;
	private int mBoardColor = Color.GRAY;
	
	// We need a reference to the game so we can draw the board
	private TicTacToeGame mGame;
			
	private Bitmap mHumanBitmap;
	private Bitmap mComputerBitmap;
	
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	public BoardView(Context context) {
		super(context);		
		initialize();
	}
	
	public BoardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);		 
		initialize();
	}

	public BoardView(Context context, AttributeSet attrs) {
	    super(context, attrs);	            	
    	initialize();
	}

	public void initialize() {   	
		mHumanBitmap = BitmapFactory.decodeResource(getResources(), 
               R.drawable.x_img); 
		mComputerBitmap = BitmapFactory.decodeResource(getResources(), 
               R.drawable.o_img);			 	    	
	} 

	public void setGame(TicTacToeGame game) {
		mGame = game;
	}
	
	public int getBoardWidth() {
		return mBoardWidth;
	}
	
	public int getBoardHeight() {
		return mBoardHeight;
	}
	
	public int getBoardCellWidth() {
		return mBoardCellWidth;
	}
	
	public int getBoardCellHeight() {
		return mBoardCellHeight;
	}
	
	public void setBoardColor(int boardColor) {
		mBoardColor = boardColor;
	}
	
	public int getBoardColor() {
		return mBoardColor;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			
	   	// w and h contain the new width and height	   	
	   	mBoardHeight = h;
		mBoardWidth = w;
		
		mBoardCellWidth = mBoardWidth / 3;
    	mBoardCellHeight = mBoardHeight / 3;
	}	
	
	@Override
	public void onDraw(Canvas canvas) {
		 super.onDraw(canvas);		 

		 int halfGridWidth = mBoardGridWidth / 2;
		 		
		 mPaint.setColor(mBoardColor);
         mPaint.setStyle(Paint.Style.FILL);
         
         // Draw the board lines
         canvas.drawRect(mBoardCellWidth - halfGridWidth, 2, 
        		 mBoardCellWidth + halfGridWidth, mBoardHeight, mPaint);
         canvas.drawRect(mBoardCellWidth * 2 - halfGridWidth, 2, 
        		 mBoardCellWidth * 2 + halfGridWidth, mBoardHeight, mPaint);
         canvas.drawRect(0, mBoardCellHeight - halfGridWidth,
        		 mBoardWidth, mBoardCellHeight + halfGridWidth, mPaint);
         canvas.drawRect(0, mBoardCellHeight * 2 - halfGridWidth,
        		 mBoardWidth, mBoardCellHeight * 2 + halfGridWidth, mPaint);
 
		 // Draw all the pieces
		 for (int i = 0; i < TicTacToeGame.BOARD_SIZE; i++) {
			 int col = i % 3;
			 int row = i / 3;
			 
			 // Define the boundaries of a destination rectangle for the image
			 int left = col * mBoardCellWidth + mBoardGridWidth;
			 int top = row * mBoardCellHeight + mBoardGridWidth;
			 int right = left + mBoardCellWidth - 10;
			 int bottom = top + mBoardCellHeight - mBoardGridWidth - 6;
			 				 
			 if (mGame != null && mGame.getBoardOccupant(i) == TicTacToeGame.HUMAN_PLAYER) {				 					 
				 canvas.drawBitmap(mHumanBitmap, 
						 null,  // src
						 new Rect(left, top, right, bottom),  // dest
						 null);
				 
			 }
			 else if (mGame != null && mGame.getBoardOccupant(i) == TicTacToeGame.COMPUTER_PLAYER) {
				 canvas.drawBitmap(mComputerBitmap, 
						 null,  // src
						 new Rect(left, top, right, bottom),  // dest 
						 null);		
			 }
		 }		 
	 }
}
