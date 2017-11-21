package ca.pet.dejavu.Model;

import android.support.annotation.Nullable;

import ca.pet.dejavu.Data.LinkEntity;

/**
 * Created by CAMac on 2017/11/20.
 * LinkEntityModel interface
 */

public interface ILinkModel {
    int doAction(int action_Id, @Nullable LinkEntity entity, @Nullable String title);

    int table_size();

    int presenting_size();

    LinkEntity getEntity(int i);
}
