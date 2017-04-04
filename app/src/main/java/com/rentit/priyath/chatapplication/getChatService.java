package com.rentit.priyath.chatapplication;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by priyath on 02-04-2017.
 */

public class getChatService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler handler;
    HttpURLConnection conn;
    String postDataParams;
    chatData chatdata;
    ArrayList<chatData> chatDatas;
    int length;
    final static String MY_ACTION = "MY_ACTION";

    public int onStartCommand(Intent intent, int flags, int startId) {
        //new MyAsyncTask().execute(0);
        handler = new Handler();
        chatDatas = new ArrayList<>();
        runnable.run();

        return START_STICKY;
    }



    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            chatStatus.newDataAdded=false;
            new MyAsyncTask().execute(0);
            handler.postDelayed(runnable,5000);
        }

    };


    public class MyAsyncTask extends AsyncTask<Integer,Integer,Integer>{

        @Override
        protected Integer doInBackground(Integer... params) {

            try {
                postData();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  0;
        }
        void postData() throws IOException {
            String link = "http://192.168.43.87:5000/chat_from";
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            InputStream in = new BufferedInputStream(connection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder response = new StringBuilder();
            String line;
            while((line = reader.readLine())!=null){
                response.append(line);

            }
            try {
                //JSONObject jsonObject = new JSONObject(response.toString());

                JSONArray jsonArray = new JSONArray(response.toString());
                JSONObject js;
                if(jsonArray.length()>0) {

                    for (int i = 0; i < jsonArray.length(); i++) {
                        chatdata = new chatData();
                        js = jsonArray.getJSONObject(i);

                        chatdata.message = js.getString("message");
                        chatdata.name = js.getString("name");
                        Log.i("name", chatdata.name);
                        chatdata.type = 0;
                        chatDatas.add(chatdata);
                        //adapter.notifyItemInserted(chatDatas.size()-1);

                    }
                }
                length = jsonArray.length();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        protected void onPreExecute(){
            getChatData();
        }

        protected void onPostExecute(Integer page){
            if(length!=0) {
                try {
                    saveChatData(chatDatas);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void saveChatData(ArrayList<chatData> data) throws IOException {
        FileOutputStream out = this.openFileOutput("data",MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(data);
        oos.close();
        out.close();
        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("DATAPASSED",length);
        sendBroadcast(intent);

        Toast.makeText(this,"saving Data",Toast.LENGTH_LONG).show();
    }
    void getChatData(){
        //chatDatas.clear();
        try {

            FileInputStream in = openFileInput("data");
            ObjectInputStream ois = new ObjectInputStream(in);

            chatDatas = (ArrayList<chatData>) ois.readObject();
            ois.close();
            in.close();
            //
            //Toast.makeText(this,"getting Data"+chatDatas.size(),Toast.LENGTH_LONG).show();

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

//;
    }
}
