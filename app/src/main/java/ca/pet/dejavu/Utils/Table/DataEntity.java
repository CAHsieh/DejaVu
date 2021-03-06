package ca.pet.dejavu.Utils.Table;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by CAHSIEH on 2017/10/29.
 * 用於儲存網頁資料的Table
 */

@Entity
public class DataEntity {

    @Id(autoincrement = true)
    private Long Id;
    private Long parent_Id;

    private int type;
    private String title;
    private String uri;
    private String thumbnailUrl;
    private float imageSize;

    private boolean isDelete;

    @Generated(hash = 2052968792)
    public DataEntity(Long Id, Long parent_Id, int type, String title, String uri,
            String thumbnailUrl, float imageSize, boolean isDelete) {
        this.Id = Id;
        this.parent_Id = parent_Id;
        this.type = type;
        this.title = title;
        this.uri = uri;
        this.thumbnailUrl = thumbnailUrl;
        this.imageSize = imageSize;
        this.isDelete = isDelete;
    }

    @Generated(hash = 1892108943)
    public DataEntity() {
    }

    public Long getId() {
        return this.Id;
    }

    public void setId(Long Id) {
        this.Id = Id;
    }

    public Long getParent_Id() {
        return this.parent_Id;
    }

    public void setParent_Id(Long parent_Id) {
        this.parent_Id = parent_Id;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public float getImageSize() {
        return this.imageSize;
    }

    public void setImageSize(float imageSize) {
        this.imageSize = imageSize;
    }

    public boolean getIsDelete() {
        return this.isDelete;
    }

    public void setIsDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

}
