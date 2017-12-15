package ca.pet.dejavu.Presenter;

import android.net.Uri;

import ca.pet.dejavu.Utils.Table.DataEntity;

/**
 * Created by CAMac on 2017/11/20.
 * MainPresenter interface
 * 定義需實現的MainPresenter的接口方法
 */

public interface IMainPresenter {

    void setQueryType(int type);

    void queryAll();

    void addNewUrl(String title, String url);

    void addNewImage(Uri... uris);

    DataEntity getEntity(int position);

    int getPresentingSize();

    void editTitle(String title);

    void onLinkSelected(int position);

    void onLinkDelete(int position);

    void onTitleModifyClick(int position);

    void onSendClick(String tag);

    void cancelTextCrawler();
}
