package edu.harding.androidtictactoe;

import android.content.Context;
import android.media.MediaPlayer;

public class AudioPlayer {

	private MediaPlayer mPlayer;
	
	public void stop() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
	
	public void play(Context c, int soundId) {
		stop();
		
		mPlayer = MediaPlayer.create(c, soundId);
		
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				stop();				
			}
		});
		
		mPlayer.start();
	}
}
