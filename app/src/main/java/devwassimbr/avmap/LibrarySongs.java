package devwassimbr.avmap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;


public class LibrarySongs extends Fragment {

    RecyclerView recyclerView;
    AudioAdapter viewAdapter;
    Playlist historyList;
    public LibrarySongs() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_songs, container, false);

        recyclerView = view.findViewById(R.id.rv3);
        historyList=MainActivity.Database.getHistoryPlaylist();
        Collections.reverse(historyList.getList());
        viewAdapter = new AudioAdapter(historyList, AudioAdapter.TEMP_LIST_TYPE,null,recyclerView,AudioAdapter.LAYOUT_MANAGER_LINEAR);
        RecyclerView.LayoutManager newReleaseManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(newReleaseManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(viewAdapter);
        return view;
    }

    public void refreshView(boolean wholeThing){
        if(wholeThing) {
            historyList.getList().clear();
            historyList.setList(MainActivity.Database.getHistoryPlaylist().getList());
            Collections.reverse(historyList.getList());
        }
        viewAdapter.notifyDataSetChanged();
    }
}
