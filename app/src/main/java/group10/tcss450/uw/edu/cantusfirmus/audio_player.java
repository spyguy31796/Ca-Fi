package group10.tcss450.uw.edu.cantusfirmus;

import android.app.ListActivity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigDecimal;

import static android.content.ContentValues.TAG;

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
    private ImageButton playButton = null;
    private ImageButton prevButton = null;
    private ImageButton nextButton = null;

    private boolean isMusicPlaying = true;
    private String currentFile = "";
    private boolean isSeekbarMoving = false;

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

        selelctedFile = (TextView) findViewById(R.id.selectedFile);
        mySeekbar = (SeekBar) findViewById(R.id.songSeekbar);
        playButton = (ImageButton) findViewById(R.id.playbtn);
        prevButton = (ImageButton) findViewById(R.id.prevSeek);
        nextButton = (ImageButton) findViewById(R.id.fwdSeek);

        mp = new MediaPlayer();

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
            }

        }


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

        currentFile = (String) view.getTag();

        startPlay(currentFile);
    }

    /**
     * This method handles what happens to the audio_player once you exit the audio_player activity.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

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
        mp.stop();
        mp.reset();
        playButton.setImageResource(android.R.drawable.ic_media_play);
        handler.removeCallbacks(updatePositionRunnable);
        mySeekbar.setProgress(0);

        isMusicPlaying = false;
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
            switch (v.getId()) {
                case R.id.playbtn: {
                    if (mp.isPlaying()) {
                        handler.removeCallbacks(updatePositionRunnable);
                        mp.pause();
                        playButton.setImageResource(android.R.drawable.ic_media_play);
                    } else {
                        if (isMusicPlaying) {
                            mp.start();
                            playButton.setImageResource(android.R.drawable.ic_media_pause);

                            updatePosition();
                        } else {
                            startPlay(currentFile);
                        }
                    }

                    break;
                }
                case R.id.fwdSeek: {
                    int seekto = mp.getCurrentPosition() + STEP_VALUE;

                    if (seekto > mp.getDuration())
                        seekto = mp.getDuration();

                    mp.pause();
                    mp.seekTo(seekto);
                    mp.start();

                    break;
                }
                case R.id.prevSeek: {
                    int seekto = mp.getCurrentPosition() - STEP_VALUE;

                    if (seekto < 0)
                        seekto = 0;

                    mp.pause();
                    mp.seekTo(seekto);
                    mp.start();

                    break;
                }
            }
        }
    };

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

            durationInMin = new BigDecimal(Double.toString(durationInMin)).setScale(3, BigDecimal.ROUND_UP).doubleValue();

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

}
