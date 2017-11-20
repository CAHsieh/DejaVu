package ca.pet.dejavu.Presenter;

import android.content.Context;
import android.support.v7.widget.SearchView;

import ca.pet.dejavu.Data.LinkEntity;

/**
 * Created by CAMac on 2017/11/20.
 */

public interface IMainPresenter {

    void queryAll();

    void addNewUrl(String title, String url);

    void editTitle(String title);

    void OnLinkSelected(LinkEntity entity);

    void OnLinkDelete(LinkEntity entity);

    void OnTitleModifyClick(LinkEntity entity);

    LinkEntity getEntity(int position);

    int getPresentingSize();

    void afterDoAction(int actionId, int tag);

    SearchView.OnQueryTextListener getOnQueryTextListener();

    void onSendClick(Context context, String tag);
}
