package ca.pet.dejavu.View;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import ca.pet.dejavu.Utils.Table.LinkEntity;
import ca.pet.dejavu.Presenter.MainPresenter;
import ca.pet.dejavu.R;

/**
 * Created by CAHSIEH on 2017/10/29.
 * Adapter of RecycleView
 * 用於顯示網頁資料
 */

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

    private MainPresenter presenter = null;

    private ImageView lastSelectedImageView;

    ContentAdapter(MainPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item_view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_content, parent, false);
        return new ViewHolder(item_view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(presenter.getEntity(position).getTitle());//設置標題
        holder.link.setText(presenter.getEntity(position).getLink());//設置網址
        holder.D.setOnClickListener(mClickAction);
        holder.parent.setOnClickListener(mClickAction);
        holder.edit.setOnClickListener(onEditClick);
        holder.delete.setOnClickListener(onDeleteClick);
        holder.entity = presenter.getEntity(position);

        //若有縮圖內容則使用Glide library將其顯示
        if (holder.entity.getThumbnailUrl() != null && !holder.entity.getThumbnailUrl().equals("")) {
            Log.i("Glide", "Id: " + holder.entity.getId() + " url: " + holder.entity.getThumbnailUrl());
            Glide.with(holder.thumbnail.getContext())
                    .load(holder.entity.getThumbnailUrl())
                    .error(R.drawable.d)//load失敗的Drawable
                    .fitCenter()//中心fit, 以原本圖片的長寬為主
                    .placeholder(R.drawable.d_no_content)
                    .into(holder.thumbnail);
        } else {
            Log.e("Glide", "not show. Id: " + holder.entity.getId() + " url: " + holder.entity.getThumbnailUrl());
            holder.thumbnail.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return presenter.getPresentingSize();
    }

    /**
     * 點擊選取
     * 呼叫present.OnLinkSelected來保留/取消目前選取的內容。
     */
    private View.OnClickListener mClickAction = (v) -> {

        if (lastSelectedImageView != null)
            lastSelectedImageView.setSelected(false);

        ViewHolder viewHolder = (ViewHolder) v.getTag();
        if (viewHolder.D.equals(lastSelectedImageView)) {
            lastSelectedImageView = null;
        } else {
            lastSelectedImageView = viewHolder.D;
            lastSelectedImageView.setSelected(true);
        }
        presenter.onLinkSelected(viewHolder.entity);
    };

    /**
     * 編輯標題
     * 呼叫presenter.OnTitleModifyClick並傳入要修改標題的實體。
     */
    private View.OnClickListener onEditClick = (v) -> {
        ViewHolder viewHolder = (ViewHolder) v.getTag();
        presenter.onTitleModifyClick(viewHolder.entity);
    };

    /**
     * 刪除資料
     * 呼叫presenter.OnLinkDelete並傳入要刪除的實體。
     */
    private View.OnClickListener onDeleteClick = (v) -> {

        LinkEntity entity = ((ViewHolder) v.getTag()).entity;
        presenter.onLinkDelete(entity);
    };

    class ViewHolder extends RecyclerView.ViewHolder {

        View parent;

        TextView title;
        TextView link;

        ImageView D;
        ImageView thumbnail;
        ImageView edit;
        ImageView delete;

        LinkEntity entity;

        ViewHolder(View v) {
            super(v);
            parent = v;
            title = v.findViewById(R.id.content_txt_title);
            link = v.findViewById(R.id.content_txt_link);
            D = v.findViewById(R.id.content_img_dejavu);
            thumbnail = v.findViewById(R.id.content_img_thumbnail);
            edit = v.findViewById(R.id.content_img_edit);
            delete = v.findViewById(R.id.content_img_delete);

            //將viewholder作為tag放入元件中。
            parent.setTag(this);
            D.setTag(this);
            edit.setTag(this);
            delete.setTag(this);
        }
    }

}
