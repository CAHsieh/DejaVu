package ca.pet.dejavu.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.MessageDialog;

import java.util.List;

import ca.pet.dejavu.Model.DBService;
import ca.pet.dejavu.Model.LinkEntity;
import ca.pet.dejavu.Model.LinkEntityDao;
import ca.pet.dejavu.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String dejavu_url = "https://youtu.be/dv13gl0a-FA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.fab_d).setOnClickListener(this);

        DBService service = DBService.getInstance();
        service.init(getApplicationContext());
        LinkEntityDao linkEntityDao = service.getLinkEntityDao();
        List<LinkEntity> currentList = linkEntityDao.loadAll();


        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                String title = intent.getStringExtra(Intent.EXTRA_TITLE);

                LinkEntity newData = new LinkEntity();
                newData.setLink(text);
                newData.setTitle(title);
                linkEntityDao.insert(newData);
                currentList.add(newData);

            }
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_content);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new myRecyclerViewAdapter(currentList));
    }

    @Override
    public void onClick(View v) {
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(dejavu_url))
                .build();
        MessageDialog.show(this, content);
    }
}
