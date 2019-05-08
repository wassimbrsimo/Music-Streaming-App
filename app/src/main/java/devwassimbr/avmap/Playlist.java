package devwassimbr.avmap;

import java.util.ArrayList;

public class Playlist {
    private String name;
    private int ID;
    private ArrayList<Audio> list;
    private int Type;

    public Playlist(){

    }

    public Playlist(String name, int ID, int Type, ArrayList<Audio> list) {
        this.name = name;
        this.ID = ID;
        this.Type = Type;
        this.list = list;
    }
    public ArrayList<Audio> getList() {
        return list;
    }

    public void setList(ArrayList<Audio> list) {
        this.list = list;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        this.Type = type;
    }

}
