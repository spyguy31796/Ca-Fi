package group10.tcss450.uw.edu.cantusfirmus;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/***
 * This activity is where music not in the local library can be searched for. Currently it only supports youtube.
 */
public class find_music extends AppCompatActivity implements View.OnClickListener {
    /***
     * Handler allowing other threads to touch the UI thread.
     */
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_music);
        Button b = ((Button)findViewById(R.id.search_btn));
        b.setOnClickListener(this);
        handler = new Handler();
    }

    /***
     * This method handles the interactions with the Youtube api. Searching for the first music video related to your search terms.
     * Then the video id is passed to our server where the video is processed and the mp3 is sent back in a response. The mp3 is saved in the
     * cache and transitioned to the music player where it is played.
     * @throws IOException If there is a server issues, an IOException can be thrown.
     */
    public void YTsearch() throws IOException {
        YouTube youTube;
        //Needs to be removed from the source code.
        String apiKey = "AIzaSyBktpICzt4gZSd08s44i1UmbsQBKoxEXDE";
        youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer(){
            public void initialize(HttpRequest request) throws IOException{

            }
        }).setApplicationName("youtube-search").build();
        String query = (((EditText)(findViewById(R.id.searchField))).getText().toString());
        YouTube.Search.List search = youTube.search().list("id,snippet");
        search.setKey(apiKey);
        search.setQ(query);
        search.setType("music");
        search.setFields("items(id/videoId)");
        search.setMaxResults((long)1);
        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResultList = searchResponse.getItems();
        if(searchResultList!=null&&searchResultList.size()>0){
            final String idString = searchResultList.get(0).get("id").toString().split(":")[1].replace("\"","").replace("}","");
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "youtubeID="+idString);
            Request request = new Request.Builder()
                    .url("https://hidden-scrubland-70822.herokuapp.com/stream_yt")
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "68f30572-d738-00dd-0ffd-bdb134c10bae")
                    .build();
            Response response = client.newCall(request).execute();
            final File file = new File(getCacheDir(),"cacheFileAppeal.sr1");
            OutputStream out = new FileOutputStream(file);
            byte buffer[] = new byte[6*1024];
            int length;
            while((length = response.body().byteStream().read(buffer))!=-1){
                out.write(buffer,0,length);
            }
            out.flush();
            out.close();
            //audio_player.setNetworkAudio(response);
            handler.post(new Runnable(){
                /***
                 * The commented out code is for sending the video id to the youtube app instead of listening natively in the app.
                 * The code remains here in the event we decide to implement that feature again.
                 */
               @Override
                public void run(){
                   //String urlString = "https://www.youtube.com/watch?v="+idString;
                   //ClipboardManager clipboard = (ClipboardManager)
                   //        getSystemService(Context.CLIPBOARD_SERVICE);
                   //ClipData clip = ClipData.newPlainText("web-address",urlString);
                   //clipboard.setPrimaryClip(clip);
                   //Toast.makeText(find_music.this,"Address Copied to Clipboard!",Toast.LENGTH_LONG).show();
                   Intent intent = new Intent(find_music.this,audio_player.class);
                   Bundle b = new Bundle();
                   b.putString("web",file.getAbsolutePath());
                   intent.putExtras(b);
                   findViewById(R.id.search_btn).setEnabled(true);
                   findViewById(R.id.progress).setVisibility(View.GONE);
                   startActivity(intent);
                   //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                   //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   //((TextView)findViewById(R.id.displaySearch)).setText(urlString);
               }
            });
        }else{
            handler.post(new Runnable(){
                @Override
                public void run(){
                    findViewById(R.id.search_btn).setEnabled(true);
                }
            });
        }
    }

    /***
     * Handles the search button press, creating another thread for the server communication and search function.
     * @param v the button pressed.
     */
    @Override
    public void onClick(final View v) {
        v.setEnabled(false);
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    YTsearch();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
