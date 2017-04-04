package com.rentit.priyath.chatapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    ArrayList<chatData> chatDatas;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    chatadpter adapter;
    EditText toMessage;
    Button sentButton;
    MyReceiver myReceiver;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.ChatRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        chatDatas = new ArrayList<>();
        chatDatas = getChatData();
        adapter = new chatadpter(chatDatas,this);
        recyclerView.setAdapter(adapter);

        if(chatDatas.size()>=1)
        recyclerView.smoothScrollToPosition(chatDatas.size()-1);

        toMessage = (EditText)findViewById(R.id.toMessage);
        sentButton = (Button)findViewById(R.id.sentButton);
        sentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatData d = new chatData();
                d.message = String.valueOf(toMessage.getText());
                d.name = "Priyath";
                d.type = 1;
                chatDatas.add(d);
                adapter.notifyDataSetChanged();
                toMessage.setText("");
                recyclerView.smoothScrollToPosition(chatDatas.size()-1);
                try {
                    saveChatData(chatDatas);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new SentPostRequest().execute("priyath",d.message,"dfdfdfdfdf");

            }
        });
        Intent intent = new Intent(this,getChatService.class);
        startService(intent);


        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getChatService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
    }


    void saveChatData(ArrayList<chatData> data) throws IOException {
        FileOutputStream out = this.openFileOutput("data",MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(data);
        oos.close();
        out.close();
        Toast.makeText(this,"saving Data",Toast.LENGTH_LONG).show();
    }
    ArrayList<chatData> getChatData(){
        ArrayList<chatData> chdata = new ArrayList<>();
        try {

            FileInputStream in = openFileInput("data");
            ObjectInputStream ois = new ObjectInputStream(in);

            chdata = (ArrayList<chatData>) ois.readObject();
            ois.close();
            in.close();
            //Toast.makeText(this,"getting Data"+chatDatas.size(),Toast.LENGTH_LONG).show();

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return chdata;
//;
    }

    public class SentPostRequest extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {

            try{
                URL url = new URL("https://rentitapi.herokuapp.com/chat_to");
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("name",params[0]);
                postDataParams.put("message",params[1]);
                postDataParams.put("toid",params[2]);

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(1500);
                conn.setConnectTimeout(1500);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os,"UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";
                    while((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    in.close();
                    return sb.toString();
                }
                else {
                    return "false : " + responseCode;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
        }
    }

    public String getPostDataString(JSONObject params) throws Exception{

        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator<String> itr = params.keys();
        while(itr.hasNext()) {
            String key = itr.next();
            Object value = params.get(key);
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(),"UTF-8"));
        }
        return result.toString();
    }

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            int datapassed = arg1.getIntExtra("DATAPASSED", 0);
            getChatData();
            ArrayList<chatData> chdata;
            chdata = getChatData();
            for(int i=(chdata.size()-datapassed);i<chdata.size();i++){
                chatDatas.add(chdata.get(i));
            }
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(chatDatas.size()-1);


        }
    }



}
