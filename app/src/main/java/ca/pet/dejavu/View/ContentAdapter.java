package ca.pet.dejavu.View;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import ca.pet.dejavu.Presenter.IMainPresenter;
import ca.pet.dejavu.R;

/**
 * Created by CAHSIEH on 2017/10/29.
 * Adapter of RecycleView
 * 用於顯示網頁資料
 */

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

    private IMainPresenter presenter = null;

    private int lastSelectedPosition;

    ContentAdapter(IMainPresenter presenter) {
        this.presenter = presenter;
        lastSelectedPosition = -1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item_view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_content, parent, false);
        return new ViewHolder(item_view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //re-initial status
        holder.thumbnail.setVisibility(View.VISIBLE);
        holder.D.setSelected(false);
        holder.title.setText(presenter.getEntity(position).getTitle());//設置標題
        holder.link.setText(presenter.getEntity(position).getUri());//設置網址

        holder.D.setOnClickListener(mClickAction);
        holder.parent.setOnClickListener(mClickAction);
        holder.edit.setOnClickListener(onEditClick);
        holder.delete.setOnClickListener(onDeleteClick);


        //若有縮圖內容則使用Glide library將其顯示
        if (presenter.getEntity(position).getThumbnailUrl() != null && !presenter.getEntity(position).getThumbnailUrl().equals("")) {
            Log.i("Glide", "Id: " + presenter.getEntity(position).getId() + " url: " + presenter.getEntity(position).getThumbnailUrl());
            Glide.with(holder.thumbnail.getContext())
                    .load(presenter.getEntity(position).getThumbnailUrl())
                    .error(R.drawable.d)//load失敗的Drawable
                    .fitCenter()//中心fit, 以原本圖片的長寬為主
                    .into(holder.thumbnail);
        } else {
            Log.e("Glide", "not show. Id: " + presenter.getEntity(position).getId());
            holder.thumbnail.setVisibility(View.GONE);
        }

        //滑動回到有選取的項目
        if (position == lastSelectedPosition) {
            holder.D.setSelected(true);
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

        ViewHolder viewHolder = (ViewHolder) v.getTag();

        if (viewHolder.getAdapterPosition() == lastSelectedPosition) {
            lastSelectedPosition = -1;
            viewHolder.D.setSelected(false);
        } else {
            lastSelectedPosition = viewHolder.getAdapterPosition();
        }

        presenter.onLinkSelected(viewHolder.getAdapterPosition());
    };

    /**
     * 編輯標題
     * 呼叫presenter.OnTitleModifyClick並傳入要修改標題的實體。
     */
    private View.OnClickListener onEditClick = (v) -> {
        ViewHolder viewHolder = (ViewHolder) v.getTag();
        presenter.onTitleModifyClick(viewHolder.getAdapterPosition());
    };

    /**
     * 刪除資料
     * 呼叫presenter.OnLinkDelete並傳入要刪除的實體。
     */
    private View.OnClickListener onDeleteClick = (v) -> {

        ViewHolder viewHolder = (ViewHolder) v.getTag();
        presenter.onLinkDelete(viewHolder.getAdapterPosition());
    };

    class ViewHolder extends RecyclerView.ViewHolder {

        View parent;

        TextView title;
        TextView link;

        ImageView D;
        ImageView thumbnail;
        ImageView edit;
        ImageView delete;

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
