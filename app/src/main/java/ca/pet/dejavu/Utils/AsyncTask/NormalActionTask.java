package ca.pet.dejavu.Utils.AsyncTask;

import android.os.AsyncTask;

import ca.pet.dejavu.Model.DataEntityModel;
import ca.pet.dejavu.Model.IDataModel;
import ca.pet.dejavu.Presenter.MainPresenter;
import ca.pet.dejavu.Utils.Table.DataEntity;

/**
 * Created by CAMac on 2017/12/15.
 * 用於連結資料庫操作的async task
 */

public class NormalActionTask extends AsyncTask<IDataModel, Void, Integer> {

    private MainPresenter presenter;

    private int actionId;
    private DataEntity entity;
    private String title;

    public NormalActionTask(MainPresenter presenter, int actionId, DataEntity entity, String title) {
        this.presenter = presenter;
        this.actionId = actionId;
        this.entity = entity;
        this.title = title;
    }

    /**
     * 前置作業
     * 開啟ProgressDialog
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (presenter != null) {
            presenter.showProgress();
        }
    }

    /**
     * 進行資料庫操作
     *
     * @param iModel interface of DataEntity model
     * @return position
     */
    @Override
    protected Integer doInBackground(IDataModel... iModel) {
        int tag = iModel[0].doAction(actionId, entity, title);
        if (actionId == DataEntityModel.ACTION_INSERT) {
            tag = 1;
        }
        return tag;
    }

    /**
     * 後續處理
     * 調用afterDoAction
     * 關閉ProgressDialog
     *
     * @param position index
     */
    @Override
    protected void onPostExecute(Integer position) {
        super.onPostExecute(position);
        if (presenter != null) {
            presenter.afterDoAction(actionId, position);
            presenter.dismissProgress();
        }
    }
}