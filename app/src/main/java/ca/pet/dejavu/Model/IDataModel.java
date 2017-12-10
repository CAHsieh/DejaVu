package ca.pet.dejavu.Model;

import android.support.annotation.Nullable;

import ca.pet.dejavu.Utils.Table.DataEntity;

/**
 * Created by CAMac on 2017/11/20.
 * DataEntityModel interface
 * 定義需實現的LinkEntityModel的接口方法
 */

public interface IDataModel {

    void setQueryType(int type);

    int doAction(int action_Id, @Nullable DataEntity entity, @Nullable String title);

    int presenting_size();

    DataEntity getEntity(int i);

    void cancelTextCrawler();
}
