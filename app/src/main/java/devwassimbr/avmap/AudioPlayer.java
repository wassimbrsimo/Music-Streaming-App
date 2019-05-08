package devwassimbr.avmap;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;

public class AudioPlayer extends MediaPlayer implements android.media.MediaPlayer.OnPreparedListener, android.media.MediaPlayer.OnBufferingUpdateListener  {
    private boolean isPrepared;
    private SeekBar skbr;
    private TextView time;
    private Handler handler;
    private Runnable runnable;
    public AudioPlayer(SeekBar s, TextView time) {
        this.skbr=s;
        this.time=time;
        this.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.setOnPreparedListener(this);
        this.setOnBufferingUpdateListener(this);
        this.handler=new Handler();
    }
    public void seekBarLoop(){
        skbr.setProgress(this.getCurrentPosition());
        if(this.isPlaying()){
            runnable = new Runnable() {
                @Override
                public void run() {
                    seekBarLoop();
                    Log.e("progress","ms "+getCurrentPosition());
                    time.setText(getCurrentPosition()+" ms   -   "+getDuration()+" ms");
                }
            };
            handler.postDelayed(runnable,1000);
        }
    }
    public void Play(String URL){
        if(!this.isPlaying() && !isPrepared)
        {
        try {
            isPrepared=false;
            this.setDataSource(URL);
            this.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            }
        }
        else if(isPrepared){
            this.start();
        }
    }
    public void Pause(){
        this.pause();
    }
    @Override
    public void onBufferingUpdate(android.media.MediaPlayer mp, int percent) {

    }

    @Override
    public void onPrepared(android.media.MediaPlayer mp) {
        isPrepared=true;
        this.start();
        skbr.setMax(this.getDuration());
        seekBarLoop();
    }
}
