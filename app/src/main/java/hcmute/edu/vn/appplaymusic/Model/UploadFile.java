package hcmute.edu.vn.appplaymusic.Model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class UploadFile implements Serializable {
    private String SongTitle, songDuration, songLink, mkey, singer, imageLink;

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public UploadFile(){}
    public UploadFile(String songTitle, String songDuration, String songLink, String singer, String imageLink) {
        if(songTitle.trim().equals("")){
            songTitle = "No title";
        }
        this.SongTitle = songTitle;
        this.songDuration = songDuration;
        this.songLink = songLink;
        this.singer = singer;
        this.imageLink = imageLink;
    }

    public void setSongTitle(String songTitle) {
        SongTitle = songTitle;
    }

    public void setSongDuration(String songDuration) {
        this.songDuration = songDuration;
    }

    public void setSongLink(String songLink) {
        this.songLink = songLink;
    }
    @Exclude
    public void setMkey(String mkey) {
        this.mkey = mkey;
    }

    public String getSongTitle() {
        return SongTitle;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public String getSongLink() {
        return songLink;
    }

    @Exclude
    public String getMkey() {
        return mkey;
    }
}
