package devwassimbr.avmap;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thekhaeng.pushdownanim.PushDownAnim;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class AudioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public static final int NOWPLAYING_TYPE =0,
                            TEMP_LIST_TYPE=4,
                            POPUP_LIST_TYPE=5,
                        TRACK_BUBBLE =1,
                        SQUARE_PLAYLIST=2,
                        PLAYLIST_BUBBLE=3,
                        LAYOUT_MANAGER_LINEAR=0,
                        LAYOUT_MANAGER_GRID=1;
    int Type;
    int LayoutManagerType;
    RecyclerView rv;
    ArrayList<Playlist> playlists= new ArrayList<>();
    Playlist playlistToPopulate= new Playlist();

    public AudioAdapter(Playlist playlist,int TYPE,ArrayList<Playlist> playlists,RecyclerView rv,int type){
        this.playlistToPopulate=playlist;
        this.Type=TYPE;
        this.playlists=playlists;
        this.rv=rv;
        this.LayoutManagerType=type;
    }

    public class QueueHolder extends RecyclerView.ViewHolder {
        public TextView title, artist;
        public AVLoadingIndicatorView isplaying;
        ImageButton favBtn,option;
        LinearLayout layout;
        public QueueHolder(View view) {
            super(view);
            option=view.findViewById(R.id.option);
            title = view.findViewById(R.id.title);
            isplaying = view.findViewById(R.id.isplayling);
            artist = view.findViewById(R.id.artist);
            favBtn = view.findViewById(R.id.fav_btn);
            layout= view.findViewById(R.id.layout);
        }
    }

    public class TrackBubbleHolder extends RecyclerView.ViewHolder {
        public TextView title, artist;
        LinearLayout layout;
        CircleImageView img;
        AVLoadingIndicatorView isplayin;

        public TrackBubbleHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            artist = view.findViewById(R.id.artist);
            layout =view.findViewById(R.id.play);
            img=view.findViewById(R.id.img) ;
            isplayin=view.findViewById(R.id.isplayling);
        }
    }
    public class PlaylistBubbleHolder extends RecyclerView.ViewHolder {
        public TextView title, img, counter;
        LinearLayout layout;
        CircleImageView pic;

        public PlaylistBubbleHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            counter = view.findViewById(R.id.counter);
            layout =view.findViewById(R.id.play);
            pic=view.findViewById(R.id.img);
        }
    }
    public class SquarePlaylistHolder extends RecyclerView.ViewHolder {
        public TextView title, img, counter;
        LinearLayout layout;
        ImageView image;

        public SquarePlaylistHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            counter = view.findViewById(R.id.counter);
            layout =view.findViewById(R.id.play);
            image=view.findViewById(R.id.img);
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView=null ;
        RecyclerView.ViewHolder holder=null;
        switch (viewType){
            case NOWPLAYING_TYPE:
                itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.queue_list_row, parent, false);
                holder= new QueueHolder(itemView);
                break;
            case TEMP_LIST_TYPE:
                itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.queue_list_row, parent, false);
                holder= new QueueHolder(itemView);
                break;
            case POPUP_LIST_TYPE:
                itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.small_queue_list_row, parent, false);
                holder= new QueueHolder(itemView);
                break;
            case TRACK_BUBBLE:
                itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.track_bubble, parent, false);
                holder=new TrackBubbleHolder(itemView);
                break;
            case PLAYLIST_BUBBLE:
                itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.playlist_bubble, parent, false);
                holder=new PlaylistBubbleHolder(itemView);
                break;
            case SQUARE_PLAYLIST:
                itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.square_playlist, parent, false);
                holder=new SquarePlaylistHolder(itemView);
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {


        switch (getItemViewType(position)){
            case NOWPLAYING_TYPE:
                        final Audio audio= playlistToPopulate.getList().get(position);
                        final QueueHolder queueHolder=(QueueHolder)holder;
                        queueHolder.title.setText(audio.getTitle());
                        queueHolder.artist.setText(audio.getArtist());
                        queueHolder.title.setTextColor(queueHolder.title.getContext().getResources().getColor(R.color.colorPrimary));
                        queueHolder.artist.setTextColor(queueHolder.title.getContext().getResources().getColor(R.color.colorSecondary));

                        queueHolder.layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(MainActivity.serviceBound)
                            ((NowPlayingActivity) v.getContext()).playIndexFromQueue(position);

                        }
                    });

                        if(playlistToPopulate.getType()!=MainActivity.FAVORITE_PLAYLIST_TYPE) {
                            if (MainActivity.Database.getFavorite(audio.getID())) {
                                queueHolder.favBtn.setImageResource(queueHolder.favBtn.getContext().getResources().getIdentifier("drawable/"+"heart_accent", null, queueHolder.favBtn.getContext().getPackageName()));
                                queueHolder.favBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        MainActivity.Database.removeFavorite(audio.getID());
                                        notifyItemChanged(position);
                                    }
                                });
                            } else {
                                queueHolder.favBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        MainActivity.Database.setFavorite(audio.getID());
                                        notifyItemChanged(position);
                                    }
                                });
                                queueHolder.favBtn.setImageResource(queueHolder.favBtn.getContext().getResources().getIdentifier("drawable/"+"heart_white", null, queueHolder.favBtn.getContext().getPackageName()));



                            }
                        }else{
                            queueHolder.favBtn.setVisibility(View.INVISIBLE);
                        }
                        final Context context=queueHolder.title.getContext();
                        if(audio.getID()== ((NowPlayingActivity)queueHolder.title.getContext()).currentAudioID)
                        {
                            queueHolder.title.setTextColor(context.getResources().getColor(R.color.colorAccent));
                            queueHolder.artist.setTextColor(context.getResources().getColor(R.color.colorAccent));
                            queueHolder.isplaying.setVisibility(View.VISIBLE);
                        }else
                            queueHolder.isplaying.setVisibility(View.INVISIBLE);

                        PushDownAnim.setPushDownAnimTo(queueHolder.option);
                        queueHolder.option.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            //creating a popup menu
                            PopupMenu popup = new PopupMenu(queueHolder.option.getContext(), queueHolder.option);
                            //inflating menu from xml resource
                            popup.inflate(R.menu.popup_menu);
                            //adding click listener
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.one://play
                                            if(MainActivity.serviceBound)
                                                ((NowPlayingActivity) queueHolder.option.getContext()).playIndexFromQueue(position);
                                            return true;
                                        case R.id.two://add to playlsit
                                            ArrayList<Audio> toAdd=new ArrayList<>();
                                            toAdd.add(audio);
                                            PlaylistsDialog dialog = new PlaylistsDialog(((NowPlayingActivity)queueHolder.option.getContext()),toAdd,false);
                                            dialog.show();
                                            return true;
                                        case R.id.three://remove from this playlist if it's not an official
                                            switch (playlistToPopulate.getType()){
                                                case MainActivity.FAVORITE_PLAYLIST_TYPE:
                                                    ((NowPlayingActivity) queueHolder.option.getContext()).removeToQueue(position);
                                                        playlistToPopulate.getList().remove(position);
                                                        notifyItemRemoved(position);
                                                    break;
                                                case MainActivity.USER_PLAYLIST_TYPE:
                                                    ((NowPlayingActivity) queueHolder.option.getContext()).removeToQueue(position);
                                                    playlistToPopulate.getList().remove(position);
                                                    notifyItemRemoved(position);
                                                    break;
                                                case MainActivity.TEMPO_PLAYLIST_TYPE:
                                                    ((NowPlayingActivity) queueHolder.option.getContext()).removeToQueue(position);
                                                    playlistToPopulate.getList().remove(position);
                                                    notifyItemRemoved(position);
                                                    break;
                                            }
                                            return true;
                                        default:
                                            return false;
                                    }
                                }
                            });
                            //displaying the popup
                            popup.show();

                        }
                    });
            break;



            case TEMP_LIST_TYPE:
                    final Audio audios= playlistToPopulate.getList().get(position);
                    final QueueHolder queuHolder=(QueueHolder)holder;
                    queuHolder.title.setText(audios.getTitle());
                    queuHolder.artist.setText(audios.getArtist());
                    queuHolder.title.setTextColor(queuHolder.title.getContext().getResources().getColor(R.color.colorPrimary));
                    queuHolder.artist.setTextColor(queuHolder.title.getContext().getResources().getColor(R.color.colorSecondary));

                        queuHolder.layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainActivity.Database.removeAllTemporary();
                                MainActivity.Database.addTemporary(audios.getID());
                                ArrayList<Audio> n=new ArrayList<>();
                                n.add(audios);
                                MainActivity.TEMPO.setList(n);
                                ((MainActivity) v.getContext()).playNewQueue(MainActivity.TEMPO.getID(),0);
                            }
                        });

                        if(playlistToPopulate.getType()!=MainActivity.FAVORITE_PLAYLIST_TYPE) {
                            if (MainActivity.Database.getFavorite(audios.getID())) {
                                queuHolder.favBtn.setImageResource(queuHolder.favBtn.getContext().getResources().getIdentifier("drawable/"+"heart_accent", null, queuHolder.favBtn.getContext().getPackageName()));

                                queuHolder.favBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        MainActivity.Database.removeFavorite(audios.getID());
                                        notifyItemChanged(position);
                                    }
                                });
                            } else {
                                queuHolder.favBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        MainActivity.Database.setFavorite(audios.getID());
                                        notifyItemChanged(position);
                                    }
                                });
                                queuHolder.favBtn.setImageResource(queuHolder.favBtn.getContext().getResources().getIdentifier("drawable/"+"heart_white", null, queuHolder.favBtn.getContext().getPackageName()));

                            }
                        }else{
                            queuHolder.favBtn.setVisibility(View.INVISIBLE);
                        }

                    if(audios.getID()== MainActivity.currentAudioID)
                    {
                        queuHolder.title.setTextColor(queuHolder.title.getContext().getResources().getColor(R.color.colorAccent));
                        queuHolder.artist.setTextColor(queuHolder.title.getContext().getResources().getColor(R.color.colorAccent));
                        queuHolder.isplaying.setVisibility(View.VISIBLE);
                    }else
                        queuHolder.isplaying.setVisibility(View.INVISIBLE);


                        queuHolder.option.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                //creating a popup menu
                                PopupMenu popup = new PopupMenu(queuHolder.option.getContext(), queuHolder.option);
                                //inflating menu from xml resource
                                popup.inflate(R.menu.popup_menu_notplayer);
                                //adding click listener
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.one: // play this shit
                                                MainActivity.Database.removeAllTemporary();
                                                MainActivity.Database.addTemporary(audios.getID());
                                                ArrayList<Audio> n=new ArrayList<>();
                                                n.add(audios);
                                                MainActivity.TEMPO.setList(n);
                                                ((MainActivity) queuHolder.option.getContext()).playNewQueue(MainActivity.TEMPO.getID(),0);
                                                return true;
                                            case R.id.two:// add to queue
                                                ((MainActivity) queuHolder.option.getContext()).addToQueue(audios.getID());
                                                return true;
                                            case R.id.three: // add to playlist
                                                ArrayList<Audio> toAdd=new ArrayList<>();
                                                toAdd.add(audios);
                                                PlaylistsDialog dialog = new PlaylistsDialog(((MainActivity)queuHolder.option.getContext()),toAdd,false);
                                                dialog.show();
                                                return true;
                                            default:
                                                return false;
                                        }
                                    }
                                });
                                popup.show();

                            }
                    });
            break;



            case POPUP_LIST_TYPE:
                final Audio audioss= playlistToPopulate.getList().get(position);
                final QueueHolder queueeHolder=(QueueHolder)holder;
                queueeHolder.title.setText(audioss.getTitle());
                queueeHolder.artist.setText(audioss.getArtist());
                queueeHolder.title.setTextColor(queueeHolder.title.getContext().getResources().getColor(R.color.colorPrimaryDark));
                queueeHolder.artist.setTextColor(queueeHolder.title.getContext().getResources().getColor(R.color.colorPrimaryDark));

                queueeHolder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.Database.removeAllTemporary();
                        MainActivity.Database.addTemporary(audioss.getID());
                        ArrayList<Audio> n=new ArrayList<>();
                        n.add(audioss);
                        MainActivity.TEMPO.setList(n);
                        ((DetailActivity) v.getContext()).playNewQueue(MainActivity.TEMPO.getID(),0);
                    }
                });
                PushDownAnim.setPushDownAnimTo(queueeHolder.favBtn);
                if(playlistToPopulate.getType()!=MainActivity.FAVORITE_PLAYLIST_TYPE) {
                    if (MainActivity.Database.getFavorite(audioss.getID())) {
                        queueeHolder.favBtn.setImageResource(queueeHolder.favBtn.getContext().getResources().getIdentifier("drawable/"+"heart_selected", null, queueeHolder.favBtn.getContext().getPackageName()));

                        queueeHolder.favBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainActivity.Database.removeFavorite(audioss.getID());
                                notifyItemChanged(position);
                            }
                        });
                    } else {
                        queueeHolder.favBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                MainActivity.Database.setFavorite(audioss.getID());
                                notifyItemChanged(position);
                            }
                        });
                        queueeHolder.favBtn.setImageResource(queueeHolder.favBtn.getContext().getResources().getIdentifier("drawable/"+"heart_black", null, queueeHolder.favBtn.getContext().getPackageName()));

                    }
                }else{
                    queueeHolder.favBtn.setVisibility(View.INVISIBLE);
                }

                if(audioss.getID()==   ((DetailActivity) queueeHolder.option.getContext()).currentAudioID)
                {
                    queueeHolder.title.setTextColor(queueeHolder.title.getContext().getResources().getColor(R.color.colorGray));
                    queueeHolder.artist.setTextColor(queueeHolder.title.getContext().getResources().getColor(R.color.colorGray));
                    queueeHolder.isplaying.setVisibility(View.VISIBLE);
                }else
                    queueeHolder.isplaying.setVisibility(View.INVISIBLE);

                PushDownAnim.setPushDownAnimTo(queueeHolder.option);
                queueeHolder.option.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(queueeHolder.option.getContext(), queueeHolder.option);
                        //inflating menu from xml resource
                        if(playlistToPopulate.getType()!=MainActivity.USER_PLAYLIST_TYPE) {
                            popup.inflate(R.menu.popup_menu_notplayer);
                            //adding click listener
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.one: // play this shit
                                            MainActivity.Database.removeAllTemporary();
                                            MainActivity.Database.addTemporary(audioss.getID());
                                            ArrayList<Audio> n=new ArrayList<>();
                                            n.add(audioss);
                                            MainActivity.TEMPO.setList(n);
                                            ((DetailActivity) queueeHolder.option.getContext()).playNewQueue(MainActivity.TEMPO.getID(),0);
                                            return true;
                                        case R.id.two:// add to queue
                                            ((DetailActivity) queueeHolder.option.getContext()).addToQueue(audioss.getID());
                                            return true;
                                        case R.id.three: // add to playlist
                                            ArrayList<Audio> toAdd=new ArrayList<>();
                                            toAdd.add(audioss);
                                            PlaylistsDialog dialog = new PlaylistsDialog(((DetailActivity)queueeHolder.option.getContext()),toAdd,false);
                                            dialog.show();
                                            //todo show dialog for playlists selection
                                            return true;
                                        default:
                                            return false;
                                    }
                                }
                            });
                        }
                        else {
                            popup.inflate(R.menu.popup_menu_user);
                            //adding click listener
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.one: // play this shit
                                            MainActivity.Database.removeAllTemporary();
                                            MainActivity.Database.addTemporary(audioss.getID());
                                            ArrayList<Audio> n=new ArrayList<>();
                                            n.add(audioss);
                                            MainActivity.TEMPO.setList(n);
                                            ((DetailActivity) queueeHolder.option.getContext()).playNewQueue(MainActivity.TEMPO.getID(),0);
                                            return true;
                                        case R.id.two:// add to queue
                                            ((DetailActivity) queueeHolder.option.getContext()).addToQueue(audioss.getID());
                                            return true;
                                        case R.id.three: // remove from playlist
                                            MainActivity.Database.removePlaylistTrack(playlistToPopulate.getID(),audioss.getID());
                                            playlistToPopulate.getList().remove(position);
                                            if(playlistToPopulate.getList().isEmpty())
                                                MainActivity.Database.removePlaylist(playlistToPopulate.getID());
                                            notifyDataSetChanged();
                                            return true;
                                        default:
                                            return false;
                                    }
                                }
                            });
                        }
                        popup.show();

                    }
                });
            break;



            case TRACK_BUBBLE:
                final Audio track= playlistToPopulate.getList().get(position);
                final TrackBubbleHolder BubbleHolder=(TrackBubbleHolder)holder;
                BubbleHolder.title.setText(track.getTitle());
                BubbleHolder.artist.setText(track.getArtist());
                if(track.getImage()!=null)
                    Picasso.get().load(track.getImage()).resize(500,500).centerCrop().into(BubbleHolder.img);
                PushDownAnim.setPushDownAnimTo(BubbleHolder.layout);
                BubbleHolder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("STARTCOMMAND","track selected : "+track.getTitle()+ " id :"+track.getID());
                    MainActivity.Database.removeAllTemporary();
                    MainActivity.Database.addTemporary(track.getID());
                    ArrayList<Audio> n=new ArrayList<>();
                    n.add(track);
                    MainActivity.TEMPO.setList(n);
                    ((MainActivity) BubbleHolder.img.getContext()).playNewQueue(MainActivity.TEMPO.getID(),0);
                }
            });
                if(track.getID()==MainActivity.currentAudioID){
                    Log.e("THiS AUDIO IS PLAYING",track.getTitle()+" ID : "+track.getID());
                    BubbleHolder.isplayin.setVisibility(View.VISIBLE);
                    BubbleHolder.img.setBorderColor(BubbleHolder.img.getResources().getColor(R.color.colorAccent));
                }else{
                    BubbleHolder.isplayin.setVisibility(View.GONE);
                    BubbleHolder.img.setBorderColor(BubbleHolder.img.getResources().getColor(R.color.colorPrimaryDark));
                }




                break;
            case PLAYLIST_BUBBLE:
                final Playlist roundplaylist= playlists.get(position);
                final PlaylistBubbleHolder playlistBubble=(PlaylistBubbleHolder)holder;
                playlistBubble.title.setText(roundplaylist.getName());
                playlistBubble.counter.setText(String.valueOf(MainActivity.Database.getPlaylistCounter(roundplaylist.getID()))+" songs");
                if(!roundplaylist.getList().isEmpty())
                Picasso.get().load(roundplaylist.getList().get(0).getImage()).resize(500,500).centerCrop().into(playlistBubble.pic);
                if(roundplaylist.getList().size()>0)

                PushDownAnim.setPushDownAnimTo(playlistBubble.layout);
                playlistBubble.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int adapterPosition = playlistBubble.getAdapterPosition();
                        if (adapterPosition == RecyclerView.NO_POSITION) {
                            return;
                        }
                        Context context = v.getContext();
                        Intent intent = new Intent(context, DetailActivity.class);
                        Bundle bnd =new Bundle();
                        bnd.putSerializable("",0);
                        intent.putExtra("position", adapterPosition);
                        intent.putExtra("TYPE",roundplaylist.getType());
                        Log.e("ROUND","TYPE : "+roundplaylist.getType());


                        //// TODO: 25/04/2017 Use ActivityOptionsCompat to support pre-lollipop
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                /*int firstVisibleItemPosition =0;
                                int lastVisibleItemPosition = 0;
                                if(LayoutManagerType==LAYOUT_MANAGER_LINEAR) {
                                    firstVisibleItemPosition = ((LinearLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
                                    lastVisibleItemPosition = ((LinearLayoutManager) rv.getLayoutManager()).findLastVisibleItemPosition();
                                }
                                else if(LayoutManagerType==LAYOUT_MANAGER_GRID) {
                                    firstVisibleItemPosition = ((GridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition()/2;
                                    lastVisibleItemPosition = ((GridLayoutManager) rv.getLayoutManager()).findLastVisibleItemPosition()/2;
                                }*/
                            List<Pair<View, String>> pairs = new ArrayList<Pair<View, String>>();
                            // for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
                            //   PlaylistBubbleHolder holderForAdapterPosition = (PlaylistBubbleHolder) rv.findViewHolderForAdapterPosition(i);

                            View itemView = playlistBubble.pic;
                            itemView.setTransitionName("tab_"+position);
                            pairs.add(Pair.create(itemView, "tab_" + position));
                            // }
                            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(((MainActivity)context), pairs.toArray(new Pair[]{})).toBundle();
                            context.startActivity(intent, bundle);
                        } else {
                            context.startActivity(intent);
                        }
                    }
                });
                break;
            case SQUARE_PLAYLIST:
                final Playlist playlist= playlists.get(position);
                final SquarePlaylistHolder SquareHolder=(SquarePlaylistHolder)holder;
                SquareHolder.title.setText(playlist.getName());
                SquareHolder.counter.setText(String.valueOf(MainActivity.Database.getPlaylistCounter(playlist.getID()))+" songs");
                if(!playlist.getList().isEmpty())
                    Picasso.get().load(playlist.getList().get(0).getImage()).resize(500,500).centerCrop().into(SquareHolder.image);

                PushDownAnim.setPushDownAnimTo(SquareHolder.layout);
                SquareHolder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int adapterPosition = SquareHolder.getAdapterPosition();
                        if (adapterPosition == RecyclerView.NO_POSITION) {
                            return;
                        }
                        Context context = v.getContext();
                        Intent intent = new Intent(context, DetailActivity.class);
                        Bundle bnd =new Bundle();
                        bnd.putSerializable("",0);
                        intent.putExtra("position", adapterPosition);
                        intent.putExtra("TYPE",playlist.getType());
                        Log.e("SQUARE","TYPE : "+playlist.getType());

                        //// TODO: 25/04/2017 Use ActivityOptionsCompat to support pre-lollipop
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                /*int firstVisibleItemPosition =0;
                                int lastVisibleItemPosition = 0;
                                if(LayoutManagerType==LAYOUT_MANAGER_LINEAR) {
                                    firstVisibleItemPosition = ((LinearLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
                                    lastVisibleItemPosition = ((LinearLayoutManager) rv.getLayoutManager()).findLastVisibleItemPosition();
                                }
                                else if(LayoutManagerType==LAYOUT_MANAGER_GRID) {
                                    firstVisibleItemPosition = ((GridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition()/2;
                                    lastVisibleItemPosition = ((GridLayoutManager) rv.getLayoutManager()).findLastVisibleItemPosition()/2;
                                }*/
                            List<Pair<View, String>> pairs = new ArrayList<Pair<View, String>>();
                            // for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
                            //   PlaylistBubbleHolder holderForAdapterPosition = (PlaylistBubbleHolder) rv.findViewHolderForAdapterPosition(i);

                            View itemView = SquareHolder.image;
                            itemView.setTransitionName("tab_"+position);
                            pairs.add(Pair.create(itemView, "tab_" + position));
                            // }
                            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(((MainActivity)context), pairs.toArray(new Pair[]{})).toBundle();
                            context.startActivity(intent/*, bundle*/);
                        } else {
                            context.startActivity(intent);
                        }
                    }
                });
                break;
        }

    }

    @Override
    public int getItemViewType(int position) {
        return Type;
    }


    @Override
    public int getItemCount() {

        if(Type!=SQUARE_PLAYLIST && Type!=PLAYLIST_BUBBLE){
            if(playlistToPopulate.getList()!=null)
            return playlistToPopulate.getList().size();
            else return 0;
        }
        else{
            if(playlists!=null)
           return playlists.size();
            else
                return 0;}
    }
}
