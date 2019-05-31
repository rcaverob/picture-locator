package com.example.picture_locator;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MarkerOptions mMarker;
    private final String TAG = "MapActivity";

    private LatLng mGoalPosition;
    private Location mGoalLocation;
    private MenuItem mAnswerButton;

    public static final String EXTRA_SCORE = "map_score";
    public static final String EXTRA_GUESS_LOCATION = "map_guess_location";

    boolean answered = false;


    private String mQuizGuessUsers;
    private String mQuizGuessCoords;

    // For Dartmouth Mode
    LatLng mDartmouth;
    private boolean isDartmouth = true;

    private String mLongDistanceUnit = "km";
    private String mShortDistanceUnit = "m";

    private List<Double> mDistanceList;

    private LinearLayout mPostAnswerLayout;
    private TextView mDistText, mScoreText, mRankText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

//      Display the back button on the App bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize distance list
        mDistanceList = new ArrayList<>();

        // Dartmouth location
        mDartmouth = new LatLng(43.7033, -72.2885);


        // Layout for after answering
        mPostAnswerLayout = findViewById(R.id.map_post_answer_layout);
        // TextViews
        mDistText = findViewById(R.id.map_distance_text);
        mScoreText = findViewById(R.id.map_score_text);
        mRankText = findViewById(R.id.map_rank_text);


        // Get Extras
        Bundle extras = getIntent().getExtras();

        // Get the goal position from extras
        if (extras != null) {
            double lat = extras.getDouble(getString(R.string.key_latitude), 0.0);
            double longit = extras.getDouble(getString(R.string.key_longitude), 0.0);
            mQuizGuessUsers = extras.getString(getString(R.string.key_guess_users));
            mQuizGuessCoords = extras.getString(getString(R.string.key_guess_coords));
            mGoalPosition = new LatLng(lat, longit);

            // Create Location object from goal position
            mGoalLocation = new Location("");
            mGoalLocation .setLatitude(mGoalPosition.latitude);
            mGoalLocation .setLongitude(mGoalPosition.longitude);
        }

        isDartmouth = findIfIsDartmouth();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    private boolean findIfIsDartmouth() {
        Location dartLoc = new Location("");
        dartLoc.setLatitude(mDartmouth.latitude);
        dartLoc.setLongitude(mDartmouth.longitude);

        double dist = dartLoc.distanceTo(mGoalLocation);
        return dist < 2000;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (!answered){
                    mMap.clear();
                    mMarker = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED)).title("Your Guess");
                    mMap.addMarker(mMarker);
                }
            }
        });

        if (isDartmouth){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDartmouth, (float)16.5));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map, menu);

        mAnswerButton = menu.findItem(R.id.menu_map_answer);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_map_answer:
                if (answered){
                    finish();
                }else if (mMarker != null){
                    LatLng answer = mMarker.getPosition();
                    computeScore(answer);
                }
                return true;
            default:
                return  true;
        }
    }

    private void computeScore(LatLng answer) {
        // Mark the correct answer on Map
        mMap.addMarker(new MarkerOptions().position(mGoalPosition).icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("Answer"));

        int zoomLevel = isDartmouth ? 17 : 6;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mGoalPosition, zoomLevel));


        // Convert LatLng of Guess and Goal to Location
        Location guess = new Location("");
        guess.setLatitude(answer.latitude);
        guess.setLongitude(answer.longitude);

        // Compute distance between guess and goal
        double distance = guess.distanceTo(mGoalLocation);

        // Score based on distance
        int score = 0;

        int perfect = 25;
        int close = 100;
        int far = 300;
        int very_far = 750;

        if (! isDartmouth){
            perfect = 5000;
            close = 50000;
            far = 800000;
            very_far = 6000000;
        }

        if (distance < perfect){
            score = 1000;
        }
        else if (distance < close){
            score = 850;
        }
        else if (distance < far){
            score = 400;
        }
        else if (distance < very_far){
            score = 200;
        }

        Log.d(TAG, "computeScore: Distance is "+distance);

        // Populate the map with guesses of other users
        populateWithGuesses();

        String distString = getDistanceString(distance);

        // Show the distance, score, and rank to the user
        mPostAnswerLayout.setVisibility(View.VISIBLE);

        int rank = getRank(distance);

        mDistText.setText(MessageFormat.format("{0}{1}", mDistText.getText(), distString));
        mScoreText.setText(MessageFormat.format("{0}{1}", mScoreText.getText(), score));
        mRankText.setText(MessageFormat.format("{0}{1}", mRankText.getText(), rank));

        // Give info back to Quiz Activity
        Intent data = new Intent();
        double[] guess_location = new double[] {answer.latitude, answer.longitude};

        data.putExtra(EXTRA_SCORE, score);
        Log.d(TAG, "computeScore: Set Score to :"+score);
        data.putExtra(EXTRA_GUESS_LOCATION, guess_location);
        setResult(RESULT_OK, data);

        answered = true;
        mAnswerButton.setTitle("RETURN");
    }

    // Gets rank based on distance from target
    private int getRank(double dist) {
        Collections.sort(mDistanceList);
        int rank = 1;
        for (int i = 0; i < mDistanceList.size(); i ++){
            if (mDistanceList.get(i) > dist){
                break;
            }
            rank ++;
        }
        return rank;
    }

    // Adds the markers corresponding to the guesses all users have made in this picture
    private void populateWithGuesses() {
        if (mQuizGuessUsers.isEmpty()){
            return;
        }
        String[] users = mQuizGuessUsers.split(" ");
        String[] coords = mQuizGuessCoords.split(" ");

        for (int i = 0; i < users.length; i++){
            int coord_idx = 2*i;
            LatLng coord = new LatLng(Double.parseDouble(coords[coord_idx]),
                    Double.parseDouble(coords[coord_idx+1]));

            Location loc = new Location("");
            loc.setLatitude(coord.latitude);
            loc.setLongitude(coord.longitude);

            double dist = loc.distanceTo(mGoalLocation);
            mDistanceList.add(dist);

            String distString = getDistanceString(dist);

            mMap.addMarker(new MarkerOptions().position(coord).icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("Guess from: "+users[i]).snippet("Distance to target: "+distString));
        }
    }

    // Returns an appropriate distance string with corresponding units
    private String getDistanceString(double dist) {
        String distUnit = mShortDistanceUnit;
        if(dist > 1000){
            dist = dist/1000;
            distUnit = mLongDistanceUnit;
        }
        return String.format(Locale.US, "%.2f", dist) + " "+ distUnit;
    }

}
