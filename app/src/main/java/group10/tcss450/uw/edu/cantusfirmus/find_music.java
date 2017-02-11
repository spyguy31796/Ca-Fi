package group10.tcss450.uw.edu.cantusfirmus;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.List;


public class find_music extends AppCompatActivity implements View.OnClickListener {
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_music);
        Button b = ((Button)findViewById(R.id.search_btn));
        b.setOnClickListener(this);
        handler = new Handler();
    }
    public String YTsearch() throws IOException {
        YouTube youTube;
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
            handler.post(new Runnable(){
               @Override
                public void run(){
                   String urlString = "https://www.youtube.com/watch?v="+idString;
                   ClipboardManager clipboard = (ClipboardManager)
                           getSystemService(Context.CLIPBOARD_SERVICE);
                   ClipData clip = ClipData.newPlainText("web-address",urlString);
                   clipboard.setPrimaryClip(clip);
                   Toast.makeText(find_music.this,"Address Copied to Clipboard!",Toast.LENGTH_LONG).show();
                   //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                   //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   Intent intent = new Intent(find_music.this,audio_player.class);
                   Bundle b = new Bundle();
                   b.putString("web","https://www.youtube.com/watch?v="+idString);
                   intent.putExtras(b);
                   startActivity(intent);
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
                return null;
    }

    @Override
    public void onClick(final View v) {
        v.setEnabled(false);
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
