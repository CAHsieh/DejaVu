package ca.pet.dejavu.Presenter;

import android.content.Context;

import ca.pet.dejavu.Data.LinkEntity;

/**
 * Created by CAMac on 2017/11/20.
 * MainPresenter interface
 */

public interface IMainPresenter {

    void afterDoAction(int actionId, int tag);

    void queryAll();

    void addNewUrl(String title, String url);

    LinkEntity getEntity(int position);

    int getPresentingSize();

    void editTitle(String title);

    void OnLinkSelected(LinkEntity entity);

    void OnLinkDelete(LinkEntity entity);

    void OnTitleModifyClick(LinkEntity entity);

    void onSendClick(Context context, String tag);
}
