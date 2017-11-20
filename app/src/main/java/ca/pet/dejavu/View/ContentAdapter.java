package ca.pet.dejavu.View;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import ca.pet.dejavu.Data.LinkEntity;
import ca.pet.dejavu.Presenter.MainPresenter;
import ca.pet.dejavu.R;

/**
 * Created by CAHSIEH on 2017/10/29.
 * Adapter of RecycleView
 */

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder>{

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
        holder.title.setText(presenter.getEntity(position).getTitle());
        holder.link.setText(presenter.getEntity(position).getLink());
        holder.D.setOnClickListener(mClickAction);
        holder.parent.setOnClickListener(mClickAction);
        holder.edit.setOnClickListener(onEditClick);
        holder.delete.setOnClickListener(onDeleteClick);
        holder.entity = presenter.getEntity(position);

        if (holder.entity.getThumbnailUrl() != null && !holder.entity.getThumbnailUrl().equals("")) {
            Log.i("Glide", "url: " + holder.entity.getThumbnailUrl());
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
        return presenter.getPresentingSize();
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
        presenter.OnLinkSelected(viewHolder.entity);
    };

    private View.OnClickListener onEditClick = (v) -> {
        ViewHolder viewHolder = (ViewHolder) v.getTag();
        presenter.OnTitleModifyClick(viewHolder.entity);
    };

    private View.OnClickListener onDeleteClick = (v) -> {

        LinkEntity entity = ((ViewHolder) v.getTag()).entity;
        presenter.OnLinkDelete(entity);
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

            parent.setTag(this);
            D.setTag(this);
            edit.setTag(this);
            delete.setTag(this);
        }
    }

}
