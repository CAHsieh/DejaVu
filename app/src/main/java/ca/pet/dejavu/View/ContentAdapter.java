package ca.pet.dejavu.View;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import ca.pet.dejavu.Model.LinkEntity;
import ca.pet.dejavu.Presenter.LinkEntityPresenter;
import ca.pet.dejavu.R;

/**
 * Created by CAHSIEH on 2017/10/29.
 * Adapter of RecycleView
 */

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> implements TitleDialog.OnTitleSetCallback {

    private LinkEntityPresenter entityPresenter;

    private OnItemActionListener onItemActionListener;

    private ImageView lastSelectedImageView;

    ContentAdapter() {
        entityPresenter = LinkEntityPresenter.getInstance();
        entityPresenter.queryAll();
    }

    void setOnItemActionListener(OnItemActionListener onItemActionListener) {
        this.onItemActionListener = onItemActionListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item_view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_content, parent, false);
        return new ViewHolder(item_view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(entityPresenter.getEntity(position).getTitle());
        holder.link.setText(entityPresenter.getEntity(position).getLink());
        holder.D.setOnClickListener(mClickAction);
        holder.parent.setOnClickListener(mClickAction);
        holder.edit.setOnClickListener(onEditClick);
        holder.delete.setOnClickListener(onDeleteClick);
        holder.entity = entityPresenter.getEntity(position);

        if (holder.entity.getThumbnailUrl() != null && !holder.entity.getThumbnailUrl().equals("")) {
            Glide.with(holder.thumbnail.getContext())
                    .load(holder.entity.getThumbnailUrl())
                    .error(R.drawable.d)//load失敗的Drawable
                    .fitCenter()//中心fit, 以原本圖片的長寬為主
                    .into(holder.thumbnail);
        } else {
            holder.thumbnail.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return entityPresenter.presenting_size();
    }

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
        if (null != onItemActionListener) {
            onItemActionListener.OnLinkSelected(viewHolder.entity);
        }
    };

    private View.OnClickListener onEditClick = (v) -> {
        ViewHolder viewHolder = (ViewHolder) v.getTag();
        if (onItemActionListener != null)
            onItemActionListener.OnTitleModifyClick(viewHolder.entity);
    };

    @Override
    public void OnTitleSet(LinkEntity entity) {

        entityPresenter.update(entity);
        notifyDataSetChanged();
    }

    private View.OnClickListener onDeleteClick = (v) -> {

        LinkEntity entity = ((ViewHolder) v.getTag()).entity;
        if (onItemActionListener != null)
            onItemActionListener.OnLinkDelete(entity);

        int position = entityPresenter.delete(entity);
        notifyItemRemoved(position);
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
            title = (TextView) v.findViewById(R.id.content_txt_title);
            link = (TextView) v.findViewById(R.id.content_txt_link);
            D = (ImageView) v.findViewById(R.id.content_img_dejavu);
            thumbnail = (ImageView) v.findViewById(R.id.content_img_thumbnail);
            edit = (ImageView) v.findViewById(R.id.content_img_edit);
            delete = (ImageView) v.findViewById(R.id.content_img_delete);

            parent.setTag(this);
            D.setTag(this);
            edit.setTag(this);
            delete.setTag(this);
        }
    }


    public interface OnItemActionListener {
        void OnLinkSelected(LinkEntity entity);

        void OnLinkDelete(LinkEntity entity);

        void OnTitleModifyClick(LinkEntity entity);
    }
}
