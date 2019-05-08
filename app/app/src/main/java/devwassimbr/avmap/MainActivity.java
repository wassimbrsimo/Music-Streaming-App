package devwassimbr.avmap;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.wonderkiln.blurkit.BlurLayout;


import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends AppCompatActivity {

    private Socket mSocket;

    TextView title,artist,playingTime;
    SeekBar seekBar;
    ProgressBar bufferProgress;
    ImageButton play,pause,next,back;
    LinearLayout plapause;
    CardView playerLayout;
    RelativeLayout swipelayout;
    private static AudioPlayerService player;
    public static boolean serviceBound = false;
    public static int currentAudioID=-1;
    public int currentPlaylistID;

    public static final String DATABASE_NAME="AVM_STORE_DB";
    public static final String Broadcast_PLAY_NEW_QUEUE = "com.avmap.audioplayer.PlayNewQueue";
    public static final String Broadcast_ADD_TO_QUEUE = "com.avmap.audioplayer.AddToQueue";
    public static final String Broadcast_REMOVE_FROM_QUEUE = "com.avmap.audioplayer.RemoveFromQueue";
    public static final String Broadcast_PLAY_INDEX_FROM_QUEUE = "com.avmap.audioplayer.PlayIndexFromQueue";
    public static final String Broadcast_PLAY_NEXT = "com.avmap.audioplayer.PlayNext";
    public static final String Broadcast_PLAY_PREVIOUS = "com.avmap.audioplayer.PlayPrevious";
    public static final String Broadcast_RESUME_AUDIO = "com.avmap.audioplayer.ResumeAudio";
    public static final String Broadcast_PAUSE_AUDIO = "com.avmap.audioplayer.PauseAudio";
    public static final String Broadcast_SEEK_TO_AUDIO = "com.avmap.audioplayer.SeekToAudio";
    public static final String Broadcast_SHUFFLE = "com.avmap.audioplayer.Shuffle";
    public static final String Broadcast_LOOP= "com.avmap.audioplayer.Loop";
    public static final String DB_NAME = "AVMAPP_DATABASE";
    public static Playlist TEMPO,NEW_RELEASES,FAVORITE;
    public static ArrayList<Playlist> ARTISTS,YEARS;
    public static final int TEMPO_PLAYLIST_TYPE =0;
    public static final int NEW_RELEASES_PLAYLIST_TYPE =1;
    public static final int ARTISTS_PLAYLIST_TYPE =2;
    public static final int YEARS_PLAYLIST_TYPE=3;
    public static final int FAVORITE_PLAYLIST_TYPE=4;
    public static final int HISTORY_PLAYLIST_TYPE=5;
    public static final int USER_PLAYLIST_TYPE=10;
    public static int UPDATE_VERSION=201805;
    int SERVER_VERSION;
    public static final int PLAYNEWQUEU_CODE=6;
    public static final int ADDTOQUEUE_CODE=9;

    public final int MinSwipe=0,MaxSwipe=-56;

    Home mHome;
    Library mLibrary;
    Search mSearch;
    Settings mSettings;
    TextView toolbartitle;
    int _yDelta;
    boolean vibrate=true,dataLoaded;
    BlurLayout blur;
    MenuItem navBarlastitem;

    ProgressDialog connect_dialog;

    private ViewPager viewPager;

    public static DB Database;

    {
        try {
            mSocket = IO.socket("http://172.28.58.141:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    void setupPlayerSwipe(){
        AlphaAnimation alpha = new AlphaAnimation(0,0);
        alpha.setDuration(0);
        alpha.setFillAfter(true);
        swipelayout.startAnimation(alpha);
        playerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int Y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        _yDelta = Y ;
                        break;
                    case MotionEvent.ACTION_UP:
                        LinearLayout.LayoutParams layoutParam = (LinearLayout.LayoutParams) playerLayout.getLayoutParams();
                        if(!vibrate)
                            fullPlayer();
                        layoutParam.topMargin=MinSwipe;
                        AlphaAnimation alpha = new AlphaAnimation(swipelayout.getAlpha(),0);
                        alpha.setDuration(0);
                        alpha.setFillAfter(true);
                        swipelayout.startAnimation(alpha);
                        vibrate=true;
                        playerLayout.setLayoutParams(layoutParam);
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:

                        break;
                    case MotionEvent.ACTION_POINTER_UP:

                        break;
                    case MotionEvent.ACTION_MOVE:
                         LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) playerLayout.getLayoutParams();
                        layoutParams.topMargin =MinSwipe+ (Y-_yDelta)/2;
                        if(layoutParams.topMargin>MaxSwipe/2)
                            vibrate=true;
                        if (layoutParams.topMargin>MinSwipe){

                            layoutParams.topMargin=MinSwipe;
                        }
                        if (layoutParams.topMargin<MaxSwipe-10) {
                            layoutParams.topMargin = MaxSwipe;
                            Vibrator vibr = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            if(vibrate){
                                vibr.vibrate(50);
                                vibrate=false;
                            }
                        }
                        float a=(((((float)layoutParams.topMargin))*100f/MaxSwipe))/100f;
                        AlphaAnimation alph = new AlphaAnimation(swipelayout.getAlpha(),a);
                        alph.setDuration(0);
                        alph.setFillAfter(true);
                        swipelayout.startAnimation(alph);
                        playerLayout.setLayoutParams(layoutParams);
                        break;
                }
                playerLayout.invalidate();
                return true;
            }
        });

    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerMediaPlayerReceiver();
        initPreConnexionView(); // prepare view and put some animation with old data
        initMediaPlayer();

        mSocket.connect();
        mSocket.on("",onConnectionEstablished); // {  201807010  }
        mSocket.on("DATA", onListRecieved);


        setupPlayerSwipe();
        blur.setVisibility(View.VISIBLE);
        /*if(dataLoaded) {
            blur.setVisibility(View.GONE);
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

            builder.setTitle("Thank you ^^ !")
                    .setMessage("")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(),"u_u",Toast.LENGTH_LONG).show();

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else {

            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

            builder.setTitle("First time check")
                    .setMessage("Chizu this is temporary ,nvm this part all you need is restart at this point would you ?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(),"u_u ",Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }*/

    setStatusBarColor(R.color.colorGray);

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
    private void initPreConnexionView(){ Database=new DB(getApplicationContext(),DB_NAME);
        blur = findViewById(R.id.blur);
        if(!mSocket.connected()){
        connect_dialog= ProgressDialog.show(MainActivity.this, "Connecting",
                "Loading. Please wait...", true);}
        Database=new DB(getApplicationContext(),DATABASE_NAME);
        Database.getWritableDatabase();
        TEMPO=Database.getTemporaryPlaylist();
        FAVORITE= Database.getFavoritePlaylist();
        NEW_RELEASES = Database.getNewReleasesPlaylist();
        ARTISTS = Database.getArtistsPlaylists();
        YEARS = Database.getYearsPlaylists();
        currentPlaylistID=TEMPO.getID();
        dataLoaded = NEW_RELEASES.getList() != null;



        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setOffscreenPageLimit(3);
        //tabLayout = (TabLayout) findViewById(R.id.tabs);
        //tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(0);
        final BottomNavigationView bnv=findViewById(R.id.bottom_navigation);
        bnv.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_home:
                                viewPager.setCurrentItem(0);
                                break;
                            case R.id.action_library:
                                viewPager.setCurrentItem(1);
                                break;
                            case R.id.action_search:
                                viewPager.setCurrentItem(2);
                                break;
                            case R.id.action_setting:
                                viewPager.setCurrentItem(3);
                                break;
                        }
                        return false;
                    }
                });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (navBarlastitem != null) {
                    navBarlastitem.setChecked(false);
                }
                else
                {
                    bnv.getMenu().getItem(0).setChecked(false);
                }

                bnv.getMenu().getItem(position).setChecked(true);
                navBarlastitem = bnv.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        blur.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        blur.setVisibility(View.GONE);

    }

    void toggleMediaplayer(){

    }
    void initMediaPlayer(){
        toolbartitle=findViewById(R.id.toolbar_title);
        swipelayout =findViewById(R.id.swipeview);
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artist);
        playingTime = findViewById(R.id.currentTime);
        seekBar = findViewById(R.id.seekBar);
        bufferProgress = findViewById(R.id.progressBar);
        if(TEMPO!=null && TEMPO.getList()!=null && TEMPO.getList().size()>0) {
            title.setText(TEMPO.getList().get(0).getTitle());
            artist.setText(TEMPO.getList().get(0).getArtist());
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekToAudio(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
            }});

        play=findViewById(R.id.play);
        pause=findViewById(R.id.pause);
        back=findViewById(R.id.previous);
        next=findViewById(R.id.next);
        plapause=findViewById(R.id.plapause);
        playerLayout=findViewById(R.id.mediaplayer);
        playerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullPlayer();
            }
        });

    }

    private void setupViewPager(ViewPager viewPager) {
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        mHome=new Home();
        mLibrary=new Library();
        mSearch=new Search();
        mSettings=new Settings();
        adapter.addFragment(mHome, "Home");
        adapter.addFragment(mLibrary, "Library");
        adapter.addFragment(mSearch, "Search");
        adapter.addFragment(mSettings, "Settings");
        viewPager.setAdapter(adapter);
    }
    private void syncDataFromServer(ArrayList<Audio> ALL_TRACKS){


        Database.flushAUDIO();
       for (Audio track:ALL_TRACKS
             ) {
            Database.addAudio(track);
        }

        NEW_RELEASES.setList(Database.buildNewReleasesPlaylist().getList());
        ARTISTS.clear();
        ARTISTS.addAll(Database.buildArtistsPlaylists());
        YEARS.clear();
        YEARS.addAll(Database.buildYearsPlaylists());
        mHome.updateView(true);
        UPDATE_VERSION=SERVER_VERSION;
        connect_dialog.dismiss();
    }



    private ArrayList<Audio> FakeServerData() {
        ArrayList<Audio> serverData=new ArrayList<>();

        serverData.add(new Audio("http://172.28.58.141:3000/music/0","All For Love","https://i.scdn.co/image/5c0b8e039ed8a2de5af60238e1b75313e5735ed3","Tungevaag & Raaban",0,2018));
        serverData.add(new Audio("http://172.28.58.141:3000/music/1","I'm A Mess ","https://i.ytimg.com/vi/eS-B29vvtDM/hqdefault.jpg","Bebe Rexha",1,2018));
        serverData.add(new Audio("http://172.28.58.141:3000/music/2","Mayores (KLAP REMIX) ","https://files.setbeat.com/tmp/s_1036144.jpg","Lucas Lucco ",2,2018));
        serverData.add(new Audio("http://172.28.58.141:3000/music/3","Mayores","https://etimg.akamaized.net/1242911076001/201712/3810/1242911076001_5682270558001_5682268289001-vs.jpg?pubId=1242911076001","Becky-G",3,2018));
        serverData.add(new Audio("http://172.28.58.141:3000/music/4","Eastside","https://www.dmpgroup.com/hubfs/Roster%20Images/Benny%20Blanco%20Roster%20Pic.jpg?t=1536871826518","Benny Blanco",4,2016));
        serverData.add(new Audio("http://172.28.58.141:3000/music/5","Breathing","https://i2-prod.mirror.co.uk/incoming/article12826542.ece/ALTERNATES/s1200/Ariana-Grande.jpg","Ariana Grande",5,2018));
        serverData.add(new Audio("http://172.28.58.141:3000/music/6","I Like It","https://i.ytimg.com/vi/-wP2M9hnTOk/maxresdefault.jpg","Cardi B, Bad Bunny & J Balvin",6,2018));
        serverData.add(new Audio("http://172.28.58.141:3000/music/7","Finest Hour","https://pl.scdn.co/images/pl/default/69cca6239dc6e8f46f32062e2c33d139bd85afc7","Cash Cash",7,2018));
        serverData.add(new Audio("http://172.28.58.141:3000/music/8"," The Way I Love You ","https://i0.wp.com/www.thenocturnaltimes.com/wp-content/uploads/2018/07/Dante-Klein_Cat-Carpenters_MG_7946-1.jpg?resize=560%2C600","Dante Klein & Cat Carpenters",8,2018));
        serverData.add(new Audio("http://172.28.58.141:3000/music/9","Don't Leave Me Alone","https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSElX-Zt6JrWf13GSFJBApk5HQi0mEEr7iydaV997T2DZqCbw_yBA","David Guetta",9,2018));
        serverData.add(new Audio("http://172.28.58.141:3000/music/10","Heaven To Me ","https://d3i94ju9t7ckp4.cloudfront.net/performers/covers/000/000/116/original/1._New_Profile_Pic.jpg?1517314422","Don Diablo ",10,2018));
        serverData.add(new Audio("http://172.28.58.141:3000/music/11","Done For Me ","http://133.242.151.193/pmstudio/images/Charlie-Puth29.jpg","Charlie Puth",11,2018));
        serverData.add(new Audio("http://172.28.58.141:3000/music/17","Guerilla","https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR6YngvUE4kezrBKpk9loZYnkMUC8Vm7qx5wr8hhYejzrZQKeQP","Soolking",17,2018));
        serverData.add(new Audio("http://172.28.58.141:3000/music/45","Dalida","https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR6YngvUE4kezrBKpk9loZYnkMUC8Vm7qx5wr8hhYejzrZQKeQP","Soolking",45,2018));
        serverData.add(new Audio("http://172.28.58.141:3000/music/46","DNA","https://metrouk2.files.wordpress.com/2017/11/pri_60761031.jpg?quality=80&strip=all&zoom=1&resize=644%2C429","BTS (방탄소년단)",46,2019));
        serverData.add(new Audio("http://172.28.58.141:3000/music/47","Blood Sweat & Tears","https://metrouk2.files.wordpress.com/2017/11/pri_60761031.jpg?quality=80&strip=all&zoom=1&resize=644%2C429","BTS (방탄소년단)",47,2019));
        serverData.add(new Audio("http://172.28.58.141:3000/music/48","Not Today","https://metrouk2.files.wordpress.com/2017/11/pri_60761031.jpg?quality=80&strip=all&zoom=1&resize=644%2C429","BTS (방탄소년단)",48,2019));
        serverData.add(new Audio("http://172.28.58.141:3000/music/49","Spring Day","https://metrouk2.files.wordpress.com/2017/11/pri_60761031.jpg?quality=80&strip=all&zoom=1&resize=644%2C429","BTS (방탄소년단)",49,2019));
        serverData.add(new Audio("http://cdl3.convert2mp3.net/swf_player.php?id=SBsYRlgz9UxY_youtube_UBLh17Lo-7A","Fire","https://metrouk2.files.wordpress.com/2017/11/pri_60761031.jpg?quality=80&strip=all&zoom=1&resize=644%2C429","BTS (방탄소년단)",50,2019));

//   BTS (방탄소년단)

       return serverData;
    }
    //Binding this Client to the AudioPlayer Service
    public static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            //Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };





    public void fullPlayer(){
    Intent intent = new Intent(getBaseContext(),NowPlayingActivity.class).putExtra("PLAYLIST_ID",currentPlaylistID);
    Pair<View, String> p1 = Pair.create((View)title, "title");
    Pair<View, String> p2 = Pair.create((View)artist, "artist");
    Pair<View, String> p5 = Pair.create((View)next, "next");
    Pair<View, String> p6 = Pair.create((View)back, "previous");
    Pair<View, String> p7 = Pair.create((View)playingTime, "currenttime");
    Pair<View, String> p8 = Pair.create((View)plapause, "plapause");
    ActivityOptionsCompat options = ActivityOptionsCompat.
            makeSceneTransitionAnimation(this, p1, p2,p5,p6,p7,p8);
    startActivity(intent,options.toBundle());
    }

    public void playNewQueue(int ID,int index) {
        //Check is service is active

        if (!serviceBound) {
            toggleMediaplayer();
            Intent playerIntent = new Intent(this, AudioPlayerService.class).putExtra("id",ID).putExtra("index",index);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastPlayNewQueue = new Intent(Broadcast_PLAY_NEW_QUEUE).putExtra("id",ID).putExtra("index",index);
            sendBroadcast(broadcastPlayNewQueue);

        }
    }
    public boolean addToQueue(int toAdd){
        //TEMPO.getList().add(toAdd);
        Playlist p=Database.getTemporaryPlaylist();
        Log.e("PLAYLIST","PLAYLIST = size "+p.getList().size());
        for (Audio track:    p.getList() ) {
            if(track.getID()==toAdd){
                Toast.makeText(getApplicationContext(),"Exist deja",Toast.LENGTH_SHORT).show();
                return true;
        }}
        Intent broadcastAddToQueue = new Intent(Broadcast_ADD_TO_QUEUE).putExtra("Audio",toAdd);
        sendBroadcast(broadcastAddToQueue);
        Toast.makeText(getApplicationContext()," Ajouté ",Toast.LENGTH_SHORT).show();
        return false;
    }
    public void playIndexFromQueue(int index){
        Intent broadcastPlayIndexFromQueue = new Intent(Broadcast_PLAY_INDEX_FROM_QUEUE).putExtra("index",index);
        sendBroadcast(broadcastPlayIndexFromQueue);
    }
    private void playNext(){
        Intent broadcastPlayNext = new Intent(Broadcast_PLAY_NEXT);
        sendBroadcast(broadcastPlayNext);
    }
    private void playPrevious(){
        Intent broadcastPlayPrevious = new Intent(Broadcast_PLAY_PREVIOUS);
        sendBroadcast(broadcastPlayPrevious);
    }
    private void resumeAudio(){
        if(serviceBound){
        Intent broadcastResume = new Intent(Broadcast_RESUME_AUDIO);
        sendBroadcast(broadcastResume);}
        else
            playNewQueue(currentPlaylistID,0);
    }
    private void pauseAudio(){
        Intent broadcastPause = new Intent(Broadcast_PAUSE_AUDIO);
        sendBroadcast(broadcastPause);
    }
    private void seekToAudio(int seekTime){
        Intent broadcastPause = new Intent(Broadcast_SEEK_TO_AUDIO).putExtra("Seek",seekTime);
        sendBroadcast(broadcastPause);
    }
    private void toggleShuffle(){
        Intent broadcastShuffle = new Intent(Broadcast_SHUFFLE);
        sendBroadcast(broadcastShuffle);
    }
    private void toggleLoop(){

        Intent broadcastLoop = new Intent(Broadcast_LOOP);
        sendBroadcast(broadcastLoop);
    }
    // add broadcastmsg + declaration on TOP



    public void playNewReleasesQueu(View view){
        int ID =0;
        mSocket.emit("streamReq",ID);
        //audioPlayer.Play(mp3File);
        playNewQueue(NEW_RELEASES.getID(),0);


    }
    public void ResumeBtn(View view){
        resumeAudio();
    }
    public void PauseBtn(View view){
       pauseAudio();

    }
    public void PlayNext(View view){
        playNext();

    }
    public void PlayPrevious(View view){
      playPrevious();


    }
    public void pickFavorite(View view){
        //add to known playlist
    }
    public void toggleShuffle(View view){
        toggleShuffle();
    }
    public void toggleLoop(View view){
            toggleLoop();
    }


    private Emitter.Listener onConnectionEstablished = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  Log.e("SOCKET","Connection established , Server Version : V"+args[0].toString());
                                  connect_dialog.setMessage("CONNECTED");
                                  if(UPDATE_VERSION<(int)args[0]) {
                                      SERVER_VERSION=(int)args[0];
                                      mSocket.emit("UPDATE_REQUEST");
                                      connect_dialog.setMessage("Connected Successfully ,Updating Data");
                                  }
                                  else
                                      connect_dialog.dismiss();
                              }
                          }

            );
        }
    };

    private Emitter.Listener onListRecieved = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
           runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Log.e("SOCKET","DATA RECIEVED "+args[0]);
                    syncDataFromServer(FakeServerData());

                }
        }

       );
        }
    };


    private BroadcastReceiver MediaPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY

            String musicTitle =intent.getStringExtra("title");
            String musicArtist =intent.getStringExtra("artist");
            int tempID= intent.getIntExtra("ID",-1);
            int currentTime=intent.getIntExtra("currentTime",0);
            int bufferPercentage=intent.getIntExtra("percentage",0);
            int duration=intent.getIntExtra("duration",0);
            boolean isPlaying=intent.getBooleanExtra("isPlaying",false);
           // TransitionManager.beginDelayedTransition(playerLayout);
            if(tempID!=currentAudioID || currentAudioID==-1){
                currentAudioID=tempID;
                switch(viewPager.getCurrentItem()){
                    case 0:
                        mHome.updateView(false);
                        Log.e("NOTIFYDATASETCHANGED"," HOME TAB");
                        break;
                    case 1 :
                        mLibrary.notifyDataChanger();
                        Log.e("NOTIFYDATASETCHANGED"," LIBRARY TAB");
                        break;

                    case 2:
                        mSearch.notifyDataChanger();
                        Log.e("NOTIFYDATASETCHANGED"," SEARCH TAB");
                        break;

                }
            }
            title.setText(musicTitle);
            artist.setText(musicArtist);
            playingTime.setText(String.format("%2d:%02d",TimeUnit.MILLISECONDS.toMinutes(currentTime),TimeUnit.MILLISECONDS.toSeconds(currentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentTime)))
                        + " - "+      String.format("%2d:%02d",TimeUnit.MILLISECONDS.toMinutes(duration),TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))
            );
            seekBar.setMax(duration);
            seekBar.setProgress(currentTime);
            bufferProgress.setMax(100);
            bufferProgress.setProgress(bufferPercentage);
            if(isPlaying)
            {
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.GONE);
            }
            else{
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
            }
        }
    };
    private void registerMediaPlayerReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioPlayerService.Broadcast_PLAYER_DATA);
        registerReceiver(MediaPlayerReceiver, intentFilter);
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
           // unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
        unregisterReceiver(MediaPlayerReceiver);
    }


}
