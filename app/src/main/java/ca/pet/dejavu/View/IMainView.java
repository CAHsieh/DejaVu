package ca.pet.dejavu.View;

import com.facebook.share.model.ShareLinkContent;

/**
 * Created by CAMac on 2017/11/20.
 */

public interface IMainView {

    void notifyDataSetChanged();
    void notifyItemRemoved(int position);
    void notifyItemChanged(int position);

    void showSnack(String message);
    void showMessageDialog(ShareLinkContent content);
    void showProgress();
    void dismissProgress();

    void showTitleDialog(String originTitle);
    void displayNoContentTextView(boolean display);
}
