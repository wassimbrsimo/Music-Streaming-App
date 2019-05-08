package devwassimbr.avmap;

import java.io.Serializable;

public class Audio implements Serializable {

    private String data,title,image,artist;
    private int ID;

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    private int date;


    public Audio(String data, String title, String img, String artist, int ID ,int date) {
        this.data = data;
        this.title = title;
        this.image = img;
        this.artist = artist;
        this.ID=ID;
        this.date=date;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}