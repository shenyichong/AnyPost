package com.example.shenyichong.anypost;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.utils.LogUtil;

//public class MainActivity extends Activity implements View.OnClickListener, IWeiboHandler.Response {
public class MainActivity extends Activity implements
                                        View.OnClickListener{

    private static final String TAG = MainActivity.class.getName();
    public static final String KEY_SHARE_TYPE = "key_share_type";
    public static final int SHARE_CLIENT = 1;
//    public static final int SHARE_ALL_IN_ONE = 2;
    private static int RESULT_LOAD_IMAGE = 3;

    private AuthInfo mAuthInfo;
    /** 显示认证后的信息，如 AccessToken */
    private SsoHandler mSsoHandler;
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;
    /** 微博微博分享接口实例 */
    private IWeiboShareAPI mWeiboShareAPI = null;

    private int mShareType = SHARE_CLIENT;
    /** 分享图片 */
    private ImageView mImageView;
    private Uri electedImage_Uri;
    private EditText editText;
    /** 分享按钮 */
    private Button          mSharedBtn;
    /** 图片选择按钮 */
    private Button          mImageSelectBtn;
    /** 用于获取微博信息流等操作的API */
    private StatusesAPI mStatusesAPI;   //open API to update status

    //used to detect Gestures
    private GestureDetectorCompat mDetector;
    //used to evoke keyboard when double taped
    private InputMethodManager imm;
    /**
     * 注意：SsoHandler 仅当 SDK 支持 SSO 时有效
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mSsoHandler = new SsoHandler(MainActivity.this, mAuthInfo);

        //注册按钮，使得按钮被点击时，调用onClick函数
        mSharedBtn = (Button) findViewById(R.id.share_button);
        mImageSelectBtn = (Button)findViewById(R.id.image_select_button);
        mSharedBtn.setOnClickListener(this);
        mImageSelectBtn.setOnClickListener(this);
        mSharedBtn.setEnabled(false);

        editText = (EditText) findViewById(R.id.editText);
        mImageView = (ImageView) findViewById(R.id.imageView);

        mShareType = getIntent().getIntExtra(KEY_SHARE_TYPE, SHARE_CLIENT);
        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);

        // 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
        // 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
        // NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
        mWeiboShareAPI.registerApp();

        // SSO 授权, 仅客户端
        findViewById(R.id.weibo_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSsoHandler.authorize(new AuthListener());
            }
        });
        // 获取当前已保存过的 Token
        mAccessToken = AccessTokenKeeper.readAccessToken(this);

        // Watch EditText
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if("".equals(editText.getText().toString().trim())) //if(editText.getText().toString().trim().isEmpty())
                    mSharedBtn.setEnabled(false);
                else
                    mSharedBtn.setEnabled(true);
            }
        });

        //support double tap to evoke keyboard.
        mDetector = new GestureDetectorCompat(this,new MyGestureListener());
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        //code from Open Scource Project Android_Universal_Image_Loader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    //class used to Implement GestureListener
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG,"onDown: " + event.toString());
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
            //evoke keyboard
            imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            return true;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


    /**
     * 当 SSO 授权 Activity 退出时，该函数被调用。
     * 当图片选择 Activity 退出时，该函数被调用。
     * @see {@link Activity#onActivityResult}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            electedImage_Uri = selectedImage;
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            //设置图片尺寸，防止图片过大无法显示
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap Image = BitmapFactory.decodeFile(picturePath,options);
            options.inSampleSize = 1;
            //while(options.outHeight > 4096 || options.outWidth > 4096){   //为什么使用while有时会卡顿，并且导致程序无响应？
            if(options.outHeight > 4096 || options.outWidth > 4096){
                options.inSampleSize *= 2;
            }
            //后续可以根据layout设置固定尺寸
            options.inJustDecodeBounds = false;
            Image = BitmapFactory.decodeFile(picturePath,options);
            mImageView.setImageBitmap(Image);
            return;
        }

        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResult
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     * 该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {

                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(MainActivity.this, mAccessToken);
                Toast.makeText(MainActivity.this,
                        R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();

            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = getString(R.string.weibosdk_demo_toast_auth_failed);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(MainActivity.this,
                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(MainActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    /**
     * 用户点击分享按钮，唤起微博客户端进行分享。
     */
    @Override
    public void onClick(View v) {
        if (R.id.share_button == v.getId()) {
            // sendMultiMessage(true, true); //使用微博发博器进行微博发送

            if(mAccessToken == null || mAccessToken.isSessionValid() == false){
                    Toast.makeText(MainActivity.this, "请先登陆微博", Toast.LENGTH_LONG).show();
                    return;
            }else
                    mStatusesAPI = new StatusesAPI(this, Constants.APP_KEY, mAccessToken);

            if(mImageView.getDrawable() != null)
                //发送图片微博
                mStatusesAPI.upload(editText.getText().toString(), ((BitmapDrawable)mImageView.getDrawable()).getBitmap(), null, null, mListener);
                // 如何发送原图到微博？ http://stackoverflow.com/questions/3879992/get-bitmap-from-an-uri-android
                // mStatusesAPI.upload(editText.getText().toString(), getThumbnail(electedImage_Uri) , null, null, mListener);
            else
                //发送文字微博
                mStatusesAPI.update(editText.getText().toString(), null, null, mListener);
        }
        else if(R.id.image_select_button == v.getId()){
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    }
    /**
     * 微博 OpenAPI 回调接口。
     */
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i(TAG, response);
                if (response.startsWith("{\"statuses\"")) {
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    StatusList statuses = StatusList.parse(response);
                    if (statuses != null && statuses.total_number > 0) {
                        Toast.makeText(MainActivity.this,
                                "获取微博信息流成功, 条数: " + statuses.statusList.size(),
                                Toast.LENGTH_LONG).show();
                    }
                } else if (response.startsWith("{\"created_at\"")) {
                    // 调用 Status#parse 解析字符串成微博对象
                    Status status = Status.parse(response);
                    Toast.makeText(MainActivity.this,
                            "发送一送微博成功, id = " + status.id,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }
        }
        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(MainActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };

    /**
     * 创建文本消息对象。
     *
     * @return 文本消息对象。
     */
    private TextObject getTextObj() {
        TextObject textObject = new TextObject();
        textObject.text = editText.getText().toString();
        return textObject;
    }

//    /**
//     * 创建图片消息对象。
//     *
//     * @return 图片消息对象。
//     */
//    private ImageObject getImageObj() {
//        ImageObject imageObject = new ImageObject();
//        BitmapDrawable bitmapDrawable = (BitmapDrawable) mImageView.getDrawable();
//        imageObject.setImageObject(bitmapDrawable.getBitmap());
//        return imageObject;
//    }


//    private void sendMultiMessage(boolean hasText, boolean hasImage) {
//
//        // 1. 初始化微博的分享消息
//        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
//        if (hasText) {
//            weiboMessage.textObject = getTextObj();
//        }
//
//        if (hasImage) {
//            weiboMessage.imageObject = getImageObj();
//        }
//
//        // 2. 初始化从第三方到微博的消息请求
//        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
//        // 用transaction唯一标识一个请求
//        request.transaction = String.valueOf(System.currentTimeMillis());
//        request.multiMessage = weiboMessage;
//
//        // 3. 发送请求消息到微博，唤起微博分享界面
//        if (mShareType == SHARE_CLIENT) {
//            mWeiboShareAPI.sendRequest(MainActivity.this, request);
//        }
//        else if (mShareType == SHARE_ALL_IN_ONE) {
//            AuthInfo authInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
//            Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(getApplicationContext());
//            String token = "";
//            if (accessToken != null) {
//                token = accessToken.getToken();
//            }
//            mWeiboShareAPI.sendRequest(this, request, authInfo, token, new WeiboAuthListener() {
//
//                @Override
//                public void onWeiboException( WeiboException arg0 ) {
//                }
//
//                @Override
//                public void onComplete( Bundle bundle ) {
//                    // TODO Auto-generated method stub
//                    Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
//                    AccessTokenKeeper.writeAccessToken(getApplicationContext(), newToken);
//                    Toast.makeText(getApplicationContext(), "onAuthorizeComplete token = " + newToken.getToken(), Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void onCancel() {
//                }
//            });
//        }
//    }

//    /**
//     * 接收微客户端博请求的数据。
//     * 当微博客户端唤起当前应用并进行分享时，该方法被调用。
//     *
//     * @param baseRequest 微博请求数据对象
//     * @see {@link IWeiboShareAPI#handleWeiboRequest}
//     */
//    @Override
//    public void onResponse(BaseResponse baseResp) {
//        switch (baseResp.errCode) {
//            case WBConstants.ErrorCode.ERR_OK:
//                Toast.makeText(this, R.string.weibosdk_demo_toast_share_success, Toast.LENGTH_LONG).show();
//                break;
//            case WBConstants.ErrorCode.ERR_CANCEL:
//                Toast.makeText(this, R.string.weibosdk_demo_toast_share_canceled, Toast.LENGTH_LONG).show();
//                break;
//            case WBConstants.ErrorCode.ERR_FAIL:
//                Toast.makeText(this,
//                        getString(R.string.weibosdk_demo_toast_share_failed) + "Error Message: " + baseResp.errMsg,
//                        Toast.LENGTH_LONG).show();
//                break;
//        }
//    }

//    /**
//     * @see {@link Activity#onNewIntent}
//     */
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//
//        // 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
//        // 来接收微博客户端返回的数据；执行成功，返回 true，并调用
//        // {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
//        mWeiboShareAPI.handleWeiboResponse(intent, this);
//    }


}