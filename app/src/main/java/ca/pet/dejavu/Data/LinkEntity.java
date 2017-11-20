package ca.pet.dejavu.Data;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

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
    private String thumbnailUrl;
    @Generated(hash = 2103337338)
    public LinkEntity(Long Id, String title, String link, String thumbnailUrl) {
        this.Id = Id;
        this.title = title;
        this.link = link;
        this.thumbnailUrl = thumbnailUrl;
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
    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

}
