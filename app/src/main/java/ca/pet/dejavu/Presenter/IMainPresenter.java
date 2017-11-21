package ca.pet.dejavu.Presenter;

import ca.pet.dejavu.Utils.Table.LinkEntity;

/**
 * Created by CAMac on 2017/11/20.
 * MainPresenter interface
 * 定義需實現的MainPresenter的接口方法
 */

public interface IMainPresenter {

    void queryAll();

    void addNewUrl(String title, String url);

    LinkEntity getEntity(int position);

    int getPresentingSize();

    void editTitle(String title);

    void OnLinkSelected(LinkEntity entity);

    void OnLinkDelete(LinkEntity entity);

    void OnTitleModifyClick(LinkEntity entity);

    void onSendClick(String tag);
}
