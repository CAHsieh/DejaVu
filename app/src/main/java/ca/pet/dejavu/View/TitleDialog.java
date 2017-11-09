package ca.pet.dejavu.View;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import ca.pet.dejavu.Model.LinkEntity;
import ca.pet.dejavu.R;

/**
 * Created by CAMac on 2017/11/3.
 * <p>
 * Dialog for edit title name.
 */

class TitleDialog {

    private AppCompatActivity appCompatActivity;
    private LinkEntity entity;

    private OnTitleSetCallback onTitleSetCallback = null;

    TitleDialog(AppCompatActivity appCompatActivity, LinkEntity entity) {
        this.appCompatActivity = appCompatActivity;
        this.entity = entity;
    }

    void setOnTitleActionCallback(OnTitleSetCallback onTitleSetCallback) {
        this.onTitleSetCallback = onTitleSetCallback;
    }

    @SuppressLint("InflateParams")
    void show() {

        final View item = LayoutInflater.from(appCompatActivity).inflate(R.layout.dialog_edittitle, null);
        ((EditText) item.findViewById(R.id.dialog_edit_title)).setText(entity.getTitle());

        new AlertDialog.Builder(appCompatActivity, R.style.AlertDialogCustom)
                .setView(item)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = ((EditText) item.findViewById(R.id.dialog_edit_title)).getText().toString();
                        entity.setTitle(title);
                        if (onTitleSetCallback != null)
                            onTitleSetCallback.OnTitleSet(entity);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public interface OnTitleSetCallback {
        void OnTitleSet(LinkEntity entity);
    }
}
