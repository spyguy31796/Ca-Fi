package group10.tcss450.uw.edu.cantusfirmus;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class playlist extends AppCompatActivity {

    private static Handler handler;
    private Map<String, String> myMap;
    private String[] playlists;
    private ListView mylistView;
    private JSONArray jsonArray;
    private playlist myClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        myClass = this;
        mylistView = (ListView) findViewById(R.id.playlist_name);
        myMap = new HashMap<>();
        handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getUserPlaylists();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    /**
     * Generates the popup allowing a user to specify the new playlist name.
     * @param view Required Parameter
     */
    public void addPlaylist(View view) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(playlist.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_playlist, null);
        final EditText playlistName = (EditText) mView.findViewById(R.id.add_playlist_name);
        Button mAddPlaylist = (Button) mView.findViewById(R.id.add_playlist);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        mAddPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(playlistName.getText().toString().isEmpty()) {
                    Toast.makeText(playlist.this, "Please write a playlist name", Toast.LENGTH_SHORT).show();
                } else {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String playlist_name = playlistName.getText().toString();
                                try {
                                    addUserPlaylists(playlist_name);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
            }
        });
    }

    /**
     * Retrieves all of the user's playlists.
     * @throws IOException Thrown if there is an issue connecting to the server or an issue with the response.
     * @throws JSONException Thrown if the data received can not be correctly parsed in JSON.
     */
    private void getUserPlaylists() throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient.Builder().cookieJar(new JavaNetCookieJar(login.getCookieManager()))
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url("https://damp-anchorage-73052.herokuapp.com/userPlaylists")
                .get()
                .addHeader("cache-control", "no-cache")
                .build();
        Response response = client.newCall(request).execute();
        String jsonData = response.body().string();
        if (!jsonData.startsWith("{")) {
            jsonData = "{playlists:" + jsonData + "}";
            JSONObject temp = new JSONObject(jsonData);
            jsonArray = temp.getJSONArray("playlists");
            int playlist_number = jsonArray.length();
            playlists = new String[playlist_number];
            handler.post(new Runnable(){
                @Override
                public void run(){
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String playlist_name = null;
                        try {
                            playlist_name = jsonArray.getJSONObject(i).getString("name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String playlist_id = null;
                        try {
                            playlist_id = jsonArray.getJSONObject(i).getString("_id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        playlists[i] = playlist_name;
                        myMap.put(playlist_name, playlist_id);
                    }
                    mylistView.setAdapter(new ArrayAdapter<String>(myClass,
                            android.R.layout.simple_list_item_1, playlists));
                    mylistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String playlistname = (String) parent.getItemAtPosition(position);
                                String playlist_id = myMap.get(playlistname);
                                Intent intent = new Intent(playlist.this, playlistsongs.class);
                                intent.putExtra("PLAY_LIST_ID", playlist_id);
                                startActivity(intent);
                        }
                    });
                }
            });
        }
    }

    /**
     * Adds a new playlist for the user to the server.
     * @param playlist_name The name of te playlist.
     * @throws IOException Thrown if there is an issue connecting to the server or an issue with the response.
     * @throws JSONException Thrown if the data received can not be correctly parsed in JSON.
     */
    private void addUserPlaylists(String playlist_name) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient.Builder().cookieJar((new JavaNetCookieJar(login.getCookieManager())))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType,"playlistName="+playlist_name);
        Request request = new Request.Builder()
                .url("https://damp-anchorage-73052.herokuapp.com/createPlaylist")
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
                    Toast.makeText(playlist.this,"Playlist Already Exists",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(playlist.this,"Playlist Successfully Added",Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(getIntent());
                }
            }
        });
    }
}
