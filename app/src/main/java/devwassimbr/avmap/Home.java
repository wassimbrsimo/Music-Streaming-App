package devwassimbr.avmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.thekhaeng.pushdownanim.PushDownAnim;

import static devwassimbr.avmap.MainActivity.ARTISTS;
import static devwassimbr.avmap.MainActivity.NEW_RELEASES;
import static devwassimbr.avmap.MainActivity.YEARS;


public class Home extends Fragment {

    RecyclerView NewView,ArtistView,YearsView,Playlists;
    AudioAdapter artistsViewAdapter,newViewAdapter,yearsViewAdapter;

    Button mNewReleases,mArtists,mYears;
    public Home() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragement_home, container, false);
        mArtists=view.findViewById(R.id.artistsbtn);
        mNewReleases=view.findViewById(R.id.newbtn);
        mYears=view.findViewById(R.id.yearbtn);
        PushDownAnim.setPushDownAnimTo(mNewReleases).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).playNewQueue(MainActivity.NEW_RELEASES.getID(),0);
            }
        });
        PushDownAnim.setPushDownAnimTo(mArtists).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(),DetailActivity.class);
                intent.putExtra("ID",MainActivity.ARTISTS.get(0).getID()).putExtra("TYPE",MainActivity.ARTISTS_PLAYLIST_TYPE).putExtra("Title",MainActivity.ARTISTS.get(0).getName());
                startActivity(intent);
            }
        });
        PushDownAnim.setPushDownAnimTo(mYears).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(),DetailActivity.class);
                intent.putExtra("ID",MainActivity.YEARS.get(0).getID()).putExtra("TYPE",MainActivity.YEARS_PLAYLIST_TYPE).putExtra("Title",MainActivity.YEARS.get(0).getName());
                startActivity(intent);
            }
        });

        NewView = view.findViewById(R.id.newReleasesView);
        RecyclerView.LayoutManager newReleaseManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        newViewAdapter = new AudioAdapter(NEW_RELEASES, AudioAdapter.TRACK_BUBBLE,null,null,0);
        NewView.setLayoutManager(newReleaseManager);
        NewView.setItemAnimator(new DefaultItemAnimator());
        NewView.setAdapter(newViewAdapter);

        ArtistView = view.findViewById(R.id.artistsView);
        artistsViewAdapter = new AudioAdapter(null, AudioAdapter.SQUARE_PLAYLIST,ARTISTS,null,0);
        RecyclerView.LayoutManager artistsManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        ArtistView.setLayoutManager(artistsManager);
        ArtistView.setItemAnimator(new DefaultItemAnimator());
        ArtistView.setAdapter(artistsViewAdapter);

        YearsView = view.findViewById(R.id.yearsView);
        yearsViewAdapter = new AudioAdapter(null, AudioAdapter.SQUARE_PLAYLIST,YEARS,null,0);
        RecyclerView.LayoutManager yearsManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        YearsView.setLayoutManager(yearsManager);
        YearsView.setItemAnimator(new DefaultItemAnimator());
        YearsView.setAdapter(yearsViewAdapter);
        newViewAdapter.notifyDataSetChanged();
        artistsViewAdapter.notifyDataSetChanged();
        yearsViewAdapter.notifyDataSetChanged();


        return view;
    }

    public void updateView(boolean wholething){
        if(wholething){
            artistsViewAdapter.notifyDataSetChanged();
            yearsViewAdapter.notifyDataSetChanged();
        }
        newViewAdapter.notifyDataSetChanged();
    }


}
