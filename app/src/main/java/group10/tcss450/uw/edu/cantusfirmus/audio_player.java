package group10.tcss450.uw.edu.cantusfirmus;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.widget.SimpleCursorAdapter;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static group10.tcss450.uw.edu.cantusfirmus.R.id.playlist_name;

/**
 * This class holds the audio player and the components that make up the audio player activity.
 * @author Jabo Johnigan
 * @version Feb 1 2017
 */
public class audio_player extends ListActivity {

    private static final int UPDATE_FREQUENCY = 500;
    private static final int STEP_VALUE = 4000;

    private MediaCursorAdapter mediaAdapter = null;
    private TextView selelctedFile = null;
    private SeekBar mySeekbar = null;
    private MediaPlayer mp = null;
    private String youtubeId = "";
    private String imgurl = "";
    private String myTitle = "";
    private ImageButton playButton = null;
    private ImageButton prevButton = null;
    private ImageButton nextButton = null;
    private ImageButton addToLibraryButton = null;
    private ToggleButton repeatButton = null;
    private audio_player myClass = this;
    private String[] playlists;
    private String addPlaylistSong;
    private String selectedPlaylist;

    private boolean isMusicPlaying = true;
    private String currentFile = "Streaming Audio";
    private boolean isSeekbarMoving = false;
    private MediaSessionCompat ms;
    private notification nf;
    private boolean fileSelected=false;

    private final Handler handler = new Handler();

    private final Runnable updatePositionRunnable = new Runnable() {
        public void run() {
            updatePosition();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        ms = new MediaSessionCompat(this,"Music");
        selelctedFile = (TextView) findViewById(R.id.selectedFile);
        mySeekbar = (SeekBar) findViewById(R.id.songSeekbar);
        playButton = (ImageButton) findViewById(R.id.playbtn);
        prevButton = (ImageButton) findViewById(R.id.prevSeek);
        nextButton = (ImageButton) findViewById(R.id.fwdSeek);
        repeatButton = (ToggleButton) findViewById(R.id.repeat);
        addToLibraryButton = (ImageButton) findViewById(R.id.addToLibrary);
        startService(new Intent(this,notificationRemover.class));
        mp = new MediaPlayer();
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mp.setOnCompletionListener(onCompletionListener);
        mp.setOnErrorListener(onErrorListener);
        mySeekbar.setOnSeekBarChangeListener(seekBarChangedListener);
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission has been granted for thee who wish to ROCK AND ROLL");
            //File write logic here
            Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            if (null != cursor) {

                cursor.moveToFirst();
                mediaAdapter = new MediaCursorAdapter(this, R.layout.song_item, cursor);

                setListAdapter(mediaAdapter);

                playButton.setOnClickListener(onButtonClick);
                nextButton.setOnClickListener(onButtonClick);
                prevButton.setOnClickListener(onButtonClick);
                repeatButton.setOnClickListener(onButtonClick);
                addToLibraryButton.setOnClickListener(onButtonClick);
            }

        }
        Intent i = getIntent();
        Bundle b = i.getExtras();
        try{
            fileSelected = true;
            String url = b.getString("web");
            i.removeExtra("web");
            String title = b.getString("name");
            i.removeExtra("name");
            String youtube = b.getString("youtubeId");
            i.removeExtra("youtubeId");
            String img = b.getString("imgurl");
            i.removeExtra("imgurl");
            String songTitle = b.getString("title");
            i.removeExtra("title");
            if(songTitle!=null){
                currentFile = songTitle;
            }
            //Log.d("url",url);
            if(url!=null) {
                startPlay(url);
            }
            if (youtube!=null) {
                youtubeId = youtube;
                imgurl = img;
                myTitle = songTitle;
                addToLibraryButton.setVisibility(View.VISIBLE);
            }
            //startPlay(networkAudio.body().byteStream());
        }catch(Exception ex){
            //Log.d("Exception",ex.getMessage());
            ex.printStackTrace();
        }
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
     * This method handles when an item in the list of music is clicked.
     * @param list the listView containing the songs
     * @param view the listView obj
     * @param position which position in the list the current item is
     * @param id the id associated with the particular listview item
     */
    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);
        addToLibraryButton.setVisibility(View.INVISIBLE);
        fileSelected = true;
        currentFile = (String) view.getTag();
        find_music.setIcon(null);
        startPlay(currentFile);
    }

    /**
     * This method handles what happens to the audio_player once you exit the audio_player activity.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(nf!=null) {
            nf.closeNotification();
        }
        handler.removeCallbacks(updatePositionRunnable);
        mp.stop();
        mp.reset();
        mp.release();

        mp = null;
    }

    /**
     * This method handles what happens when you select a file from the list.
     * @param file a file to be used for playing audio
     */
    private void startPlay(String file) {
        nf = new notification(getApplicationContext());
        Log.i("Selected: ", file);
        selelctedFile.setText(file);
        mySeekbar.setProgress(0);

        mp.stop();
        mp.reset();

        try {
            mp.setDataSource(file);
            mp.prepare();
            mp.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mySeekbar.setMax(mp.getDuration());
        playButton.setImageResource(android.R.drawable.ic_media_pause);

        updatePosition();

        isMusicPlaying = true;
    }


    /**
     * This method handles what happens when stop playing music in the audio_player.
     */
    private void stopPlay() {
        if(!mp.isLooping()) {
            if(nf!=null){
                nf.closeNotification();
                nf = new notification(this);
            }
            mp.stop();
            mp.reset();
            playButton.setImageResource(android.R.drawable.ic_media_play);
            handler.removeCallbacks(updatePositionRunnable);
            isMusicPlaying = false;
        }
        mySeekbar.setProgress(0);
    }

    /**
     * This method updates the position of the slider and handles the time it takes to update each
     * clock cycle.
     */
    private void updatePosition() {
        handler.removeCallbacks(updatePositionRunnable);

        mySeekbar.setProgress(mp.getCurrentPosition());

        handler.postDelayed(updatePositionRunnable, UPDATE_FREQUENCY);
    }


    /**
     * An onClickListener view obj that handles the case when the play, fwdseek, and prevseek buttons
     * are clicked.
     */
    private View.OnClickListener onButtonClick = new View.OnClickListener() {

        /**
         * This overridden method will take the view and check which was pressed and launch another
         * method accordingly.
         * @param v the current view/button being pressed
         */
        @Override
        public void onClick(View v) {
            if(nf==null){
                nf = new notification(getApplicationContext());
            }
            switch (v.getId()) {
                case R.id.playbtn: {
                    PlayPauseHandler();
                    break;
                }
                case R.id.fwdSeek: {
                    SkipForwardHandler();
                    break;
                }
                case R.id.prevSeek: {
                   SkipBackHandler();
                    break;
                }
                case R.id.repeat:
                    if(((ToggleButton)v).isChecked()){
                        mp.setLooping(true);
                    }else{
                        mp.setLooping(false);
                    }
                    break;
                case R.id.addToLibrary:
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(audio_player.this);
                    View mView = getLayoutInflater().inflate(R.layout.dialog_add_to_playlist, null);
                    final Spinner spinner = (Spinner) mView.findViewById(R.id.playlist_spinner);
                    //String text = spinner.getSelectedItem().toString();
                    spinner.setAdapter(new ArrayAdapter<String>(myClass,
                            android.R.layout.simple_spinner_dropdown_item, playlists));
                    Button mAddPlaylist = (Button) mView.findViewById(R.id.add_song_to_playlist);
                    mBuilder.setView(mView);
                    final AlertDialog dialog = mBuilder.create();
                    dialog.show();
                    mAddPlaylist.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            selectedPlaylist = spinner.getSelectedItem().toString();
                            if(selectedPlaylist.isEmpty()) {
                                Toast.makeText(audio_player.this, "Please Create A Playlist", Toast.LENGTH_SHORT).show();
                            } else {
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            addSongToLibrary();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                thread.start();
                            }
                        }
                    });
                    break;
            }
        }
    };

    private void getUserPlaylists() throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient.Builder().cookieJar(new JavaNetCookieJar(login.getCookieManager()))
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
        //Log.d("What cookie to use",login.getCookieManager().getCookieStore().getCookies().get(0).getValue());
        Request request = new Request.Builder()
                .url("https://damp-anchorage-73052.herokuapp.com/userPlaylists")
                .get()
                .addHeader("cache-control", "no-cache")
                .build();
        Response response = client.newCall(request).execute();
        String jsonData = response.body().string();
        if (!jsonData.startsWith("{")) {
            jsonData = "{playlists:" + jsonData + "}";
            Log.d("JSON DATA", jsonData);
            JSONObject temp = new JSONObject(jsonData);
            final JSONArray jsonArray = temp.getJSONArray("playlists");
            int playlist_number = jsonArray.length();
            playlists = new String[playlist_number];
            handler.post(new Runnable(){
                @Override
                public void run(){
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String playlist_name = null;
                        try {

                            playlist_name = jsonArray.getJSONObject(i).getString("name");
                            Log.d("playlistName", playlist_name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        playlists[i] = playlist_name;
                    }
                }
            });
        }
    }

    private void addSongToLibrary() throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient.Builder().cookieJar((new JavaNetCookieJar(login.getCookieManager())))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "id="+myTitle+"&imgurl="+imgurl+"&src="+youtubeId+"&title="+myTitle);
        Request request = new Request.Builder()
                .url("https://damp-anchorage-73052.herokuapp.com/addToLibrary")
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache")
                .build();
        final Response response = client.newCall(request).execute();
        final String jsonData = response.body().string();
        //Log.d("add playlist message", jsonData);
        handler.post(new Runnable(){
            @Override
            public void run(){
                if(jsonData.contains("error")) {
                    Toast.makeText(audio_player.this,"Cannot be added to the database library",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(audio_player.this,"Song Successfully Added to Library",Toast.LENGTH_SHORT).show();
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getUserLibrary();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
            }
        });
    }

    public void getUserLibrary() throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient.Builder().cookieJar(new JavaNetCookieJar(login.getCookieManager()))
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
        //Log.d("What cookie to use",login.getCookieManager().getCookieStore().getCookies().get(0).getValue());
        Request request = new Request.Builder()
                .url("https://damp-anchorage-73052.herokuapp.com/userLibrary")
                .get()
                .addHeader("cache-control", "no-cache")
                .build();
        Response response = client.newCall(request).execute();
        String jsonData = response.body().string();
        if (!jsonData.startsWith("{")) {
            jsonData = "{songs:" + jsonData + "}";
            Log.d("JSON DATA", jsonData);
            JSONObject temp = new JSONObject(jsonData);
            final JSONArray jsonArray = temp.getJSONArray("songs");
            handler.post(new Runnable(){
                @Override
                public void run(){
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String song_id = null;
                        String song_name = null;
                        try {

                            song_id = jsonArray.getJSONObject(i).getString("_id");
                            if (jsonArray.getJSONObject(i).has("title")) {
                                song_name = jsonArray.getJSONObject(i).getString("title");
                                if (song_name.equals(myTitle)) {
                                    addPlaylistSong = song_id;
                                    Log.d("songName", song_name);
                                }
                            }
                            Log.d("playlistName", song_id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    addSongToPlaylist();
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
    }

    public void addSongToPlaylist() throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().cookieJar((new JavaNetCookieJar(login.getCookieManager())))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "playlistName="+selectedPlaylist+"&songId="+addPlaylistSong);
        Request request = new Request.Builder()
                .url("https://damp-anchorage-73052.herokuapp.com/addSongToPlaylist")
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache")
                .build();
        final Response response = client.newCall(request).execute();
        final String jsonData = response.body().string();
        //Log.d("add playlist message", jsonData);
        handler.post(new Runnable(){
            @Override
            public void run(){
                if(jsonData.contains("error")) {
                    //Toast.makeText(audio_player.this,"Cannot be added to" + selectedPlaylist,Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(audio_player.this,"Song Successfully Added to " + selectedPlaylist,Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void PlayPauseHandler(){
        fileSelected=false;
        if(nf==null){
            nf = new notification(getApplicationContext());
        }
        if (mp.isPlaying()) {
            handler.removeCallbacks(updatePositionRunnable);
            mp.pause();
            playButton.setImageResource(android.R.drawable.ic_media_play);
            nf.closeNotification();
            nf = new notification(getApplicationContext());
        } else {
            if (isMusicPlaying) {
                mp.start();
                playButton.setImageResource(android.R.drawable.ic_media_pause);
                nf.closeNotification();
                nf = new notification(getApplicationContext());
                updatePosition();
            } else {
                nf.closeNotification();
                nf = new notification(getApplicationContext());
                startPlay(currentFile);
            }
        }
    }
    private void SkipBackHandler(){
        int seekto = mp.getCurrentPosition() - STEP_VALUE;

        if (seekto < 0)
            seekto = 0;

        mp.pause();
        mp.seekTo(seekto);
        mp.start();
    }
    private void SkipForwardHandler(){
        int seekto = mp.getCurrentPosition() + STEP_VALUE;

        if (seekto > mp.getDuration())
            seekto = mp.getDuration();

        mp.pause();
        mp.seekTo(seekto);
        mp.start();
    }
    /**
     * An onCompletionListener obj to handle what happens when the media player completes playing
     * music or is stopped.
     */
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            stopPlay();
        }
    };

    /**
     * An onErrorListener obj to handle errors thrown by the media player, if any.
     */
    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {

            return false;
        }
    };


    private SeekBar.OnSeekBarChangeListener seekBarChangedListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekbarMoving = false;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekbarMoving = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (isSeekbarMoving) {
                mp.seekTo(progress);

                Log.i("OnSeekBarChangeListener", "onProgressChanged");
            }
        }
    };


    /**
     * This private inner class is the media cursor adapter that gets the song information and links
     * it with the media player playback to display the information.
     * @author Jabo Johnigan
     * @version Feb 1, 2017
     */
    private class MediaCursorAdapter extends SimpleCursorAdapter {

        /**
         * Constructs a media cursor adapter that parses the information stored from whatever data
         * stream its given.
         * @param context a context obj to get information about the current state of the application
         * @param layout layout of the current view
         * @param c the cursor
         */
        public MediaCursorAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c,
                    new String[]{MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.TITLE, MediaStore.Audio.AudioColumns.DURATION},
                    new int[]{R.id.displayname, R.id.title, R.id.duration});
        }

        /**
         * This method handles the formatting of the audio track in the list view.
         * @param view current view of the listView
         * @param context the context of the application
         * @param cursor the current media cursor
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView name = (TextView) view.findViewById(R.id.displayname);
            TextView duration = (TextView) view.findViewById(R.id.duration);

            name.setText(cursor.getString(
                    cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)));

            title.setText(cursor.getString(
                    cursor.getColumnIndex(MediaStore.MediaColumns.TITLE)));

            long durationInMs = Long.parseLong(cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));

            double durationInMin = ((double) durationInMs / 1000.0) / 60.0;

            durationInMin = new BigDecimal(Double.toString(durationInMin)).setScale(2, BigDecimal.ROUND_UP).doubleValue();
            duration.setText("" + durationInMin);
            view.setTag(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
        }

        /**
         * Creates a new list based off the the specs in the song_item xml
         * @param context the current context of the application
         * @param cursor the media player cursor
         * @param parent the encapsulating view
         * @return the listView
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.song_item, parent, false);

            bindView(v, context, cursor);

            return v;
        }
    }

    /***
     * Service That Removes Notification if the App is Force Closed.
     */
    public static class notificationRemover extends Service {
        public notificationRemover() {
            super();
        }
        @Override
        public int onStartCommand(Intent intent, int flags, int startId){
           return START_STICKY;
        }
        @Override
        public void onCreate(){
            HandlerThread thread = new HandlerThread("ServiceStartArguments",
                THREAD_PRIORITY_BACKGROUND);
            thread.start();
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onTaskRemoved(Intent rootIntent) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(2);
            stopSelf();
        }
    }

    private class notification {
        private NotificationManager nm;
        private NotificationCompat.Builder nb;

        public notification(Context parent){
            nb = new NotificationCompat.Builder(parent);
            nb.setContentText(currentFile);
            if(find_music.getIcon()!=null){
                nb.setLargeIcon(find_music.getIcon());
            }
            nb.setContentIntent(PendingIntent.getActivity(parent, 0, new Intent(parent, audio_player.class), 0));
            nb.setOngoing(true);
            Intent skipBack = new Intent(parent,audio_player.class);
            skipBack.putExtra("Action","Back");
            nb.addAction(android.R.drawable.ic_media_rew,"Back",PendingIntent.getActivity(parent,2,skipBack,0));
            Intent pauseIntent = new Intent(parent,audio_player.class);
            pauseIntent.putExtra("Action","Pause");
            if(!mp.isPlaying()&&!fileSelected) {
                nb.addAction(android.R.drawable.ic_media_play, "Play", PendingIntent.getActivity(parent, 1, pauseIntent, 0));
            }else{
                nb.addAction(android.R.drawable.ic_media_pause, "Pause", PendingIntent.getActivity(parent, 1, pauseIntent, 0));
            }
            Intent skipForward = new Intent(parent,audio_player.class);
            skipForward.putExtra("Action","Front");
            nb.addAction(android.R.drawable.ic_media_ff,"Front",PendingIntent.getActivity(parent,3,skipForward,0));
            nb.setStyle(new NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1)
                    .setMediaSession(ms.getSessionToken()));
            nb.setSmallIcon(android.R.drawable.ic_media_play);
            nm = (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(2, nb.build());
        }
        public void closeNotification(){
            nm.cancel(2);
        }
    }
    @Override
    protected void onNewIntent (Intent intent) {
        if(intent.hasExtra("Action")) {
            switch (intent.getStringExtra("Action")) {
                case "Pause":
                    PlayPauseHandler();
                    break;
                case "Back":
                    SkipBackHandler();
                    break;
                case "Front":
                    SkipForwardHandler();
                    break;
                default:
                    break;
            }
        }
    }
}
