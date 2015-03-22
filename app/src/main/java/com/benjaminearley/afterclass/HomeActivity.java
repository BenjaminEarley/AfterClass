package com.benjaminearley.afterclass;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dexafree.materialList.cards.BigImageButtonsCard;
import com.dexafree.materialList.view.MaterialListView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends ActionBarActivity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String user_id;
    private String user_name;

    private long xp;

    SharedPreferences prefs;

    Typeface myTypeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getEvents();
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        checkPlayServices();

        Bundle extras = getIntent().getExtras();
        this.user_id = extras.getString("id");
        this.user_name = extras.getString("name");

        prefs = this.getSharedPreferences(
                "com.benjaminearley.afterclass", Context.MODE_PRIVATE);

        myTypeface = Typeface.createFromAsset(getAssets(), "latoRegular.ttf");

        TextView name = (TextView) findViewById(R.id.name);
        name.setText(this.user_name);
        name.setTypeface(myTypeface);


    }

    @Override
    public void onResume() {
        super.onResume();

        if (prefs.contains("xp")) {
            prefs.getLong("xp", xp);
        } else {
            xp = 0;
        }

        myTypeface = Typeface.createFromAsset(getAssets(), "latoRegular.ttf");



        TextView mTextView = (TextView) findViewById(R.id.xp);
        mTextView.setText(xp + "XP");
        mTextView.setTypeface(myTypeface);
        TextView mTextView2 = (TextView) findViewById(R.id.level);
        mTextView2.setText(Integer.toString(setLevel(xp)));
        mTextView2.setTypeface(myTypeface);
    }

    @Override
    public void onPause() {
        super.onPause();

        prefs.edit().putLong("xp", xp).apply();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.logout) {
            Intent intent = new Intent();
            intent.setClass(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("logout", "logout");
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getEvents() {

        String url = "http://afterclass.johnspaetzel.com/events";
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("JSON Request", response.toString());
                try {
                    // Parsing json object response
                    JSONArray jsonEvents = response.getJSONArray("events");
                    int length = jsonEvents.length();
                    ArrayList<Event> eventsList = new ArrayList<>();

                    final MaterialListView mListView = (MaterialListView) findViewById(R.id.events_listview);

                    for ( int i = 0; i < length; i++ ) {
                        JSONObject jsonEvent = (JSONObject) jsonEvents.get(i);
                        Event event = new Event();
                        event.Id = jsonEvent.getInt("Id");
                        event.Name = jsonEvent.getString("Name");
                        event.Description = jsonEvent.getString("Description");
                        event.EndDatetime = jsonEvent.getString("EndDatetime");
                        event.StartDatetime = jsonEvent.getString("StartDatetime");
                        event.Latitude = jsonEvent.getString("Latitude");
                        event.Longitude = jsonEvent.getString("Longitude");
                        event.LocationName = jsonEvent.getString("LocationName");
                        eventsList.add(event);

                        BigImageButtonsCard card = new BigImageButtonsCard (getApplicationContext());

                        card.setTitle(event.Name);
                        card.setDescription(event.Description);
                        card.setDrawable("http://api.tiles.mapbox.com/v4/zzbomb.lhb63odi/"+event.Longitude+","+event.Latitude+",19/1280x300.png?access_token=pk.eyJ1Ijoienpib21iIiwiYSI6ImFUXzl4V2MifQ.TtHzm29PQS99KQ0dXf7gGA");

                        card.setLeftButtonText("More Info");

                        card.setRightButtonTextColorRes(R.color.primary_dark);
                        card.setRightButtonText("Check In");


                        mListView.add(card);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Volley Error", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }

    }


    public void locationSettingsOffAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Please Turn On Location Services and Restart App");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("Google Play Services", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private int setLevel(long xp) {

        int level = 1;

        if (xp < 1000) {
            level = 1;
        }
        else if (xp >= 1000){
            level = 2;
        }
        else if (xp >= 3000){
            level = 3;
        }
        else if (xp >= 5000){
            level = 4;
        }
        else if (xp >= 10000){
            level = 5;
        }
        else if (xp >= 20000){
            level = 6;
        }
        else if (xp >= 50000){
            level = 7;
        }
        else if (xp >= 100000){
            level = 8;
        }

        return level;
    }

}
