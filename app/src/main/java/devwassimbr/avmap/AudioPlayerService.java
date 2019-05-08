package devwassimbr.avmap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class AudioPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,AudioManager.OnAudioFocusChangeListener {

    public static final int BROADCAST_DELAY=1000;
    public static final String ACTION_PLAY = "com.devwassim.avm.audioplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.devwassim.avm.audioplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.devwassim.avm.audioplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.devwassim.avm.audioplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.devwassim.avm.audioplayer.ACTION_STOP";
    public static final String Broadcast_PLAYER_DATA="com.devwassim.avm.audioplayer.BROADCAST_MEDIAPLAYER_DATA";
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;

    private final IBinder iBinder =  new LocalBinder();
    private AudioManager audioManager;

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    //List of available Audio files
    private MediaPlayer mediaPlayer;
    private int resumePosition,
                bufferPosition;
    private Playlist playlist =new Playlist();
    private int audioIndex = 0;
    private Audio activeAudio;
    private Boolean shuffle=false,repeat=false;
    private Handler mSeekbarUpdateHandler = new Handler();
    private int duration;



    @Override
    public void onCreate() {
        super.onCreate();
        callStateListener();
        register_playNewQueue();
        registerBecomingNoisyReceiver();
        register_addToQueue();
        register_pauseAudio();
        register_resumeAudio();
        register_playIndexFromSameQueue();
        register_playNext();
        register_playPrevious();
        register_removeFromQueue();
        register_seekToReceiver();
        register_toggleShuffle();
        register_toggleLoop();
        mUpdateSeekbar.run();
        playlist=MainActivity.TEMPO;


    }


    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();
        activeAudio= playlist.getList().get(audioIndex);
        MainActivity.Database.addHistory(activeAudio);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location

            mediaPlayer.setDataSource(activeAudio.getData());

        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        Log.e("PLAYER SERVICE ","STREAMING REQUEST : "+activeAudio.getData());
        mediaPlayer.prepareAsync();
        sendMediaPlayerMetaDataBroadcast();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {


        if (requestAudioFocus() == false ) {
            //Could not gain focus
            stopSelf();
        }
        playlist.setList(MainActivity.Database.getPlaylist(intent.getIntExtra("id",-1)).getList());

        audioIndex=intent.getIntExtra("index",0);

        if (mediaSessionManager == null) {
            initMediaSession();
            if(mediaPlayer!=null){
                stopMedia();
                mediaPlayer.reset();
            }
        }
            initMediaPlayer();
        if(intent.getIntExtra("id",-1)!=playlist.getID()){
            mUpdatePlaylist.run();
        }
            //buildNotification(PlaybackStatus.PLAYING);
            //Handle Intent action from MediaSession.TransportControls
            handleIncomingActions(intent);

            return START_NOT_STICKY;
    }

    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }
    private Runnable mUpdatePlaylist = new Runnable() {
        @Override
        public void run() {
            MainActivity.Database.updatePlaylist(MainActivity.TEMPO.getID(),playlist.getList());
        }
    };

    private BroadcastReceiver playNewQueue = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playlist.setList(MainActivity.Database.getPlaylist(intent.getIntExtra("id",-1)).getList());
            audioIndex=intent.getIntExtra("index",0);

            if(mediaPlayer!=null) {
                stopMedia();
                mediaPlayer.reset();
            }
            initMediaPlayer();


            if(intent.getIntExtra("id",-1)!=playlist.getID()){
               mUpdatePlaylist.run();
            }
            //buildNotification(PlaybackStatus.PLAYING);
        }
    };
    private BroadcastReceiver playIndexFromSameQueue = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e("playIndexFromSameQueue","RECIEVED");
            audioIndex= intent.getIntExtra("index",-1);
            if(mediaPlayer!=null){
                stopMedia();
                mediaPlayer.reset();
            }
            initMediaPlayer();
            //buildNotification(PlaybackStatus.PLAYING);
        }
    };
    private BroadcastReceiver addToQueue = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Audio toAdd =MainActivity.Database.getAudio(intent.getIntExtra("Audio",-1));
            playlist.getList().add(toAdd);
            MainActivity.Database.addPlaylistTrack(toAdd.getID(),playlist.getID());
        }
    };
    private BroadcastReceiver removeFromQueue = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playlist.getList().remove(intent.getIntExtra("Audio",-1));
            if(playlist.getList().size()!=0 && audioIndex==intent.getIntExtra("Audio",-1)) {
                if (audioIndex >= playlist.getList().size())
                    skipToPrevious();
                else
                    skipToNext();
            }else{
                stopMedia();
            }
            MainActivity.Database.removePlaylistTrack(playlist.getID(),intent.getIntExtra("Audio",-1));
        }
    };
    private BroadcastReceiver playNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        skipToNext();
        }
    };
    private BroadcastReceiver playPrevious = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        skipToPrevious();
        }
    };
    private BroadcastReceiver resumeAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          resumeMedia();
        }
    };
    private BroadcastReceiver pauseAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          pauseMedia();
        }
    };
    private BroadcastReceiver seekToReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int seekTime =intent.getIntExtra("Seek",mediaPlayer.getCurrentPosition());
            mediaPlayer.seekTo(seekTime);

        }
    };
    private BroadcastReceiver toggleShuffle= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            shuffle=!shuffle;
        }
    };
    private BroadcastReceiver toggleLoop = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            repeat=!repeat;
        }
    };
    private void sendMediaPlayerMetaDataBroadcast(){
        if(mediaPlayer!=null) {
            Intent broadcastPlayerData = new Intent(Broadcast_PLAYER_DATA);
            broadcastPlayerData.putExtra("currentTime", mediaPlayer.getCurrentPosition());
            broadcastPlayerData.putExtra("title", activeAudio.getTitle());
            broadcastPlayerData.putExtra("ID", activeAudio.getID());
            broadcastPlayerData.putExtra("playlistID", playlist.getID());
            broadcastPlayerData.putExtra("artist", activeAudio.getArtist());
            broadcastPlayerData.putExtra("percentage", bufferPosition);
            broadcastPlayerData.putExtra("duration", duration);
            broadcastPlayerData.putExtra("isPlaying", mediaPlayer.isPlaying());
            broadcastPlayerData.putExtra("repeat",repeat);
            broadcastPlayerData.putExtra("shuffle",shuffle);
            sendBroadcast(broadcastPlayerData);
        }
    }
    private Runnable mUpdateSeekbar = new Runnable() {
        @Override
        public void run() {

            sendMediaPlayerMetaDataBroadcast();
            mSeekbarUpdateHandler.postDelayed(this, BROADCAST_DELAY);
        }
    };

    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void playMedia() {

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }
    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }
    private void initMediaSession() {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);


        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }


    private void skipToNext() {
        Log.e("SKIP","INDEX = "+audioIndex);
        if(repeat){
            mediaPlayer.seekTo(0);
            return;
            }
        else if (audioIndex >= playlist.getList().size() - 1) {
            //if last in playlist
            audioIndex = 0;

            activeAudio = playlist.getList().get(audioIndex);
        } else {
            if(shuffle)
            audioIndex = new Random().nextInt((playlist.getList().size() - 1) + 1);
            activeAudio = playlist.getList().get(++audioIndex);

        }

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToPrevious() {
        if(repeat){
            mediaPlayer.seekTo(0);
            return;
        }
        else if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of playlist
            audioIndex = playlist.getList().size() - 1;
            activeAudio = playlist.getList().get(audioIndex);
        } else {
            //get previous in playlist
            activeAudio = playlist.getList().get(--audioIndex);
        }

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }
    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the layout action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher_foreground); //replace with your own image

        // Create a new Notification



        Notification.Builder notificationBuilder = new Notification.Builder(this)
                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorPrimary))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(activeAudio.getArtist())
                .setContentTitle(activeAudio.getImage())
                .setContentInfo(activeAudio.getTitle())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, AudioPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            playMedia();
            //transportControls.layout();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            pauseMedia();
           // transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        bufferPosition=percent;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(!repeat)
       skipToNext();
        else
            mediaPlayer.seekTo(0);
    }

    //Handle errors
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        duration=mediaPlayer.getDuration();
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        //Could not gain focus
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }


    public void getStreamMetaData(String url){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(url, new HashMap<String, String>());
        //now do whatever you want

    }
    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }
    private void register_playNewQueue() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_QUEUE);
        registerReceiver(playNewQueue, filter);
    }
    private void register_playIndexFromSameQueue() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_INDEX_FROM_QUEUE);
        registerReceiver(playIndexFromSameQueue, filter);
    }
    private void register_addToQueue() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_ADD_TO_QUEUE);
        registerReceiver(addToQueue, filter);
    }
    private void register_removeFromQueue() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_REMOVE_FROM_QUEUE);
        registerReceiver(removeFromQueue, filter);
    }
    private void register_playNext() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEXT);
        registerReceiver(playNext, filter);
    }
    private void register_playPrevious() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_PREVIOUS);
        registerReceiver(playPrevious, filter);
    }
    private void register_resumeAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_RESUME_AUDIO);
        registerReceiver(resumeAudio, filter);
    }
    private void register_pauseAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PAUSE_AUDIO);
        registerReceiver(pauseAudio, filter);
    }
    private void register_seekToReceiver() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_SEEK_TO_AUDIO);
        registerReceiver(seekToReceiver, filter);
    }
    private void register_toggleShuffle() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_SHUFFLE);
        registerReceiver(toggleShuffle, filter);
    }
    private void register_toggleLoop() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_LOOP);
        registerReceiver(toggleLoop, filter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();
        mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewQueue);
        unregisterReceiver(addToQueue);
        unregisterReceiver(removeFromQueue);
        unregisterReceiver(playIndexFromSameQueue);
        unregisterReceiver(playNext);
        unregisterReceiver(playPrevious);
        unregisterReceiver(resumeAudio);
        unregisterReceiver(pauseAudio);
        unregisterReceiver(seekToReceiver);
        unregisterReceiver(toggleShuffle);
        unregisterReceiver(toggleLoop);


    }
}