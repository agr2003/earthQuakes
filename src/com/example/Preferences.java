package com.example;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.AndroidCharacter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 05.06.11
 * Time: 20:57
 * To change this template use File | Settings | File Templates.
 */
public class Preferences extends Activity {

    private CheckBox autoUpdate;
    private Spinner updateFreqSpinner;
    private Spinner magnitudeSpinner;

    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
    public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
    public static final String PREF_UPDATE_MAG = "PREF_UPDATE_FREQ";

    private SharedPreferences preferences;

    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.preferences );

        updateFreqSpinner = (Spinner)findViewById( R.id.spinner_update_freq );
        magnitudeSpinner = (Spinner)findViewById( R.id.spinner_quake_mag );
        autoUpdate = (CheckBox)findViewById( R.id.checkbox_auto_update );

        populateSpinners();

        Context context = getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences( context );
        updateUIFromPreferences();

        Button okButton = (Button)findViewById( R.id.okButton );
        okButton.setOnClickListener( new View.OnClickListener() {

            public void onClick( View view ){
                savePreferences();
                Preferences.this.setResult( RESULT_OK );
                finish();
            }
        });

        Button cancelButton = (Button)findViewById( R.id.cancelButton );
        cancelButton.setOnClickListener( new View.OnClickListener() {
            public void onClick( View view ) {
                Preferences.this.setResult( RESULT_CANCELED );
                finish();
            }
        });

    }

    private void savePreferences(){
        int updateIndex = updateFreqSpinner.getSelectedItemPosition();
        int minMagnitudeIndex = magnitudeSpinner.getSelectedItemPosition();
        boolean autoUpdateChecked = autoUpdate.isChecked();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean( PREF_AUTO_UPDATE, autoUpdateChecked );
        editor.putInt( PREF_UPDATE_MAG, updateIndex );
        editor.putInt( PREF_MIN_MAG, minMagnitudeIndex );
        editor.commit();
    }

    private void updateUIFromPreferences(){
        boolean autoUpdateChecked = preferences.getBoolean( PREF_AUTO_UPDATE, false );
        int updateFreqIndex = preferences.getInt(PREF_UPDATE_MAG, 2);
        int minMagnitudeIndex = preferences.getInt( PREF_MIN_MAG, 0 );

        updateFreqSpinner.setSelection( updateFreqIndex );
        magnitudeSpinner.setSelection( minMagnitudeIndex );
        autoUpdate.setChecked( autoUpdateChecked );
    }

    private void populateSpinners(){
        // Populate the update frequency spinner
        ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource( this,
                R.array.update_freq_options, android.R.layout.simple_spinner_item );
        int spinner_dropdown_item = android.R.layout.simple_spinner_dropdown_item;
        frequencyAdapter.setDropDownViewResource( spinner_dropdown_item );
        updateFreqSpinner.setAdapter( frequencyAdapter );

        // Populate the minimum magnitude spinner
        ArrayAdapter<CharSequence> magnitudeAdapter = ArrayAdapter.createFromResource( this,
                R.array.magnitude_options, android.R.layout.simple_spinner_item );
        magnitudeAdapter.setDropDownViewResource( spinner_dropdown_item );
        magnitudeSpinner.setAdapter( magnitudeAdapter );

    }
}