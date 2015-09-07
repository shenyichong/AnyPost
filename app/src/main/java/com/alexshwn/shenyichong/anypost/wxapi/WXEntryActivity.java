package com.alexshwn.shenyichong.anypost.wxapi;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.alexshwn.shenyichong.anypost.Constants;
import com.alexshwn.shenyichong.anypost.R;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by shenyichong on 2015/7/5.
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler{

    private IWXAPI api;
    private static int MID_PRE = 10;
    private static int MID_POST = 11;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this,
                Constants.APP_ID, false);
        api.handleIntent(getIntent(), this);
    }

    // 信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                //goToGetMsg();
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                //goToShowMsg((ShowMessageFromWX.Req) req);
                break;
            default:
                break;
        }
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        int result = 0;

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
            {
                result = R.string.errcode_success;
                //在通知栏显示AnyPost微信朋友圈发布成功
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.publish_status_wechat_done))
                        .setContentText(getString(R.string.publish_status_wechat_done));;
                mBuilder.setTicker(getString(R.string.publish_status_wechat_done));
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(MID_PRE);
                mNotificationManager.notify(null, MID_POST, mBuilder.build());
                //通知栏显示1s后自动关闭
                try {
                    Thread.currentThread().sleep(1000);//延时的时间，毫秒
                } catch (InterruptedException ee) {
                    ee.printStackTrace();
                }
                mNotificationManager.cancel(MID_POST);
                break;
            }
            case BaseResp.ErrCode.ERR_USER_CANCEL:
            {
                result = R.string.errcode_cancel;
                //在通知栏显示AnyPost微信朋友圈发布失败
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.publish_status_wechat_cancel))
                        .setContentText(getString(R.string.publish_status_wechat_cancel));;
                mBuilder.setTicker(getString(R.string.publish_status_wechat_cancel));
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(MID_PRE);
                mNotificationManager.notify(null, MID_POST, mBuilder.build());
                //通知栏显示1s后自动关闭
                try {
                    Thread.currentThread().sleep(1000);//延时的时间，毫秒
                } catch (InterruptedException ee) {
                    ee.printStackTrace();
                }
                mNotificationManager.cancel(MID_POST);
                break;
            }
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
            {
                result = R.string.errcode_deny;
                //在通知栏显示AnyPost微信朋友圈发布失败
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.publish_status_wechat_fail))
                        .setContentText(getString(R.string.publish_status_wechat_fail));;
                mBuilder.setTicker(getString(R.string.publish_status_wechat_fail));
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(MID_PRE);
                mNotificationManager.notify(null, MID_POST, mBuilder.build());
                //通知栏显示1s后自动关闭
                try {
                    Thread.currentThread().sleep(1000);//延时的时间，毫秒
                } catch (InterruptedException ee) {
                    ee.printStackTrace();
                }
                mNotificationManager.cancel(MID_POST);
                break;
            }
            default:
            {
                result = R.string.errcode_unknown;
                //在通知栏显示AnyPost微信朋友圈发布失败
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.publish_status_wechat_fail))
                        .setContentText(getString(R.string.publish_status_wechat_fail));;
                mBuilder.setTicker(getString(R.string.publish_status_wechat_fail));
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(MID_PRE);
                mNotificationManager.notify(null, MID_POST, mBuilder.build());
                //通知栏显示1s后自动关闭
                try {
                    Thread.currentThread().sleep(1000);//延时的时间，毫秒
                } catch (InterruptedException ee) {
                    ee.printStackTrace();
                }
                mNotificationManager.cancel(MID_POST);
                break;
            }
        }

        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        finish();
    }

}
