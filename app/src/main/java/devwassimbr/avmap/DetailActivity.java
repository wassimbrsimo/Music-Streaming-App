package devwassimbr.avmap;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.Loader;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.PageIndicator;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static devwassimbr.avmap.MainActivity.Broadcast_ADD_TO_QUEUE;
import static devwassimbr.avmap.MainActivity.Broadcast_PLAY_NEW_QUEUE;
import static devwassimbr.avmap.MainActivity.Database;
import static devwassimbr.avmap.MainActivity.serviceBound;
import static devwassimbr.avmap.MainActivity.serviceConnection;

public class DetailActivity extends AppCompatActivity {

    private PageIndicator indicator;
    static LoaderManager loaderManager;
    private ArrayList<Pair<DetailPage, Integer>> fragPosition;
    ViewPager pager;
    public int currentAudioID = -1;
    int currentItem;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }*/
        setContentView(R.layout.activity_detail);

        registerMediaPlayerReceiver();
        loaderManager = getSupportLoaderManager();


        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pager = findViewById(R.id.pager);
        ArrayList<Playlist> list = new ArrayList<>();
        int type=getIntent().getIntExtra("TYPE",MainActivity.ARTISTS_PLAYLIST_TYPE);
        switch (type){
            case MainActivity.ARTISTS_PLAYLIST_TYPE :
                list = MainActivity.ARTISTS;
                break;
            case MainActivity.YEARS_PLAYLIST_TYPE:
                list=MainActivity.YEARS;
                break;
            default :
                list = Database.getPlaylistByType(type);
                break;
        }
        pager.setAdapter(new IconAdapter(this, getSupportFragmentManager(),list));
        indicator = findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        pager.setCurrentItem(getIntent().getIntExtra("position", 0), false);
        findViewById(R.id.touchme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.pager).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

/*
        pager.post(new Runnable() {
            @Override
            public void run() {
                supportStartPostponedEnterTransition();
            }
        });
*/
        fragPosition = new ArrayList<>();
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
    public void playNewQueue(int ID, int index) {
        if (!serviceBound) {
            // toggleMediaplayer();
            Intent playerIntent = new Intent(this, AudioPlayerService.class).putExtra("id", ID).putExtra("index", index);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            MainActivity.serviceBound = true;
        } else {
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastPlayNewQueue = new Intent(Broadcast_PLAY_NEW_QUEUE).putExtra("id", ID).putExtra("index", index);
            sendBroadcast(broadcastPlayNewQueue);
        }
    }

    public Boolean addToQueue(int ID) {

        Playlist p = Database.getTemporaryPlaylist();
        Log.e("PLAYLIST", "PLAYLIST = size " + p.getList().size());
        for (Audio track : p.getList()) {
            if (track.getID() == ID) {
                Toast.makeText(getApplicationContext(), "Exist deja", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        Intent broadcastAddToQueue = new Intent(Broadcast_ADD_TO_QUEUE).putExtra("Audio", ID);
        sendBroadcast(broadcastAddToQueue);
        Toast.makeText(getApplicationContext(), " Ajouté ", Toast.LENGTH_SHORT).show();
        return false;
    }

    private class IconAdapter extends FragmentStatePagerAdapter implements IconPagerAdapter {
        private final Context context;
        ArrayList<Playlist> list;


        private IconAdapter(Context context, FragmentManager manager, ArrayList<Playlist> list) {
            super(manager);
            this.context = context;
            this.list = list;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return list.get(position).getName();
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putInt("ID", list.get(position).getID());
            bundle.putInt("TYPE", list.get(position).getType());
            bundle.putString("Title", list.get(position).getName());
            fragPosition.add(new Pair<DetailPage, Integer>((DetailPage) DetailPage.instantiate(context, DetailPage.class.getName(), bundle), position));
            return fragPosition.get(fragPosition.size() - 1).first;
        }

        @Override
        public int getIconResId(int index) {

            return list.get(index).getID();
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }

    public static class DetailPage extends Fragment implements LoaderManager.LoaderCallbacks<Playlist> {

        AudioAdapter mAdapter;
        RecyclerView list;
        Playlist playlist;


        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.detail_page, container, false);
        }

        @Override
        public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            int id = getArguments().getInt("ID", -69);
            int Type=getArguments().getInt("TYPE",2);
            String title=getArguments().getString("Title");
            list = view.findViewById(R.id.list);
            list.setLayoutManager(new LinearLayoutManager(getContext()));
            playlist = new Playlist("", id, Type, new ArrayList<Audio>());
            mAdapter = new AudioAdapter(playlist, AudioAdapter.POPUP_LIST_TYPE, null, null, 0);
            list.setAdapter(mAdapter);

            Log.e("OncreateView", "InitLoader " + id);
            loaderManager.initLoader(id, getArguments(), this).forceLoad();

            //list.setAdapter(AudioAdapter(LayoutInflater.from(getContext()), new ArrayList<Item>());
        }

        @Override
        public Loader<Playlist> onCreateLoader(int id, Bundle args) {
            Log.e("LOADER", "CREATED");
            return new PlaylistLoader(getActivity(), getArguments());
        }

        @Override
        public void onLoadFinished(@NonNull android.support.v4.content.Loader<Playlist> loader, Playlist data) {
            Log.e("onLoadFinished", "load is finished with : ........................ data = " + getArguments().getInt("ID", -69));
            playlist.getList().clear();
            playlist.setList(data.getList());
            mAdapter.notifyDataSetChanged();
            getView().findViewById(R.id.progbar).setVisibility(View.GONE);
            getView().findViewById(R.id.viewtoclick).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        }

        @Override
        public void onLoaderReset(@NonNull android.support.v4.content.Loader<Playlist> loader) {

        }

    }

    private static class PlaylistLoader extends AsyncTaskLoader {
        Bundle args;

        public PlaylistLoader(Context context, Bundle args) {
            super(context);
            this.args = args;
        }

        @Override
        public Playlist loadInBackground() {
            Log.e("Loading ", "Data ..");
            Playlist toLoad = Database.getPlaylist(args.getInt("ID", -1));
            return toLoad;
        }

    }

    private BroadcastReceiver MediaPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int tempItem = pager.getCurrentItem();
            int tempID = intent.getIntExtra("ID", -1);
            Log.e("DETAIL", "AUDIO ID :" + tempID + " .............................................." + currentAudioID + ".............................................................. TempItem :" + tempItem + ".... currentItem : " + currentItem);

            if (currentAudioID != tempID || tempItem != currentItem) {
                currentItem = tempItem;
                currentAudioID = tempID;
                getCurrentFragement(pager.getCurrentItem()).mAdapter.notifyDataSetChanged();
            }

        }
    };

    DetailPage getCurrentFragement(int position) {
        for (Pair<DetailPage, Integer> temp : fragPosition
                ) {
            if (temp.second == position) {
                return temp.first;

            }
        }
        return null;
    }

    private void registerMediaPlayerReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioPlayerService.Broadcast_PLAYER_DATA);
        registerReceiver(MediaPlayerReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(MediaPlayerReceiver);
    }


}