package com.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.AndroidCharacter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

public class Earthquake extends Activity{

    private ListView earthquakeListView;
    private ArrayAdapter<Quake> arrayAdapter;
    private Quake selectedQuake;


    private ArrayList<Quake> earthquakes = new ArrayList<Quake>();

    private static final int MENU_UPDATE = Menu.FIRST;
    private static final int MENU_PREFERENCES = MENU_UPDATE + 1;

    private static final int QUAKE_DIALOG = 1;
    private static final int SHOW_PREFERENCES = 1;

    int minimumMagnitude = 0;
    boolean autoUpdate = false;
    int updateFreq = 0;

    @Override
    public void onCreate( Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        earthquakeListView = (ListView)this.findViewById( R.id.earthquakeListView );

        earthquakeListView.setOnItemClickListener( new AdapterView.OnItemClickListener(){
            public void onItemClick( AdapterView<?> adapterView, View view, int i, long l ) {
                selectedQuake = earthquakes.get( i );
                showDialog( QUAKE_DIALOG );

            }
        });

        int layoutId = android.R.layout.simple_list_item_1;
        arrayAdapter = new ArrayAdapter<Quake>( this, layoutId, earthquakes );
        earthquakeListView.setAdapter( arrayAdapter );

        updateFromPreferences();
        refreshEarthquakes();
    }

    @Override
    public Dialog onCreateDialog( int id ){
        switch( id ){
            case ( QUAKE_DIALOG ) : {
                LayoutInflater layoutInflater = LayoutInflater.from( this );
                View quakeDetailsView = layoutInflater.inflate( R.layout.quake_details, null );

                AlertDialog.Builder quakeDetailsDialog = new AlertDialog.Builder( this );
                quakeDetailsDialog.setTitle("Quake time");
                quakeDetailsDialog.setView( quakeDetailsView );
                return quakeDetailsDialog.create();
            }
        }
        return null;
    }

   @Override
   public void onPrepareDialog( int id, Dialog dialog ){
       switch ( id ){
           case ( QUAKE_DIALOG ) : {
               SimpleDateFormat sdf = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss");
               String dateString = sdf.format( selectedQuake.getDate() );
               String quakeText = String.format( "Magnitude %s \n %s \n %s", selectedQuake.getMagnitude(),
                                  selectedQuake.getDetails(), selectedQuake.getLink() );

               AlertDialog quakeDialog = (AlertDialog)dialog;
               quakeDialog.setTitle( dateString );
               TextView textView = (TextView)quakeDialog.findViewById( R.id.quakeDetailsTextView );
               textView.setText( quakeText );

               break;
           }
       }
   }


    @Override
    public boolean onCreateOptionsMenu( Menu menu ){
        super.onCreateOptionsMenu( menu );

        menu.add( 0, MENU_UPDATE, Menu.NONE, R.string.menu_update );
        menu.add( 0, MENU_PREFERENCES, Menu.NONE, R.string.menu_preferences );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem menuItem ){
        super.onOptionsItemSelected( menuItem );

        switch ( menuItem.getItemId() ){
            case ( MENU_UPDATE ) : {
                refreshEarthquakes();
                return true;
            }
            case ( MENU_PREFERENCES ) : {
                Intent preferencesIntent = new Intent( this, Preferences.class );
                startActivityForResult( preferencesIntent, SHOW_PREFERENCES );
                return true;
            }
        }

        return false;
    }

    private void refreshEarthquakes() {
	  // Get the XML
	  URL url;
	  try {
	    String quakeFeed = getString(R.string.quake_feed);
	    url = new URL(quakeFeed);

	    URLConnection connection;
	    connection = url.openConnection();

	    HttpURLConnection httpConnection = (HttpURLConnection)connection;
	    int responseCode = httpConnection.getResponseCode();

	    if (responseCode == HttpURLConnection.HTTP_OK) {
	      InputStream in = httpConnection.getInputStream();

	      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	      DocumentBuilder db = dbf.newDocumentBuilder();

	      // Parse the earthquake feed.
	      Document dom = db.parse(in);
	      Element docEle = dom.getDocumentElement();

	      // Clear the old earthquakes
	      earthquakes.clear();
//	      loadQuakesFromProvider();

	      // Get a list of each earthquake entry.
	      NodeList nl = docEle.getElementsByTagName("entry");
	      if (nl != null && nl.getLength() > 0) {
	        for (int i = 0 ; i < nl.getLength(); i++) {
	          Element entry = (Element)nl.item(i);
	          Element title = (Element)entry.getElementsByTagName("title").item(0);
	          Element g = (Element)entry.getElementsByTagName("georss:point").item(0);
	          Element when = (Element)entry.getElementsByTagName("updated").item(0);
	          Element link = (Element)entry.getElementsByTagName("link").item(0);

	          String details = title.getFirstChild().getNodeValue();
	          String hostname = "http://earthquake.usgs.gov";
	          String linkString = hostname + link.getAttribute("href");

	          String point = g.getFirstChild().getNodeValue();
	          String dt = when.getFirstChild().getNodeValue();
	          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
	          Date qdate = new GregorianCalendar(0,0,0).getTime();
	          try {
	            qdate = sdf.parse(dt);
	          } catch (ParseException e) {
	            e.printStackTrace();
	          }

	          String[] location = point.split(" ");
	          Location l = new Location("dummyGPS");
	          l.setLatitude(Double.parseDouble(location[0]));
	          l.setLongitude(Double.parseDouble(location[1]));

	          String magnitudeString = details.split(" ")[1];
	          int end =  magnitudeString.length()-1;
	          double magnitude = Double.parseDouble(magnitudeString.substring(0, end));

	          details = details.split(",")[1].trim();

	          Quake quake = new Quake(qdate, details, l, magnitude, linkString);

	          // Process a newly found earthquake
	          addNewQuake(quake);
	        }
	      }
	    }
	  } catch (MalformedURLException e) {
	    e.printStackTrace();
	  } catch (IOException e) {
	    e.printStackTrace();
	  } catch (ParserConfigurationException e) {
	    e.printStackTrace();
	  } catch (SAXException e) {
	    e.printStackTrace();
	  } finally {
	  }
	}

    private void addNewQuake( Quake quake ) {
        if( quake.getMagnitude() > minimumMagnitude ){
            earthquakes.add( quake );
            arrayAdapter.notifyDataSetChanged();
        }
    }

    private void updateFromPreferences(){
        Context context = getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( context );

        int minMagnitudeIndex = preferences.getInt( Preferences.PREF_MIN_MAG, 0 );
        if( minMagnitudeIndex < 0 ){
            minMagnitudeIndex = 0;
        }

        int freqIndex = preferences.getInt( Preferences.PREF_UPDATE_MAG, 0 );
        if( freqIndex < 0 ){
            freqIndex = 0;
        }

        autoUpdate = preferences.getBoolean( Preferences.PREF_AUTO_UPDATE, false );

        Resources resources = getResources();
        int[] minMagnitudeValues = resources.getIntArray( R.array.magnitude_values );
        int[] freqValues = resources.getIntArray( R.array.update_freq_values );

        minimumMagnitude = minMagnitudeValues[minMagnitudeIndex];
        updateFreq = freqValues[freqIndex];

    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ){
        super.onActivityResult( requestCode, resultCode, data );

        if( requestCode == SHOW_PREFERENCES ){
            if( resultCode == Activity.RESULT_OK ){
                updateFromPreferences();
                refreshEarthquakes();
            }
        }
    }
}
