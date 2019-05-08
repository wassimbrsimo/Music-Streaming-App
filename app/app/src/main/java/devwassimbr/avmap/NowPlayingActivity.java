package devwassimbr.avmap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;


import com.squareup.picasso.Picasso;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import me.itangqi.waveloadingview.WaveLoadingView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static devwassimbr.avmap.MainActivity.Broadcast_LOOP;
import static devwassimbr.avmap.MainActivity.Broadcast_PAUSE_AUDIO;
import static devwassimbr.avmap.MainActivity.Broadcast_PLAY_INDEX_FROM_QUEUE;
import static devwassimbr.avmap.MainActivity.Broadcast_PLAY_NEXT;
import static devwassimbr.avmap.MainActivity.Broadcast_PLAY_PREVIOUS;
import static devwassimbr.avmap.MainActivity.Broadcast_REMOVE_FROM_QUEUE;
import static devwassimbr.avmap.MainActivity.Broadcast_RESUME_AUDIO;
import static devwassimbr.avmap.MainActivity.Broadcast_SEEK_TO_AUDIO;
import static devwassimbr.avmap.MainActivity.Broadcast_SHUFFLE;

public class NowPlayingActivity extends AppCompatActivity {
    TextView title, artist, playingTime;
    SeekBar seekBar;
    ProgressBar bufferProgress;

    ImageButton play, pause, next, back,save,repeat,shuffle;
    ImageView img;


    Playlist queueList = new Playlist();
    public int currentAudioID;
    AudioAdapter mAdapter;
    WaveLoadingView mWaveLoadingView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullplayer);
        img = findViewById(R.id.img);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        back = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artist);
        save=findViewById(R.id.save);
        repeat=findViewById(R.id.repeat);
        shuffle=findViewById(R.id.shuffle);
        playingTime = findViewById(R.id.currentTime);
        seekBar = findViewById(R.id.seekBar);
        bufferProgress = findViewById(R.id.progressBar);
        queueList = MainActivity.Database.getPlaylist(getIntent().getIntExtra("PLAYLIST_ID", -1));
        mWaveLoadingView = (WaveLoadingView) findViewById(R.id.waveLoadingView);
        Log.e("Playlist", "QUEU = ID : " + queueList.getID());
        registerMediaPlayerReceiver();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new AudioAdapter(queueList, AudioAdapter.NOWPLAYING_TYPE, null,null,0);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        PushDownAnim.setPushDownAnimTo(save,next,back,pause,play,shuffle,repeat);
        setStatusBarColor(R.color.colorGray);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaylistsDialog dialog = new PlaylistsDialog((NowPlayingActivity)v.getContext(),queueList.getList(),true);
                dialog.show();
            }
        });

    }

    private void setStatusBarColor(int colorID){
        Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(this.getResources().getColor(colorID));

    }

    private BroadcastReceiver MediaPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY

            Bundle bundle = intent.getBundleExtra("queue");

            String musicTitle = intent.getStringExtra("title");
            int id = intent.getIntExtra("ID", -1);
            if (currentAudioID != id) {

                currentAudioID = id;
                mAdapter.notifyDataSetChanged();
                Picasso.get().load(MainActivity.Database.getAudio(currentAudioID).getImage()).resize(500,500).centerCrop().into(img);
            String musicArtist = intent.getStringExtra("artist");
            title.setText(musicTitle);
            artist.setText(musicArtist);
            }
            if(intent.getBooleanExtra("repeat",false))
                repeat.setImageResource(getApplicationContext().getResources().getIdentifier("drawable/"+"onrepeat", null, getApplicationContext().getPackageName()));
                else
                repeat.setImageResource(getApplicationContext().getResources().getIdentifier("drawable/"+"repeat", null, getApplicationContext().getPackageName()));

            if(intent.getBooleanExtra("shuffle",false))
                shuffle.setImageResource(getApplicationContext().getResources().getIdentifier("drawable/"+"selectedshuffle", null, getApplicationContext().getPackageName()));
            else
                shuffle.setImageResource(getApplicationContext().getResources().getIdentifier("drawable/"+"shuffle", null, getApplicationContext().getPackageName()));

            int currentTime = intent.getIntExtra("currentTime", 0);
            int bufferPercentage = intent.getIntExtra("percentage", 0);
            int duration = intent.getIntExtra("duration", 0);
            boolean isPlaying = intent.getBooleanExtra("isPlaying", false);

            playingTime.setText(String.format("%2d:%02d",TimeUnit.MILLISECONDS.toMinutes(currentTime),TimeUnit.MILLISECONDS.toSeconds(currentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentTime)))
                    + " - "+      String.format("%2d:%02d",TimeUnit.MILLISECONDS.toMinutes(duration),TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))));
            seekBar.setMax(duration);
            seekBar.setProgress(currentTime);
            bufferProgress.setMax(100);
            bufferProgress.setProgress(bufferPercentage);
            if (isPlaying) {
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.GONE);
                mWaveLoadingView.startAnimation();
            } else {
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                mWaveLoadingView.pauseAnimation();
            }

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                    seekToAudio(seekBar.getProgress());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


                }
            });


        }
    };

    public void toggleShuffle(View v){
        Intent broadcastShuffle = new Intent(Broadcast_SHUFFLE);
        sendBroadcast(broadcastShuffle);
    }
    public void toggleLoop(View v){

        Intent broadcastLoop = new Intent(Broadcast_LOOP);
        sendBroadcast(broadcastLoop);
    }
    public void playNext(View view) {
        Intent broadcastPlayNext = new Intent(Broadcast_PLAY_NEXT);
        sendBroadcast(broadcastPlayNext);
        mAdapter.notifyDataSetChanged();
    }

    public void playPrevious(View view) {
        Intent broadcastPlayPrevious = new Intent(Broadcast_PLAY_PREVIOUS);
        sendBroadcast(broadcastPlayPrevious);
        mAdapter.notifyDataSetChanged();
    }

    public void resumeAudio(View view) {
        Intent broadcastResume = new Intent(Broadcast_RESUME_AUDIO);
        sendBroadcast(broadcastResume);
    }

    public void pauseAudio(View view) {
        Intent broadcastPause = new Intent(Broadcast_PAUSE_AUDIO);
        sendBroadcast(broadcastPause);
    }
    public void backActivity(View view) {
        finish();
    }
    private void seekToAudio(int seekTime) {
        Intent broadcastPause = new Intent(Broadcast_SEEK_TO_AUDIO).putExtra("Seek", seekTime);
        sendBroadcast(broadcastPause);
    }

    private void registerMediaPlayerReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioPlayerService.Broadcast_PLAYER_DATA);
        registerReceiver(MediaPlayerReceiver, intentFilter);
    }

    public void removeToQueue(int index) {
        Intent broadcastRemoveToQueue = new Intent(Broadcast_REMOVE_FROM_QUEUE).putExtra("Audio", index);
        sendBroadcast(broadcastRemoveToQueue);
    }

    public void playIndexFromQueue(int index) {

        Intent broadcastPlayIndexFromQueue = new Intent(Broadcast_PLAY_INDEX_FROM_QUEUE).putExtra("index", index);
        sendBroadcast(broadcastPlayIndexFromQueue);
    }

    private void toggleShuffle() {
        Intent broadcastShuffle = new Intent(Broadcast_SHUFFLE);
        sendBroadcast(broadcastShuffle);
    }

    private void toggleLoop() {

        Intent broadcastLoop = new Intent(Broadcast_LOOP);
        sendBroadcast(broadcastLoop);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        supportFinishAfterTransition();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(MediaPlayerReceiver);
    }



}

