package com.isaac.smartdrawer;


import com.isaac.smartdrawer.ContentDbSchema.ContentsTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class Client {
    private static String sHost;
    private static int sPort ;

    private static List sNullList = Arrays.asList(null,null);

    private Socket mServer;
    private PrintWriter mWriter;
    private BufferedReader mReader;

    private static class Singleton {
        private static final Client sClient = new Client();
    }

    public static Client instance() {
        return Singleton.sClient;
    }

    public static void setConnection(String addrString) {
        String[] addr = addrString.split(":");
        sHost = addr[0];
        sPort = Integer.parseInt(addr[1]);
    }

    public List<Content> getAllContents() {
        try
        {
            ArrayList<Content> contents = new ArrayList<>();
            for (JSONObject json : send("{\"source\":\"client\",\"option\":\"get\"}")) {
                if (json == null) return sNullList;
                contents.add(new Content(json.getString(ContentsTable.Cols.ID),
                        json.getString(ContentsTable.Cols.NAME),
                        json.getString(ContentsTable.Cols.CATEGORY),
                        json.getInt(ContentsTable.Cols.EXIST) == 1
                ));
            }
            return contents;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getIds() {
        List<String> ids = new ArrayList<>();
        for (JSONObject json : send("{\"source\":\"client\", \"option\":\"get_change\"}")) {
            if (json == null) return null;
            try {
                String id = json.getString("id");
                if (id.length() == 1) return null;
                ids.add(id);
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }

        return ids;
    }

    public List<History.Child> getHistory() {
        List<History.Child> histories = new ArrayList<>();

        for (JSONObject json : send("{\"source\":\"client\",\"option\":\"history\"}")) {
            if (json == null) return sNullList;
            try {
                histories.add(new History.Child(json.getString(History.ID), json.getString(History.OPTION), History.tDateFormat(json.getString(History.DATE))));
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
        return histories;
    }

    public List<History.Child> getHistory(String id) {
        List<History.Child> histories = new ArrayList<>();

        for (JSONObject json : send("{\"source\":\"client\",\"option\":\"history\",\"id\":\"" + id + "\"}")) {
            if (json == null) return null;
            try {
                histories.add(new History.Child(json.getString(History.ID), json.getString(History.OPTION), History.tDateFormat(json.getString(History.DATE))));
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
        return histories;
    }

    public boolean delete(String id) {
        String str = null;
        try
        {
            JSONObject json = send("{\"source\":\"client\",\"option\":\"delete\",\"id\":\""+id+"\"}").get(0);
            if (json == null) return false;
            str = json.getString("result");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return "true".equals(str);
    }

    public boolean update(String id, String key, String value) {
        JSONObject json = new JSONObject();
        String str = "";
        try
        {
            json.put("source", "client");
            json.put("option", "update");
            json.put("id", id);
            json.put(key, value);
            JSONObject result = send(json.toString()).get(0);
            if (result == null) return false;
            str = result.getString("result");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return str.equals("true");
    }

    private List<JSONObject> send(String json) {
        if (!init()) return sNullList;
        String str;
        ArrayList<JSONObject> jsons = new ArrayList<>();
        try
        {
            mWriter.println(new JSONObject(json));
            while ((str = mReader.readLine()) != null)
            {
                if (str.equals("true")) jsons.add(new JSONObject("{\"result\":\"true\"}"));
                else jsons.add(new JSONObject(str));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        finally {
            closeAll();
        }
        return jsons;
    }

    private boolean init() {
        try
        {
            mServer = new Socket(sHost, sPort);
            mWriter = new PrintWriter(mServer.getOutputStream(), true);
            mReader = new BufferedReader(new InputStreamReader(mServer.getInputStream()));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return mWriter!=null;
    }

    private void closeAll() {
        try
        {
            mReader.close();
            mWriter.close();
            mServer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

