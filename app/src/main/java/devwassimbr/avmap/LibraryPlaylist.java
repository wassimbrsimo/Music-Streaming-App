package devwassimbr.avmap;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class LibraryPlaylist extends Fragment {
    RecyclerView recyclerView;
    AudioAdapter viewAdapter;
    ArrayList<Playlist> playlissts;
    public LibraryPlaylist() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_playlist, container, false);

        recyclerView = view.findViewById(R.id.rv2);
        playlissts=new ArrayList<>();
        playlissts.add(MainActivity.Database.getFavoritePlaylist());
        playlissts.addAll(MainActivity.Database.getPlaylistByType(MainActivity.USER_PLAYLIST_TYPE));

        viewAdapter = new AudioAdapter(null, AudioAdapter.PLAYLIST_BUBBLE,playlissts,recyclerView,AudioAdapter.LAYOUT_MANAGER_GRID);
        RecyclerView.LayoutManager newReleaseManager = new GridLayoutManager(getContext(), Library.COLNUMBER);
        recyclerView.setLayoutManager(newReleaseManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(viewAdapter);

        return view;
    }
    public void refreshView(){
        playlissts.clear();
        playlissts.add(MainActivity.Database.getFavoritePlaylist());
        playlissts.addAll(MainActivity.Database.getPlaylistByType(MainActivity.USER_PLAYLIST_TYPE));
        viewAdapter.notifyDataSetChanged();
    }

}
