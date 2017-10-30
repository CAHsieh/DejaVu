package ca.pet.dejavu.View;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ca.pet.dejavu.Model.LinkEntity;
import ca.pet.dejavu.R;

/**
 * Created by CAHSIEH on 2017/10/29.
 * Adapter of RecycleView
 */

public class myRecyclerViewAdapter extends RecyclerView.Adapter<myRecyclerViewAdapter.ViewHolder> {

    private List<LinkEntity> linkEntityList;

    myRecyclerViewAdapter(List<LinkEntity> linkEntityList) {
        this.linkEntityList = linkEntityList;
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
        holder.parent_view.setOnClickListener(mClickAction);
    }

    @Override
    public int getItemCount() {
        return linkEntityList.size();
    }

    private View.OnClickListener mClickAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {

        View parent_view;
        TextView title;
        TextView link;

        ViewHolder(View v) {
            super(v);
            parent_view = v;
            title = (TextView) v.findViewById(R.id.content_txt_title);
            link = (TextView) v.findViewById(R.id.content_txt_link);
        }
    }

}
