package ca.pet.dejavu.View;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ca.pet.dejavu.Model.DBService;
import ca.pet.dejavu.Model.LinkEntity;
import ca.pet.dejavu.Model.LinkEntityDao;
import ca.pet.dejavu.R;

/**
 * Created by CAHSIEH on 2017/10/29.
 * Adapter of RecycleView
 */

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> implements TitleDialog.OnTitleSetCallback {

    private AppCompatActivity appCompatActivity;
    private List<LinkEntity> linkEntityList;

    private OnLinkActionListener onLinkActionListener;

    private ImageView lastSelectedImageView;

    ContentAdapter(AppCompatActivity appCompatActivity, List<LinkEntity> linkEntityList) {
        this.appCompatActivity = appCompatActivity;
        this.linkEntityList = linkEntityList;
    }

    public void setOnLinkActionListener(OnLinkActionListener onLinkActionListener) {
        this.onLinkActionListener = onLinkActionListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item_view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_content, parent, false);
        return new ViewHolder(item_view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(linkEntityList.get(position).getTitle());
        holder.link.setText(linkEntityList.get(position).getLink());
        holder.D.setOnClickListener(mClickAction);
        holder.edit.setOnClickListener(onEditClick);
        holder.delete.setOnClickListener(onDeleteClick);
        holder.entity = linkEntityList.get(position);
    }

    @Override
    public int getItemCount() {
        return linkEntityList.size();
    }

    private View.OnClickListener mClickAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (lastSelectedImageView != null)
                lastSelectedImageView.setSelected(false);

            ViewHolder viewHolder = (ViewHolder) v.getTag();
            lastSelectedImageView = viewHolder.D;
            lastSelectedImageView.setSelected(true);
            if (null != onLinkActionListener) {
                onLinkActionListener.OnLinkSelected(viewHolder.entity);
            }
        }
    };

    private View.OnClickListener onEditClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHolder viewHolder = (ViewHolder) v.getTag();
            TitleDialog titleDialog = new TitleDialog(appCompatActivity, viewHolder.entity);
            titleDialog.setOnTitleActionCallback(ContentAdapter.this);
            titleDialog.show();
        }
    };

    @Override
    public void OnTitleSet(LinkEntity entity) {

        LinkEntityDao entityDao = DBService.getInstance().getLinkEntityDao();
        entityDao.update(entity);

        notifyDataSetChanged();
    }

    private View.OnClickListener onDeleteClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            LinkEntity entity = ((ViewHolder) v.getTag()).entity;
            if (onLinkActionListener != null)
                onLinkActionListener.OnLinkDelete(entity);

            LinkEntityDao entityDao = DBService.getInstance().getLinkEntityDao();
            entityDao.delete(entity);
            linkEntityList.remove(entity);

            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView link;

        ImageView D;
        ImageButton edit;
        ImageButton delete;

        LinkEntity entity;

        ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.content_txt_title);
            link = (TextView) v.findViewById(R.id.content_txt_link);
            D = (ImageView) v.findViewById(R.id.content_img_dejavu);
            edit = (ImageButton) v.findViewById(R.id.content_img_edit);
            delete = (ImageButton) v.findViewById(R.id.content_img_delete);

            D.setTag(this);
            edit.setTag(this);
            delete.setTag(this);
        }
    }


    public interface OnLinkActionListener {
        void OnLinkSelected(LinkEntity entity);

        void OnLinkDelete(LinkEntity entity);
    }
}
