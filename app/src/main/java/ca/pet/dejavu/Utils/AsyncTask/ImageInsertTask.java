package ca.pet.dejavu.Utils.AsyncTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import ca.pet.dejavu.Model.IDataModel;
import ca.pet.dejavu.Presenter.MainPresenter;
import ca.pet.dejavu.Utils.DBService;
import ca.pet.dejavu.Utils.MyApplication;
import ca.pet.dejavu.Utils.SPConst;
import ca.pet.dejavu.Utils.Table.DataEntity;
import ca.pet.dejavu.Utils.Table.DataEntityDao;

/**
 * Created by CAMac on 2017/12/15.
 * 用於新增圖片的Async Task.
 */

public class ImageInsertTask extends NormalActionTask {

    private Uri[] uris;

    public ImageInsertTask(MainPresenter presenter, int actionId, @NonNull Uri... uris) {
        super(presenter, actionId, null, null);
        this.uris = uris;
    }

    @Override
    protected Integer doInBackground(IDataModel... iDataModels) {

        Context context = MyApplication.getContext();
        DataEntityDao entityDao = DBService.getInstance().getDataEntityDao();
        int successCount = 0;

        File dir = checkAndMkDir(context);
        if (null == dir) {
            return 0;
        }

        for (Uri uri : uris) {
            try {
                Bitmap bitmap = Glide.with(context).load(uri).asBitmap().into(200, 200).get();
                if (bitmap != null) {

                    DataEntity entity = new DataEntity();
                    entity.setType(SPConst.VISIBLE_TYPE_IMAGE);
                    Long id = entityDao.insert(entity);

                    File file = new File(dir, "image_" + id + ".webp");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 100, baos);

                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                    byte[] outBytes = baos.toByteArray();
                    outputStream.write(outBytes);
                    outputStream.flush();
                    outputStream.close();
                    baos.close();
                    bitmap.recycle();

                    entity.setTitle(file.getName());
                    entity.setUri(FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file).toString());
                    entity.setThumbnailUrl(file.getAbsolutePath());
                    entity.setImageSize(Math.round((outBytes.length / 1024f) * 100) / 100f);
                    entityDao.update(entity);

                    successCount++;
                }
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
                Log.e("DEJAVU", "Load Bitmap Error.");
                Log.e("DEJAVU", "Uri: " + uri.toString());
            }
        }
        return successCount;
    }

    private File checkAndMkDir(Context context) {
        File path = new File(context.getFilesDir(), "images");
        int tryMkDirTimes = 0;
        while (!path.exists() && !path.mkdir()) {
            if (tryMkDirTimes > 4) {
                return null;
            }
            tryMkDirTimes++;
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
        return path;
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
