package com.alexshwn.shenyichong.anypost;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class infoActivity extends Activity {

    //topbar
    private Topbar  mTopbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        mTopbar=(Topbar)findViewById(R.id.topbar);

        mTopbar.setOnTopbarClickListener(new Topbar.topbarClickListener() {
            @Override
            public void leftClick() {
                Toast.makeText(infoActivity.this, "left button", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void rightClick() {
                finish();
            }
        });
        mTopbar.setLeftButtonVisible(false);
        mTopbar.setRightButtonVisible(true);

        TextView email = (TextView)findViewById(R.id.email_address);
        final TextView github_addr = (TextView)findViewById(R.id.github_address);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent send = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("shenyichong2011@gmail.com") +
                        "?subject=" + Uri.encode("[AnyPost bug 反馈]") +
                        "&body=" + Uri.encode("反馈信息：");
                Uri uri = Uri.parse(uriText);
                send.setData(uri);
                startActivity(Intent.createChooser(send, "发送反馈邮件"));
            }
        });

        github_addr.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(github_addr.getText().toString());
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
