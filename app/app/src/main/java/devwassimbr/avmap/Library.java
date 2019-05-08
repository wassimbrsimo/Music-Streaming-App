package devwassimbr.avmap;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gigamole.navigationtabstrip.NavigationTabStrip;

import java.util.ArrayList;
import java.util.List;


public class Library extends Fragment {

    public static final int COLNUMBER=3;
    private ViewPager ViewPager;
    PagerAdapter adapter;
    LibraryArtists mArtist;
    LibraryYears mYears;
    LibrarySongs mSongs;
    LibraryPlaylist mPlaylists;

    public Library() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        ViewPager = view.findViewById(R.id.viewpager2);
        setupViewPager(ViewPager);
        ViewPager.setOffscreenPageLimit(3);
        ViewPager.setCurrentItem(0);
        NavigationTabStrip navigationTabStrip = view.findViewById(R.id.nts);
        navigationTabStrip.setTitles("Artists", "Years", "Recent","Playlist");
        navigationTabStrip.setStripType(NavigationTabStrip.StripType.POINT);
        navigationTabStrip.setViewPager(ViewPager);
        navigationTabStrip.setTitleSize(28);
        navigationTabStrip.setStripColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        navigationTabStrip.setStripWeight(12);
        navigationTabStrip.setStripFactor(2);
        navigationTabStrip.setStripGravity(NavigationTabStrip.StripGravity.BOTTOM);
        navigationTabStrip.setCornersRadius(3);
        navigationTabStrip.setAnimationDuration(300);
        navigationTabStrip.setInactiveColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        navigationTabStrip.setActiveColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        navigationTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        ((LibraryArtists)adapter.getItem(0)).refreshView();
                        break;
                    case 1:
                        ((LibraryYears)adapter.getItem(1)).refreshView();
                        break;
                    case 2:
                        ((LibrarySongs)adapter.getItem(2)).refreshView(true);
                        break;
                    case 3:
                        ((LibraryPlaylist)adapter.getItem(3)).refreshView();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



        return view;
    }

    public void notifyDataChanger(){
        mSongs.refreshView(false);
    }
    private void setupViewPager(ViewPager viewPager) {
        adapter = new PagerAdapter(getActivity().getSupportFragmentManager());
        mArtist = new LibraryArtists();
        mYears = new LibraryYears();
        mSongs = new LibrarySongs();
        mPlaylists = new LibraryPlaylist();
        adapter.addFragment(mArtist, "Artists");
        adapter.addFragment(mYears, "Years");
        adapter.addFragment(mSongs, "Recent");
        adapter.addFragment(mPlaylists, "Playlist");
        viewPager.setAdapter(adapter);
    }
}
