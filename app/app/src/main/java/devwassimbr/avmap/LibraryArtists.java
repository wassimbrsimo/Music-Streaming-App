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


public class LibraryArtists extends Fragment {
    RecyclerView recyclerView;
    AudioAdapter viewAdapter;
    public LibraryArtists() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_artists, container, false);

        recyclerView = view.findViewById(R.id.rv1);
        viewAdapter = new AudioAdapter(null, AudioAdapter.PLAYLIST_BUBBLE,MainActivity.Database.getPlaylistByType(MainActivity.ARTISTS_PLAYLIST_TYPE),recyclerView,AudioAdapter.LAYOUT_MANAGER_GRID);
        RecyclerView.LayoutManager newReleaseManager = new GridLayoutManager(getContext(), Library.COLNUMBER);
        recyclerView.setLayoutManager(newReleaseManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(viewAdapter);

        return view;
    }
    public void refreshView(){
        viewAdapter.notifyDataSetChanged();
    }

}
