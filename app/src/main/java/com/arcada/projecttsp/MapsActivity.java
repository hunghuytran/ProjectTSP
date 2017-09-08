package com.arcada.projecttsp;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    Polyline line;
    boolean routeUsed;
    private Permute p;
    private GoogleMap mMap;
    private Button mAddButton;
    private Button mRouteButton;
    private Button mClearButton;
    private Button mSaveButton;
    private EditText mDistText;
    private EditText mAddText;
    private EditText mStartText;
    private Vector<Waypoint> mWaypoints;
    private Marker markerStartLoc;
    private double finalKm;
    private String distanceMatrix = new String();
    private boolean addPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Enable the add button
        mWaypoints = new Vector();
        mAddButton = (Button) findViewById(R.id.add_button);
        mAddButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                /* Picks the text from edittext to convert for later use.
                This button is adding start location and add location to a vector list */

                mAddText = (EditText) findViewById(R.id.addloc_text);
                mStartText = (EditText) findViewById(R.id.startloc_text);
                String add = mAddText.getText().toString().replaceAll("\\s+","%20");
                String start = mStartText.getText().toString().replaceAll("\\s+","%20");

                /* Checks if the the start and add fields are empty or not */
                if (mAddText.getText().toString().matches("") ||
                        mStartText.getText().toString().matches("") ||
                        routeUsed == true) {
                    Toast.makeText(MapsActivity.this, "The fields seems a bit empty or you should clean the vector list",Toast.LENGTH_SHORT).show();
                }
                /* This is triggered if the above isn't fulfilled*/
                else
                {
                    String mAddTextUrl = "https://maps.googleapis.com/maps/api/geocode/json?address="
                            + add + "&key=AIzaSyCmfaY9ogGe17Qm0BkIwNZ2zhqlTPLnshE";
                    String mStartTextUrl = "https://maps.googleapis.com/maps/api/geocode/json?address="
                            + start + "&key=AIzaSyCmfaY9ogGe17Qm0BkIwNZ2zhqlTPLnshE";

                    /* Method for adding location to a vector list */
                    onAddLocation(mStartTextUrl ,mAddTextUrl);
                    addPressed = true;

                }
            }
        });

        mSaveButton = (Button) findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(mWaypoints.isEmpty() || addPressed == false)
                {
                    Toast.makeText(MapsActivity.this, "Your vector list seems empty, please fill it", Toast.LENGTH_LONG).show();
                }
                else {
                    String points = "";
                    for (int i = 0; i < mWaypoints.size(); i++) {
                        points += mWaypoints.get(i).getLocation() + "|";
                    }
                    String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins="
                            + points + "&destinations=" + points + "&key=AIzaSyCmfaY9ogGe17Qm0BkIwNZ2zhqlTPLnshE";

                    //Creates an try and catch loop to see if the codes works.
                    try {
                        distanceMatrix = new GetLocation().execute(url).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }

        });


        mRouteButton = (Button) findViewById(R.id.route_button);
        mRouteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /* Checks if the vector list is empty or not, triggered if it's empty*/
                if(mWaypoints.isEmpty())
                {
                 Toast.makeText(MapsActivity.this, "Your vector list seems empty, please fill it", Toast.LENGTH_LONG).show();
                }

                /*This will be triggered if the above isn't fulfilled, it permutes the vector list in different order*/
                else {

                    //for every value in size() 1 is added to indexes[].
                    int[] indexes = new int[mWaypoints.size()];
                    int x = 0;
                    for (int j = 0; j < mWaypoints.size(); j++) {
                        indexes[x] = j;
                        x++;
                    }
                    // Find all possible comibation from the index value = 1 to the length. Taken from int [] indexes.
                    p = new Permute(indexes, 1, indexes.length, mWaypoints, distanceMatrix);
                    ExecutorService threadPool = Executors.newFixedThreadPool(2);

                    //Aktivera en tråd från threadpoolen och ge som uppgift åt den att exekvera metoden
                    //doSomething i HeavAlgroithm-klassen
                    Future t1;

                    //Vi startar upp trådarna
                    t1 = threadPool.submit(p);


                    //Vänta på att båda trådarna (t1 och t2) har kört klart
                    try {
                        t1.get();

                    }
                    catch(Exception e)
                    {

                    }

                    //Wire the textview together with the display at xml layout.
                    //Prints out the best route for the travelling man.

                    /* Shows the best route by printing out the order of the locations*/
                    String route = "The recommended route for this trip is: ";
                    for (int i = 0; i < mWaypoints.size(); i++) {
                        route += mWaypoints.get(p.getTempList().get(i)).getLocation() + ", ";
                    }
                    route += mWaypoints.get(0).getLocation() + ". This algorithm uses brute!";

                    //Test for printing out templist and showing it. By checking if the arraylist does not equal null or is empty!

                    if (p.getTempList() != null && !p.getTempList().isEmpty()) {
                        finalKm = p.getTempList().get(p.getTempList().size()-1);
                    }

                    String printKm = "The distance is: "+finalKm+" km.";
                    mDistText = (EditText) findViewById(R.id.distance_view);
                    mDistText.setText(printKm);

                    //Creates the polylines on google maps. The route from location to location for every location in the vector list.
                    int n = 0;
                    for (; n < mWaypoints.size() - 1; n++) {
                        createPolyline(mWaypoints.get(p.getTempList().get(n)).getLocation(), mWaypoints.get(p.getTempList().get(n + 1)).getLocation());
                    }
                    createPolyline(mWaypoints.get(p.getTempList().get(n)).getLocation(), mWaypoints.get(p.getTempList().get(0)).getLocation());

                    System.out.println(route);
                    routeUsed = true;
                    addPressed = false;
                }
            }
        });
    }

    /* Method for creating the polyline. It gets a json respond from googles server then it sends back the overview_polyline,
    which contains the route from a location to another location. This method is repeating in the route button*/
    public void createPolyline(String startDir, String endDir) {

        String dirUrl = "https://maps.googleapis.com/maps/api/directions/json?origin="+startDir+
                "&destination="+endDir+"&key=AIzaSyCmfaY9ogGe17Qm0BkIwNZ2zhqlTPLnshE";

        String jsonDirResp = new String();

        try {
            jsonDirResp = new GetLocation().execute(dirUrl).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {

            /* Creates JSONObject - JSONArray and get the strings which can be used later */
            JSONObject jsonDir = new JSONObject(jsonDirResp);
            JSONArray jsonDirArr = new JSONArray(jsonDir.getString("routes"));
            jsonDir = jsonDirArr.getJSONObject(0);
            jsonDir = jsonDir.getJSONObject("overview_polyline");
            String polyline = jsonDir.getString("points");

            try {
               PolylineOptions p = new Decode().execute(polyline).get();
                line = mMap.addPolyline(p);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onAddLocation(String startLoc, String addLoc)
    {
        String jsonResp = new String();
        String jsonStart = new String();

        //Creates an try and catch loop to see if the codes works.
        try {
            jsonResp = new GetLocation().execute(addLoc).get();
            jsonStart = new GetLocation().execute(startLoc).get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //Create JSON object and array by searching the url json.
        try {
            //These lines are for the first added object.
            JSONObject jsonObj = new JSONObject(jsonResp);
            JSONArray jsonArr = new JSONArray(jsonObj.getString("results"));

            //These lines parses the name of the location for adding to vector list.
            JSONObject jsonAdd = jsonArr.getJSONObject(0);
            JSONArray jsonAddArr = new JSONArray(jsonAdd.getString("address_components"));
            JSONObject jsonLoc = jsonAddArr.getJSONObject(0);
            String loc = jsonLoc.getString("long_name");

            //These line parses the lat and lng and gets added to the vector list.
            JSONObject geoLoc = jsonAdd.getJSONObject("geometry");
            JSONObject latLng = geoLoc.getJSONObject("location");
            double lat = Double.valueOf(latLng.getString("lat"));
            double lng = Double.valueOf(latLng.getString("lng"));

            //This lines is for the start object.

            JSONObject jsonObj2 = new JSONObject(jsonStart);
            JSONArray jsonArr2 = new JSONArray(jsonObj2.getString("results"));

            //These lines parses the name of the location for adding to vector list.
            JSONObject jsonAdd2 = jsonArr2.getJSONObject(0);
            JSONArray jsonAddArr2 = new JSONArray(jsonAdd2.getString("address_components"));
            JSONObject jsonLoc2 = jsonAddArr2.getJSONObject(0);
            String loc2 = jsonLoc2.getString("long_name");

            //These line parses the lat and lng and gets added to the vector list.
            JSONObject geoLoc2 = jsonAdd2.getJSONObject("geometry");
            JSONObject latLng2 = geoLoc2.getJSONObject("location");
            double lat2 = Double.valueOf(latLng2.getString("lat"));
            double lng2 = Double.valueOf(latLng2.getString("lng"));



            //Mark in google maps
            LatLng markedLoc = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(markedLoc).title(mAddText.getText().toString()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(markedLoc));

            //The start location is added if waypoint is empty
            //Set the pinpoint on google maps
            if (mWaypoints.isEmpty())
            {
                mWaypoints.add(new Waypoint(loc2, lat2, lng2));
                LatLng markedStartLoc = new LatLng(lat2, lng2);
                markerStartLoc = mMap.addMarker(new MarkerOptions().position(markedStartLoc)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
            else
            {
            //If the waypoiny is not empty then it will just set the new location for the start point
                mWaypoints.setElementAt(new Waypoint(loc2, lat2, lng2), 0);
                markerStartLoc.setPosition(new LatLng(lat2, lng2));
            }


            //Location, latitude, longitude added to the vector list.
            mWaypoints.add(new Waypoint(loc, lat, lng));


            mAddText.setText("");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        UiSettings uiSet = mMap.getUiSettings();
        uiSet.setZoomControlsEnabled(true);
        mClearButton = (Button) findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(new View.OnClickListener()
        {
            /* Clears the map and vector list on click*/
            @Override
            public void onClick(View v) {
                mMap.clear();
                mWaypoints.clear();
                mDistText.setText("");
                Toast.makeText(MapsActivity.this,"Your list and map has been cleared", Toast.LENGTH_LONG).show();
                routeUsed = false;
            }
        });
    }
}
