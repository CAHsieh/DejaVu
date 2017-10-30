package ca.pet.dejavu.Model;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by CAHSIEH on 2017/10/29.
 * Data Entity
 */

@Entity
public class LinkEntity {

    @Id(autoincrement = true)
    private Long Id;

    private String title;
    private String link;
    @Generated(hash = 745129294)
    public LinkEntity(Long Id, String title, String link) {
        this.Id = Id;
        this.title = title;
        this.link = link;
    }
    @Generated(hash = 1585243722)
    public LinkEntity() {
    }
    public Long getId() {
        return this.Id;
    }
    public void setId(Long Id) {
        this.Id = Id;
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getLink() {
        return this.link;
    }
    public void setLink(String link) {
        this.link = link;
    }
    

}
