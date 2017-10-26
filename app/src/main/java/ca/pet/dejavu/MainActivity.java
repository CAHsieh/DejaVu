package ca.pet.dejavu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.messenger.ShareToMessengerParams;
import com.facebook.messenger.ShareToMessengerParamsBuilder;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.MessageDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static String dejavu_url = "https://youtu.be/dv13gl0a-FA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.fab_d).setOnClickListener(this);

        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                String title = intent.getStringExtra(Intent.EXTRA_TITLE);

                TextView textView = new TextView(getApplicationContext());
                textView.setText(text);
                ((LinearLayout) findViewById(R.id.text_content)).addView(textView);
            }
        }
    }

    @Override
    public void onClick(View v) {
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(dejavu_url))
                .build();
        MessageDialog.show(this, content);
    }
}
