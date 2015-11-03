package com.alexshwn.shenyichong.anypost;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
        final TextView download_addr = (TextView)findViewById(R.id.download_address);
        TextView version_name = (TextView)findViewById(R.id.version_name);
        try {
            version_name.setText("version "+getVersionName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("shenyichong2011@gmail.com") +
                        "?subject=" + Uri.encode("[AnyPost bug 反馈]") +
                        "&body=" + Uri.encode("反馈信息：");
                Uri uri = Uri.parse(uriText);
                intent.setData(uri);
                startActivity(Intent.createChooser(intent, "发送反馈邮件"));
            }
        });

        github_addr.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(github_addr.getText().toString());
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                //startActivity(intent);
                intent.setData(uri);
                startActivity(Intent.createChooser(intent, "访问项目github地址"));
            }
        });

        download_addr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(download_addr.getText().toString());
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                intent.setData(uri);
                startActivity(Intent.createChooser(intent, "访问AnyPost最新下载地址"));
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

    private String getVersionName() throws Exception
    {
        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);
        String version = packInfo.versionName;
        return version;
    }
}
