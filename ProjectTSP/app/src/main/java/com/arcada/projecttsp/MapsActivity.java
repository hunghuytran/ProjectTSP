package com.arcada.projecttsp;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Polyline line;
    private GoogleMap mMap;
    private Button mAddButton;
    private Button mRouteButton;
    private Button mSaveButton;
    private EditText mAddText;
    private EditText mStartText;
    private Vector<Waypoint> mWaypoints;
    private Marker markerStartLoc;
    ArrayList<Integer> tempList = new ArrayList<>();

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
                //Connect to internet and search the url string for elements.
                mAddText = (EditText) findViewById(R.id.addloc_text);
                mStartText = (EditText) findViewById(R.id.startloc_text);

                if (mAddText.getText().toString().matches("") ||
                        mStartText.getText().toString().matches("")) {
                    Toast.makeText(MapsActivity.this, "The fields seems a bit empty.",Toast.LENGTH_SHORT).show();
                } else {

                    String mAddTextUrl = "https://maps.googleapis.com/maps/api/geocode/json?address="
                            + mAddText.getText().toString() + "&key=AIzaSyCmfaY9ogGe17Qm0BkIwNZ2zhqlTPLnshE";
                    String mStartTextUrl = "https://maps.googleapis.com/maps/api/geocode/json?address="
                            + mStartText.getText().toString() + "&key=AIzaSyCmfaY9ogGe17Qm0BkIwNZ2zhqlTPLnshE";
                    onAddLocation(mStartTextUrl ,mAddTextUrl);

                }
            }
        });

        mRouteButton = (Button) findViewById(R.id.route_button);
        mRouteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //for every value in size() 1 is added to indexes[].
                int[] indexes = new int[mWaypoints.size()];
                int x = 0;
                for (int j = 0; j < mWaypoints.size(); j++){
                    indexes[x] = j;
                    x++;
                }
                // Find all possible comibation from the index value = 1 to the length. Taken from int [] indexes.
                permute(indexes, 1, indexes.length);

                //Wire the textview together with the display at xml layout.

                //Prints out the best route for the travelling man.

                String route = "The recommended route for this trip is:\n";

                for(int i = 0; i < mWaypoints.size(); i++)
                {
                    route += mWaypoints.get(tempList.get(i)).getLocation() + "\n";
                }
                route += mWaypoints.get(0).getLocation();

                int n = 0;
                for(; n < mWaypoints.size()-1; n++)
                {
                    createPolyline(mWaypoints.get(tempList.get(n)).getLocation(), mWaypoints.get(tempList.get(n+1)).getLocation());
                }
                createPolyline(mWaypoints.get(n).getLocation(), mWaypoints.get(0).getLocation());

                System.out.println(route);

            }
        });
    }

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

            JSONObject jsonDir = new JSONObject(jsonDirResp);
            JSONArray jsonDirArr = new JSONArray(jsonDir.getString("routes"));
            jsonDir = jsonDirArr.getJSONObject(0);
            jsonDir = jsonDir.getJSONObject("overview_polyline");
            String polyline = jsonDir.getString("points");
            System.out.println(polyline);
            List<LatLng> list = decodePoly(polyline);

            PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
            for (int z = 0; z < list.size(); z++) {
                LatLng point = list.get(z);
                options.add(point);
            }
            line = mMap.addPolyline(options);

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

                mWaypoints.setElementAt(new Waypoint(loc2, lat2, lng2), 0);
                markerStartLoc.setPosition(new LatLng(lat2, lng2));
            }


            //Location, latitude, longitude added to the vector list.
            mWaypoints.add(new Waypoint(loc, lat, lng));

            for (int i = 0; i < mWaypoints.size(); i++) {
                System.out.println(mWaypoints.get(i).getLocation() + ", " +
                        mWaypoints.get(i).getLatitude() + ", " +
                        mWaypoints.get(i).getLongitude());
            }


            mAddText.setText("");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void permute(int[] array,int i,int length)
    {
        if (length == i) {
            bestRoute(array, mWaypoints);
            return;
        }

        int j = i;
        for (j = i; j < length; j++)
        {
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
            permute(array,i+1,length);
            temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
        return;
    }

    //Calculates the best route out of all possibilities by storing it in a temporary arraylist.
    //The arraylist is used in the textView later.
    public void bestRoute(int[] array, Vector<Waypoint> waypoint) {
        //Adds up the summary of the distance to totalDist.

        double distance = 0.0;
        int i = 1;

        for(; i < waypoint.size() - 1; i++)
        {
            distance += getDistance(waypoint.get(array[i]).getLatitude(), waypoint.get(array[i]).getLongitude(), waypoint.get(array[i+1]).getLatitude(), waypoint.get(array[i+1]).getLongitude());
        }

        distance += getDistance(waypoint.get(i).getLatitude(), waypoint.get(i).getLongitude(), waypoint.get(0).getLatitude(), waypoint.get(0).getLongitude());

        //If the tempList is empty then it will loop as long as array.length is bigger than k. In tempList, the totalsum is added for later use.
        if(tempList.isEmpty()) {
            for(int k = 0; k < array.length; k++) {
                tempList.add(array[k]);
            }
            tempList.add((int)distance);
        }
        //If totalDist is smaller than the current tempList value (totalDist) then it will overrite that value.
        if(distance < tempList.get(tempList.size() - 1)) {
            tempList.clear();
            for(int k = 0; k < array.length; k++) {
                tempList.add(array[k]);
            }
            tempList.add((int) distance);
        }
    }

    public double getDistance(double lat1, double lon1, double lat2, double lon2)
    {
        lon1 = lon1*Math.PI/180;
        lat1 = lat1*Math.PI/180;
        lon2 = lon2*Math.PI/180;
        lat2 = lat2*Math.PI/180;

        //Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;

        double a = Math.pow(Math.sin(dlat/2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2),2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double km = 6367 * c;

        return km;
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mSaveButton = (Button) findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                // Instantiates a new Polyline object and adds points to define a rectangle
                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0))
                        .width(5)
                        .color(Color.RED));
            }
        });
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
