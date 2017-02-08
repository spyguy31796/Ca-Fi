package group10.tcss450.uw.edu.cantusfirmus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_music);
        Button b = ((Button)findViewById(R.id.search_btn));
        b.setOnClickListener(this);
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
        if(searchResultList!=null){
            Toast.makeText(find_music.this,searchResultList.get(0).toPrettyString(),Toast.LENGTH_LONG);
        }
                return null;
    }

    @Override
    public void onClick(View v) {
        try {
            YTsearch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
