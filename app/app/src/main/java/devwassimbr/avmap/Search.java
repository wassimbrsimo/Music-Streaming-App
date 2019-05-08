package devwassimbr.avmap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;

public class Search extends Fragment {
    RecyclerView recyclerView;
    AudioAdapter tracksAdapter;
    EditText searchText;
    Playlist songsList;


    public Search() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_search, container, false);

       searchText= view.findViewById(R.id.searchtext);
       searchText.addTextChangedListener(new TextWatcher() {

           public void afterTextChanged(Editable s) {
           }

           public void beforeTextChanged(CharSequence s, int start,
                                         int count, int after) {
           }

           public void onTextChanged(CharSequence s, int start,
                                     int before, int count) {
               songsList.getList().clear();
               Playlist temp=new Playlist();
               if(searchText.getText().toString().equals(""))
                   temp.setList(new ArrayList<Audio>());
                else
               temp=MainActivity.Database.getAudioSearch(searchText.getText().toString());

               songsList.getList().addAll(temp.getList());
               tracksAdapter.notifyDataSetChanged();
           }
       });

        songsList = new Playlist("Search",-1,-1,new ArrayList<Audio>());
        recyclerView = view.findViewById(R.id.trackView);
        tracksAdapter = new AudioAdapter(songsList, AudioAdapter.TEMP_LIST_TYPE,null,null,0);
        RecyclerView.LayoutManager newReleaseManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(newReleaseManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(tracksAdapter);
        return view;
    }
    public void notifyDataChanger(){
        tracksAdapter.notifyDataSetChanged();
    }



}
