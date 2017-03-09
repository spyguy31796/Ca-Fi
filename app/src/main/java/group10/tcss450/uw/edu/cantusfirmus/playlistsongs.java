package group10.tcss450.uw.edu.cantusfirmus;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class playlistsongs extends AppCompatActivity {

    private static Handler handler;
    private ListView mylistView;
    private String[] mySongs;
    private Map<String, String> myMap;
    private playlistsongs myClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlistsongs);
        handler = new Handler();
        myClass = this;
        mylistView= (ListView) findViewById(R.id.playlist_song_list);
        myMap = new HashMap<String, String>();
        final String playlistId = getIntent().getStringExtra("PLAY_LIST_ID");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getPlayListSongs(playlistId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void getPlayListSongs(String playlistId) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().cookieJar((new JavaNetCookieJar(login.getCookieManager())))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType,"playlistId="+playlistId);
        Request request = new Request.Builder()
                .url("https://damp-anchorage-73052.herokuapp.com/getPlaylist")
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache")
                .build();
        final Response response = client.newCall(request).execute();
        final String jsonData = response.body().string();
        Log.d("add playlist message", jsonData);
        handler.post(new Runnable(){
            @Override
            public void run(){
                if(jsonData.contains("error")) {
                    Toast.makeText(playlistsongs.this,"Unable to load up playlist from database",Toast.LENGTH_SHORT).show();
                }else {
                    try {
                        JSONObject temp = new JSONObject(jsonData);
                        JSONArray songarray = temp.getJSONArray("songs");
                        int song_num = songarray.length();
                        mySongs = new String[song_num];
                        for (int i = 0; i < song_num; i++) {
                            JSONObject songs = songarray.getJSONObject(i);
                            String song_id = songs.getString("_id");
                            String song_name = songs.getString("title");
                            mySongs[i] = song_name;
                            myMap.put(song_name, song_id);
                        }
                        mylistView.setAdapter(new ArrayAdapter<String>
                                (myClass, android.R.layout.simple_expandable_list_item_1, mySongs));
//                        mylistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                                String playlistname = (String) parent.getItemAtPosition(position);
//                                String playlist_id = myMap.get(playlistname);
//                                Intent intent = new Intent(playlist.this, playlistsongs.class);
//                                intent.putExtra("PLAY_LIST_ID", playlist_id);
//                                startActivity(intent);
//                            }
//                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
