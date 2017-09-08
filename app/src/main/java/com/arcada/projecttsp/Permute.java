package com.arcada.projecttsp;

import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Hung on 2016-05-22.
 */
public class Permute implements Runnable {

    public ArrayList<Integer> getTempList() {
        return tempList;
    }

    private ArrayList<Integer> tempList = new ArrayList<>();
    private int[] array;
    private int i;
    private int length;
    private Vector<Waypoint> waypoint;
    private String distanceMatrix;

    public Permute(int[] array,int i,int length, Vector<Waypoint> waypoint, String distanceMatrix)
    {
        this.waypoint = waypoint;
        this.i = i;
        this.length = length;
        this.array = array;
        this.distanceMatrix = distanceMatrix;
    }

    @Override
    public void run() {
        permute(array, i, length, waypoint);
    }


    /* Algoritm method use to determine which route is the best */
    public void permute(int[] array,int i,int length, Vector<Waypoint> mWaypoints)
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
            permute(array,i+1,length, mWaypoints);
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
        int i = 0;

        /* It loops through all the variables in given waypoint vector and then get distance from it
            */
       /* for(; i < waypoint.size() - 1; i++)
        {
            distance += getDistance(waypoint.get(array[i]).getLatitude(), waypoint.get(array[i]).getLongitude(), waypoint.get(array[i+1]).getLatitude(), waypoint.get(array[i+1]).getLongitude());
        }

        distance += getDistance(waypoint.get(array[i]).getLatitude(), waypoint.get(array[i]).getLongitude(), waypoint.get(array[0]).getLatitude(), waypoint.get(array[0]).getLongitude());
        */

        for(; i < waypoint.size() - 1; i++)
        {
            distance += distGet(waypoint.get(array[i]).getLocation(),waypoint.get(array[i+1]).getLocation());
        }

        distance += distGet(waypoint.get(array[i]).getLocation(),waypoint.get(array[0]).getLocation());

        /*
        This method will get the highest distance
         */

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

    /* Get the distance between two locations*/

    public double distGet(String orig, String dest)
    {
        double km = 0.0;
        int origins = getLoc(orig);
        int destins = getLoc(dest);


        JSONObject jsonObj;
        try {
            jsonObj = new JSONObject(distanceMatrix);
            JSONArray jsonArr = new JSONArray(jsonObj.getString("rows"));
            JSONObject jsonAdd = jsonArr.getJSONObject(origins);
            JSONArray jsonAddArr = new JSONArray(jsonAdd.getString("elements"));
            String distis = jsonAddArr.getJSONObject(destins).getJSONObject("distance").getString("value");
            km = Double.valueOf(distis);
            km = km/1000;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return km;
    }

    public int getLoc(String location)
    {
        for(int i = 0; i < waypoint.size(); i++)
        {
            if(location.equals(waypoint.get(i).getLocation()))
            {
                return i;
            }
        }
        return -1;
    }
}
