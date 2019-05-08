package devwassimbr.avmap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.thekhaeng.pushdownanim.PushDownAnim;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsDialog extends Dialog {

    private Activity c;
    private PlaylistsDialog d;
    private Button add,close;
    private EditText text;
    private boolean isNew;
    private ArrayList<Audio> toAdd;



    public PlaylistsDialog(Activity a,ArrayList<Audio> toAdd,boolean isNew) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.isNew=isNew;
        this.toAdd=toAdd;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(!isNew) {
            setContentView(R.layout.playlists_dialog);
            ListView list = findViewById(R.id.listview);
            list.setAdapter(new PlaylistDialogAdapter(getContext(),MainActivity.Database.getPlaylistByType(MainActivity.USER_PLAYLIST_TYPE)));
           }

        else {
            setContentView(R.layout.newplaylists_dialog);
            add = findViewById(R.id.save);
            text=findViewById(R.id.playlistName);
            PushDownAnim.setPushDownAnimTo(add).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!text.getText().toString().equals("")) {
                        Playlist userplylst = new Playlist(text.getText().toString(), 0, MainActivity.USER_PLAYLIST_TYPE, toAdd);
                        MainActivity.Database.createPlaylist(userplylst);
                    }
                    else {
                        Toast.makeText(getContext(),"insert playlist name",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        close = findViewById(R.id.close);
        PushDownAnim.setPushDownAnimTo(close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        }

    void onPlaylistSelected(int ID){
        for (Audio Track:toAdd
             ) {
            MainActivity.Database.addPlaylistTrack(Track.getID(),ID);
        }
        dismiss();
    }
    public class PlaylistDialogAdapter extends ArrayAdapter<Playlist> {

        private Context mContext;
        private List<Playlist> playlists;

        public PlaylistDialogAdapter(@NonNull Context context, List<Playlist> list) {
            super(context, -1,list);
            mContext = context;
            playlists = list;
        }

        @NonNull
        @Override
        public View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.playlist_item, parent, false);
            ((TextView)rowView.findViewById(R.id.playlist_name)).setText(playlists.get(position).getName());
            rowView.findViewById(R.id.bg).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPlaylistSelected(playlists.get(position).getID());
                }
            });
            return rowView;
        }
    }

}