package group10.tcss450.uw.edu.cantusfirmus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
        myMap = new HashMap<>();
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

    /**
     * Retrieves the songs saved into a playlist from the server.
     * @param playlistId the id of the playlist to pull songs from.
     * @throws IOException Thrown if there is an issue connecting to the server or an issue with the response.
     */
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
                            String song_src = songs.getString("src");
                            String song_name = songs.getString("title");
                            mySongs[i] = song_name;
                            myMap.put(song_name, song_src);
                        }
                        mylistView.setAdapter(new ArrayAdapter<String>
                                (myClass, android.R.layout.simple_expandable_list_item_1, mySongs));
                        mylistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String playlistname = (String) parent.getItemAtPosition(position);
                                final String playlist_src = myMap.get(playlistname);
                                final ProgressDialog progressDialog = new ProgressDialog(playlistsongs.this);
                                progressDialog.setTitle("Loading song from playlist");
                                progressDialog.setMessage("Loading your playlist song...");
                                progressDialog.show();
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            playMusic(playlist_src, progressDialog);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                thread.start();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Locates the music requested from the playlist and sends that intent to the audio player.
     * @param playlist_src The playlist the song comes from.
     * @param progressDialog The dialog box showing the progress of the load.
     * @throws IOException Thrown if there is an issue connecting to the server or an issue with the response.
     */
    public void playMusic(String playlist_src, final ProgressDialog progressDialog) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "youtubeID="+playlist_src);
        Request request = new Request.Builder()
                .url("https://damp-anchorage-73052.herokuapp.com/stream_yt")
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "4014accd-f315-a762-d57b-25613deb8758")
                .build();
        Response response = client.newCall(request).execute();
        final File file = new File(getFilesDir(),"cache.dat");
        OutputStream out = new FileOutputStream(file);
        byte buffer[] = new byte[6*1024];
        int length;
        while((length = response.body().byteStream().read(buffer))!=-1){
            out.write(buffer,0,length);
        }
        out.flush();
        out.close();
        handler.post(new Runnable(){
            /***
             * The commented out code is for sending the video id to the youtube app instead of listening natively in the app.
             * The code remains here in the event we decide to implement that feature again.
             */
            @Override
            public void run(){
                progressDialog.dismiss();
                Intent intent = new Intent(playlistsongs.this,audio_player.class);
                Bundle b = new Bundle();
                b.putString("web",file.getAbsolutePath());
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }


}
