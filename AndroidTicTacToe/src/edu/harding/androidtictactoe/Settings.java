package edu.harding.androidtictactoe;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import edu.harding.androidtictactoe.ColorPickerDialog.OnColorChangedListener;

public class Settings extends PreferenceActivity {
	
	private final String LOGTAG = "TTT";
	
	public static final String SOUND_PREFERENCE_KEY = "sound";
	public static final String GOES_FIRST_PREFERENCE_KEY = "goes_first";
	public static final String BOARD_COLOR_PREFERENCE_KEY = "board_color";
	public static final String DIFFICULTY_PREFERENCE_KEY = "difficulty_level";
	public static final String VICTORY_MESSAGE_PREFERENCE_KEY = "victory_message";
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		// Turn on up button
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (NavUtils.getParentActivityName(this) != null)
				this.getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
							
		final ListPreference goesFirstPref = (ListPreference) findPreference(GOES_FIRST_PREFERENCE_KEY);
		String goesFirst = prefs.getString(GOES_FIRST_PREFERENCE_KEY, "Alternate");
		goesFirstPref.setSummary((CharSequence) goesFirst);
		goesFirstPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				goesFirstPref.setSummary((CharSequence) newValue);

				// Since we are handling the pref, we must save it
				SharedPreferences.Editor ed = prefs.edit();
				ed.putString(GOES_FIRST_PREFERENCE_KEY, newValue.toString());
				ed.commit();
				
				return true;
			}
		});		
		
		final ListPreference difficultyLevelPref = (ListPreference) 
				findPreference(DIFFICULTY_PREFERENCE_KEY);
		String difficulty = prefs.getString(DIFFICULTY_PREFERENCE_KEY, 
				getResources().getString(R.string.difficulty_harder));
		difficultyLevelPref.setSummary((CharSequence) difficulty);
		difficultyLevelPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				difficultyLevelPref.setSummary((CharSequence) newValue);

				// Since we are handling the pref, we must save it
				SharedPreferences.Editor ed = prefs.edit();
				ed.putString(DIFFICULTY_PREFERENCE_KEY, newValue.toString());
				ed.commit();
				
				return true;
			}
		});
		
		final EditTextPreference victoryMessagePref = (EditTextPreference) 
				findPreference(VICTORY_MESSAGE_PREFERENCE_KEY);
		String victoryMessage = prefs.getString(VICTORY_MESSAGE_PREFERENCE_KEY, getResources().getString(R.string.result_human_wins));
		victoryMessagePref.setSummary("\"" + victoryMessage + "\"");
		victoryMessagePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				victoryMessagePref.setSummary((CharSequence) newValue);

				// Save the new preference
				SharedPreferences.Editor ed = prefs.edit();
				ed.putString(VICTORY_MESSAGE_PREFERENCE_KEY, newValue.toString());
				ed.commit();
				
				return true;
			}
		});
		
		
		final Preference boardColorPref = (Preference) findPreference(BOARD_COLOR_PREFERENCE_KEY);
		boardColorPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
            public boolean onPreferenceClick(Preference preference) { 
				int color = prefs.getInt(BOARD_COLOR_PREFERENCE_KEY, Color.GRAY);
				new ColorPickerDialog(Settings.this, new OnColorChangedListener() {

					@Override
					public void colorChanged(int color) {
						
						// Save the new color
						SharedPreferences.Editor ed = prefs.edit();
						ed.putInt(BOARD_COLOR_PREFERENCE_KEY, color);
						ed.commit();							
					}					
				},
                color).show();
                return true;
            }
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			
			// Go back to main screen.  Had to add android:launchMode="singleTop" 
			// to the parent activity in manifest to get it to not re-create itself
			if (NavUtils.getParentActivityName(this) != null)
				NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
