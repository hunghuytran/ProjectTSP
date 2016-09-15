package com.arcada.projecttsp;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetLocation extends AsyncTask<String, Void, String>
{
    @Override
    protected String doInBackground(String[] urlString)
    {
        String JSONString = new String();

        try {

            URL url = new URL(urlString[0]);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            StringBuilder sb = new StringBuilder();

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String nextLine = "";

            while ((nextLine = reader.readLine()) != null) {
                sb.append(nextLine);
            }
            JSONString = sb.toString();

            return JSONString;
        }

        catch(Exception e)
        {

        }

        return null;
    }
}

