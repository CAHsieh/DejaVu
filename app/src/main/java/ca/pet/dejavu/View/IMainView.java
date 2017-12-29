package ca.pet.dejavu.View;

import com.facebook.share.model.ShareContent;

/**
 * Created by CAMac on 2017/11/20.
 * MainActivity interface
 * 定義需實現的MainActivity的接口方法
 */

public interface IMainView {

    void showSnack(String message);

    void showProgress();

    void dismissProgress();

    void showTitleDialog(String originTitle);

    void displayNoContentTextView(boolean display);

    void showMessengerDialog(ShareContent content);

    void notifyDataSetChanged();

    void notifyItemRemoved(int position);

    void notifyItemChanged(int position);

    void notifyInsertCompleted();

    void requestPermission();
}
