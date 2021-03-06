package com.alexshwn.shenyichong.anypost;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
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
import com.tencent.connect.share.QQShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


//public class MainActivity extends Activity implements View.OnClickListener, IWeiboHandler.Response {
public class MainActivity extends Activity implements
                                        View.OnClickListener{

    private static final String TAG = MainActivity.class.getName();
    public static final String KEY_SHARE_TYPE = "key_share_type";
    public static final int SHARE_CLIENT = 1;
//    public static final int SHARE_ALL_IN_ONE = 2;
    private static int RESULT_LOAD_IMAGE = 3;
    private static final String GLOBAL_SETTINGS = "global_settings";
    private static final String TIME_THREAD = "time_thread";
    private static int MID_PRE = 10;
    private static int MID_POST = 11;
    private static int MEDIA_TYPE_IMAGE = 12;
    private static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 13;

    private AuthInfo mAuthInfo;
    /** 显示认证后的信息，如 AccessToken */
    private SsoHandler mSsoHandler;
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;
    /** 用于分享等操作的API */
    private StatusesAPI      mStatusesAPI;   //open API to update status
    /** 微博注册接口API */
    private IWeiboShareAPI mWeiboShareAPI = null;
    /** IWXAPI 是第三方app与微信通信的openApi接口 */
    private IWXAPI mWeixinAPI ;
    /** 腾讯OpenAPI接口实例 */
    private Tencent mTencent;

    /** 分享图片 */
    private ImageView mImageView;
    private int mSampleSize;
    private int mHeight;
    private int mWidth;

    //flag
    private boolean text_exit;
    private boolean image_exit;

    //private Uri electedImage_Uri;
    private EditText editText;
    /** 分享按钮 */
    private ImageButton          mSharedBtn;
    /** 图片选择按钮 */
    private ImageButton          mImageSelectBtn;
    //微信选择分享按钮
    private ToggleButton     mWechatBtn;
    //微博选择分享按钮
    private ToggleButton     mWeiboBtn;
    //qqzone选择分享按钮
    private ToggleButton     mQqzoneBtn;

    //topbar
    private Topbar  mTopbar;



    //used to detect Gestures
    private GestureDetectorCompat mDetector;
    //used to evoke keyboard when double taped
    private InputMethodManager imm;
    //Uri to save camera photo
    private Uri  fileUri;
    //Path to show image location
    String picturePath;
    //Time thread
    int time_Thread;
    //container to show thumbnail
    RelativeLayout thumbnail_container;

    ImageLoaderConfiguration mUILconfig;
    ImageLoader imageloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toast.makeText(MainActivity.this, "onCreate", Toast.LENGTH_SHORT).show();
        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mSsoHandler = new SsoHandler(MainActivity.this, mAuthInfo);

        //注册按钮，使得按钮被点击时，调用onClick函数
        mSharedBtn = (ImageButton) findViewById(R.id.share_button);
        editText = (EditText) findViewById(R.id.editText);
        mImageSelectBtn = (ImageButton)findViewById(R.id.image_select_button);
        mWechatBtn=(ToggleButton)findViewById(R.id.toggleButton_wechat);
        mWeiboBtn=(ToggleButton)findViewById(R.id.toggleButton_weibo);
        mQqzoneBtn=(ToggleButton)findViewById(R.id.toggleButton_tencent);
        mTopbar=(Topbar)findViewById(R.id.topbar);
        mImageView=null;
        picturePath = null;

        mSharedBtn.setOnClickListener(this);
        mSharedBtn.setEnabled(false);
        mSharedBtn.setBackgroundResource(R.drawable.ic_send_grey_24);
        mImageSelectBtn.setOnClickListener(this);
        //实现长按图片选择按钮，弹出照相界面功能
        mImageSelectBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // create Intent to take a picture and return control to the calling application
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = Uri.fromFile(getOutputMediaFile(MEDIA_TYPE_IMAGE)); // create a file to save the image
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                // start the image capture Intent
                startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                return true;
            }
        });
        //设置微博分享选择按钮背景
        mWeiboBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mWeiboBtn.setBackgroundResource(isChecked ? R.drawable.btn_share_weibo : R.drawable.btn_share_weibo_unselected);
            }
        });
        //设置微信分享选择按钮背景
        mWechatBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mWechatBtn.setBackgroundResource(isChecked ? R.drawable.btn_share_weixin_quan : R.drawable.btn_share_weixin_quan_unselected);
            }
        });
        //设置qqzone分享选择按钮背景
        mQqzoneBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mQqzoneBtn.setBackgroundResource(isChecked ? R.drawable.btn_share_qzone : R.drawable.btn_share_qzone_unselected);
            }
        });

        mTopbar.setOnTopbarClickListener(new Topbar.topbarClickListener() {
            @Override
            public void leftClick() {
                Toast.makeText(MainActivity.this, "left button", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void rightClick() {
                Intent intent = new Intent(MainActivity.this,infoActivity.class);
                startActivity(intent);
            }
        });
        mTopbar.setLeftButtonVisible(false);
        mTopbar.setRightButtonVisible(true);


        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);

        // 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
        // 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
        // NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
        mWeiboShareAPI.registerApp();

        //通过WXAPIFactory工厂，获取IWXAPI实例
        mWeixinAPI = WXAPIFactory.createWXAPI(this, Constants.APP_ID, true);
        //将应用注册到微信
        mWeixinAPI.registerApp(Constants.APP_ID);

        //获取tencent OpenAPI实例
        mTencent = Tencent.createInstance(Constants.APP_ID_TENCENT, this.getApplicationContext());

        // SSO 授权, 仅客户端
        mWeiboBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWeiboBtn.isChecked() == true && (mAccessToken == null || mAccessToken.isSessionValid() == false))
                    mSsoHandler.authorize(new AuthListener());
            }
        });
        // 获取当前已保存过的微博 Token
        mAccessToken = AccessTokenKeeper.readAccessToken(this);

        //设置社交网络选择按钮
        //微博
        if(mAccessToken == null || mAccessToken.isSessionValid() == false ){
            mWeiboBtn.setChecked(false);
            mWeiboBtn.setBackgroundResource(R.drawable.btn_share_weibo_unselected);
        }
        else{
            //refresh token operation
            //to be defined
            //mAccessToken.setToken(mAccessToken.getRefreshToken());

            mWeiboBtn.setChecked(true);
            mWeiboBtn.setBackgroundResource(R.drawable.btn_share_weibo);
        }

        mWechatBtn.setChecked(true);
        mQqzoneBtn.setChecked(true);

        //flag
        text_exit = false;
        image_exit = false;

        // Watch EditText
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if("".equals(editText.getText().toString().trim()))  //if(editText.getText().toString().trim().isEmpty())
                    text_exit = false;
                else
                    text_exit = true;
                mSharedBtn.setEnabled((image_exit||text_exit)?true:false);
                mSharedBtn.setBackgroundResource((image_exit || text_exit)? R.drawable.ic_send_green_24:R.drawable.ic_send_grey_24);
            }
        });

        //support double tap to evoke keyboard.
        mDetector = new GestureDetectorCompat(this,new MyGestureListener());
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //code from Open Scource Project Android_Universal_Image_Loader
        mUILconfig = new ImageLoaderConfiguration.Builder(this).build();
        imageloader = ImageLoader.getInstance();
        imageloader.init(mUILconfig);

//functionality to show thumbnail when there is picture captured within one hour using content provider
        String[] filePathColumn = {MediaStore.Images.Media.DATA,MediaStore.MediaColumns.DATE_ADDED};
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, filePathColumn, null, null, null);
        cursor.moveToLast();//move to the latest Image added
        int columnIndex0 = cursor.getColumnIndex(filePathColumn[0]);
        int columnIndex1 = cursor.getColumnIndex(filePathColumn[1]);
        final String latest_pic_path = cursor.getString(columnIndex0);
        String str_seconds_before_1970 = cursor.getString(columnIndex1);
        cursor.close();
        final int int_seconds_before_1970 = Integer.parseInt(str_seconds_before_1970);
        long time=System.currentTimeMillis()/1000;
        SharedPreferences globalSettings = getSharedPreferences(GLOBAL_SETTINGS,Context.MODE_PRIVATE);
        time_Thread = globalSettings.getInt(TIME_THREAD,0);
        if((time-int_seconds_before_1970)/3600 < 1 && time-int_seconds_before_1970 > 3 && time_Thread < int_seconds_before_1970 ){
            //time-int_seconds_before_1970 > 5 is used to avoid captured picture inApp shown
            //Toast.makeText(MainActivity.this, "there's image captured within one hour", Toast.LENGTH_SHORT).show();
            Bitmap Image = BitmapFactory.decodeFile(latest_pic_path);
            Bitmap thumb_image = ThumbnailUtils.extractThumbnail(Image, 200, 200);//need to be modify later

            //add UI component to show thumbnails
            //implement thumbnail imageview itself
            ImageView thumbnail_view = new ImageView(this);
            thumbnail_view.setId(R.id.thumbnail_view);
            thumbnail_view.setImageBitmap(thumb_image);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            thumbnail_view.setLayoutParams(lp);

            //add background of thumbnail(including text and background)
            TextView thumbnail_text = new TextView(this);
            thumbnail_text.setId(R.id.thumbnail_text);
            thumbnail_text.setText(R.string.asking_request);
            thumbnail_text.setTextSize(10f);//need to be modify later
            RelativeLayout.LayoutParams lp0 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp0.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            thumbnail_text.setLayoutParams(lp0);

            //implement RelativeLayout of thumbnail container
            thumbnail_container = new RelativeLayout(this);
            RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(200, 290);//need to be modify later
            lp1.addRule(RelativeLayout.ABOVE,R.id.image_select_button);
            lp1.addRule(RelativeLayout.ALIGN_RIGHT,R.id.image_select_button);
            lp1.bottomMargin = 40;//need to be modify later
            thumbnail_container.setBackgroundResource(R.drawable.abc_ab_share_pack_mtrl_alpha);
            //RelativeLayout main_content
            final RelativeLayout main_content = (RelativeLayout) findViewById(R.id.MainContent);
            thumbnail_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    picturePath = latest_pic_path;
                    addImage();
                    thumbnail_container.setBackgroundResource(0);//delete background resource
                    thumbnail_container.removeAllViews();
                    main_content.removeView(thumbnail_container);
                }
            });
            thumbnail_container.addView(thumbnail_view);
            thumbnail_container.addView(thumbnail_text);
            //add thumbnail container into RelativeLayout main_content
            main_content.addView(thumbnail_container, lp1);

            saveTimeThread(int_seconds_before_1970);

            //显示4s后自动关闭
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    //delayed method
                    //安卓中的消息处理方式实现延时
                    thumbnail_container.setBackgroundResource(0);
                    thumbnail_container.removeAllViews();
                    main_content.removeView(thumbnail_container);
                }
            }, 4000);

        }else{
            //Toast.makeText(MainActivity.this, "no images captured within one hour", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStop(){
        super.onStop();
        //Toast.makeText(MainActivity.this, "onStop", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //Toast.makeText(MainActivity.this, "onDestroy", Toast.LENGTH_SHORT).show();
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

        if (requestCode == RESULT_LOAD_IMAGE && null != data) {
            if(resultCode == RESULT_OK ) {
                Uri selectedImage_uri = data.getData();
                picturePath =  getRealPathFromURI(selectedImage_uri);
                addImage();
            }else if(resultCode == RESULT_CANCELED){
                if(mImageView == null){
                    image_exit = false;
                    mSharedBtn.setEnabled((image_exit||text_exit)?true:false);
                    mSharedBtn.setBackgroundResource((image_exit || text_exit)? R.drawable.ic_send_green_24:R.drawable.ic_send_grey_24);
                }
            }
        }else if(requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE){
            if (resultCode == RESULT_OK) {
                picturePath =  getRealPathFromURI(fileUri);
                addImage();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                if(mImageView == null){
                    image_exit = false;
                    mSharedBtn.setEnabled((image_exit||text_exit)?true:false);
                    mSharedBtn.setBackgroundResource((image_exit || text_exit)? R.drawable.ic_send_green_24:R.drawable.ic_send_grey_24);
                }
            } else {
                // Image capture failed, advise user
                if(mImageView == null){
                    image_exit = false;
                    mSharedBtn.setEnabled((image_exit||text_exit)?true:false);
                    mSharedBtn.setBackgroundResource((image_exit || text_exit)? R.drawable.ic_send_green_24:R.drawable.ic_send_grey_24);
                }
            }

        }

        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResult
        else if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }

        //为了让tencent API成功接收到回调，添加如下代码
        else if(mTencent != null)
            mTencent.onActivityResult(requestCode, resultCode, data);
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
                        mWeiboBtn.setChecked(true);
                        mWeiboBtn.setBackgroundResource(R.drawable.btn_share_weibo);
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
                mWeiboBtn.setChecked(false);
                mWeiboBtn.setBackgroundResource(R.drawable.btn_share_weibo_unselected);
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(MainActivity.this,
                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
                    mWeiboBtn.setChecked(false);
                    mWeiboBtn.setBackgroundResource(R.drawable.btn_share_weibo_unselected);
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(MainActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    mWeiboBtn.setChecked(false);
                    mWeiboBtn.setBackgroundResource(R.drawable.btn_share_weibo_unselected);
        }
    }


    /**
     * 用户点击分享按钮，唤起微博客户端进行分享。
     */
    @Override
    public void onClick(View v) {

        if (R.id.share_button == v.getId()) {
            // sendMultiMessage(true, true); //使用微博发博器进行微博发送
            //发送微博功能实现
            if(mWeiboBtn.isChecked() || mWechatBtn.isChecked() || mQqzoneBtn.isChecked()){
                if (mWeiboBtn.isChecked() == true){
                    mStatusesAPI = new StatusesAPI(this, Constants.APP_KEY, mAccessToken);
                    mImageView = (ImageView) findViewById(R.id.image_selected);
                    if(mImageView != null) {
                        //发送图片微博
                        if ("".equals(editText.getText().toString().trim()))//when no text typed
                            mStatusesAPI.upload(getString(R.string.share_picture_without_text), ((BitmapDrawable) mImageView.getDrawable()).getBitmap(), null, null, mListener);
                        else
                            mStatusesAPI.upload(editText.getText().toString(), ((BitmapDrawable) mImageView.getDrawable()).getBitmap(), null, null, mListener);
                        // 如何发送原图到微博？ http://stackoverflow.com/questions/3879992/get-bitmap-from-an-uri-android
                        // mStatusesAPI.upload(editText.getText().toString(), getThumbnail(electedImage_Uri) , null, null, mListener);
                    }
                    else
                        //发送文字微博
                        mStatusesAPI.update(editText.getText().toString(), null, null, mListener);
                }
                if (mWechatBtn.isChecked() == true){
                    //实现微信发送朋友圈功能
                    mImageView = (ImageView) findViewById(R.id.image_selected);
                    if (mImageView != null) {
                        //发送图片朋友圈
                        Bitmap bmp = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
                        WXImageObject imgObj = new WXImageObject(bmp);
                        //issue：使用相机照片发送朋友圈时出现调其分享界面然后崩溃
                        //设置传入图片位图大小，经过多次测试，当WXImageObject.imageData大于240KB时，就无法成功唤起朋友圈分享UI
                        //因此设置当WXImageObject.imageData大于240KB就缩放构造其的bmp，直到imageData小于240KB
                        while(imgObj.imageData.length/1024 > 240){
                            //缩放bmp的代码，新的newbmp宽和高都缩放为原来的0.5倍。
                            int tmp_height = bmp.getHeight();
                            int tmp_width = bmp.getWidth();
                            Matrix matrix = new Matrix();
                            matrix.postScale((float)0.8, (float)0.8);//bmp的缩放比例为0.8，可设置
                            bmp = Bitmap.createBitmap(bmp, 0, 0, tmp_width, tmp_height, matrix, true);
                            imgObj = new WXImageObject(bmp);
                        }

                        if(imgObj.checkArgs() == false) {
                            Toast.makeText(MainActivity.this, "WXImageObject args is wrong!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        WXMediaMessage msg = new WXMediaMessage();
                        msg.mediaObject = imgObj;

                        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
                        //bmp.recycle();
                        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);  // 设置缩略图（大小不能够超过32KB）
                        //msg.title = String.valueOf(editText.getText());
                        //msg.description = String.valueOf(editText.getText());

                        SendMessageToWX.Req req = new SendMessageToWX.Req();
                        req.transaction = "img" + System.currentTimeMillis(); // transaction字段用于唯一标识一个请求
                        req.message = msg;
                        req.scene = SendMessageToWX.Req.WXSceneTimeline;
                        mWeixinAPI.sendReq(req);

                        //copy editText into clipboard
                        if("".equals(editText.getText().toString().trim()) == false){
                            ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData cd = ClipData.newPlainText("text", editText.getText().toString());
                            cmb.setPrimaryClip(cd);
                            Toast.makeText(MainActivity.this, R.string.copy_to_clipboard, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        //发送文字朋友圈
                        // 初始化一个WXTextObject对象
                        WXTextObject textObj = new WXTextObject();
                        textObj.text = String.valueOf(editText.getText());
                        // 用WXTextObject对象初始化一个WXMediaMessage对象
                        WXMediaMessage msg = new WXMediaMessage();
                        msg.mediaObject = textObj;
                        // 发送文本类型的消息时，title字段不起作用
                        // msg.title = "Will be ignored";
                        msg.description = String.valueOf(editText.getText());
                        // 构造一个Req
                        SendMessageToWX.Req req = new SendMessageToWX.Req();
                        req.transaction = "text" + System.currentTimeMillis(); // transaction字段用于唯一标识一个请求
                        req.message = msg;
                        req.scene = SendMessageToWX.Req.WXSceneTimeline;
                        // 调用api接口发送数据到微信
                        mWeixinAPI.sendReq(req);
                    }
                }
                if(mQqzoneBtn.isChecked() == true){

                    final Bundle params = new Bundle();
                    IUiListener iUiListener = new IUiListener() {
                        @Override
                        public void onComplete(Object o) {
                            Toast.makeText(MainActivity.this, "QQ空间发送成功！", Toast.LENGTH_LONG).show();
                            //Toast.makeText(MainActivity.this, o.toString(), Toast.LENGTH_LONG).show();
                            //通知栏显示AnyPost发送成功
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setContentTitle(getString(R.string.publish_status_qqzone_done))
                                    .setContentText(getString(R.string.publish_status_qqzone_done));;
                            mBuilder.setTicker(getString(R.string.publish_status_qqzone_done));
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

                        }

                        @Override
                        public void onError(UiError uiError) {
                            Toast.makeText(MainActivity.this, ("onError:"+ "code:" + uiError.errorCode + ", msg:"
                                    + uiError.errorMessage + ", detail:").toString() + uiError.errorDetail, Toast.LENGTH_LONG).show();
                            // Toast.makeText(MainActivity.this, "QQ空间发送失败！", Toast.LENGTH_LONG).show();
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setContentTitle(getString(R.string.publish_status_qqzone_fail))
                                    .setContentText(getString(R.string.publish_status_qqzone_fail));;
                            mBuilder.setTicker(getString(R.string.publish_status_qqzone_fail));
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
                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(MainActivity.this, "QQ空间发送取消！", Toast.LENGTH_LONG).show();
                            //通知栏显示AnyPost发送取消
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setContentTitle(getString(R.string.publish_status_qqzone_cancel))
                                    .setContentText(getString(R.string.publish_status_qqzone_cancel));;
                            mBuilder.setTicker(getString(R.string.publish_status_qqzone_cancel));
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
                    }
                } ;
                    if(picturePath == null){
                        Toast.makeText(MainActivity.this,R.string.notice_select_picture_qqzone , Toast.LENGTH_LONG).show();
                        return;
                    }
                    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);//!important! select this to avoid provide target URL(QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
                    params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, picturePath);
                    params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  "AnyPost");
                    params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN); //!important! add this to share to qqzone
                    mTencent.shareToQQ(MainActivity.this, params, iUiListener);
                    //copy editText into clipboard
                    if("".equals(editText.getText().toString().trim()) == false) {
                        ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData cd = ClipData.newPlainText("text", editText.getText().toString());
                        cmb.setPrimaryClip(cd);
                        Toast.makeText(MainActivity.this, R.string.copy_to_clipboard, Toast.LENGTH_LONG).show();
                    }
                }
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle(getString(R.string.publish_status))
                                .setContentText(getString(R.string.publish_status));
                //设定通知优先级
                mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
                //设定闪现提示语文字
                mBuilder.setTicker(getString(R.string.publish_status));
                //设定不能够被滑动删除
                mBuilder.setOngoing(true);
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // mId allows you to update the notification later on.
                mNotificationManager.notify(MID_PRE,mBuilder.build());
            }
            else{
                Toast.makeText(MainActivity.this, R.string.please_select_social_network, Toast.LENGTH_LONG).show();
            }
        }
        else if(R.id.image_select_button == v.getId()){
            //Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
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
                    //通知栏显示AnyPost发送成功
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(getString(R.string.publish_status_weibo_done))
                            .setContentText(getString(R.string.publish_status_weibo_done));;
                    mBuilder.setTicker(getString(R.string.publish_status_weibo_done));
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(MID_PRE);
                    mNotificationManager.notify(null, MID_POST, mBuilder.build());

                    // 调用 Status#parse 解析字符串成微博对象
                    Status status = Status.parse(response);
                    Toast.makeText(MainActivity.this,
                            "发送一送微博成功, id = " + status.id,
                            Toast.LENGTH_LONG).show();

                    //通知栏显示1s后自动关闭
                    try {
                        Thread.currentThread().sleep(1000);//延时的时间，毫秒
                    } catch (InterruptedException ee) {
                        ee.printStackTrace();
                    }
                    mNotificationManager.cancel(MID_POST);
                } else {
                    Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }
        }
        @Override
        public void onWeiboException(WeiboException e) {

            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            NotificationCompat.Builder mBuilder;
            //if weibo access token expired, reauthorization needed.
            if(info.error_code.equals("21332".toString()) || info.error_code.equals("21327".toString()) ){
                //在通知栏显示AnyPost发布失败，提示需要进行重新授权并调起授权页面。
                mBuilder = new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.publish_status_weibo_fail_and_reauth))
                        .setContentText(getString(R.string.publish_status_weibo_fail_and_reauth));
                mBuilder.setTicker(getString(R.string.publish_status_weibo_fail_and_reauth));
                mSsoHandler.authorize(new AuthListener());
            }else {
                Toast.makeText(MainActivity.this, info.toString(), Toast.LENGTH_LONG).show();
                //在通知栏显示AnyPost发布失败
                mBuilder = new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.publish_status_weibo_fail))
                        .setContentText(getString(R.string.publish_status_weibo_fail));
                mBuilder.setTicker(getString(R.string.publish_status_weibo_fail));
            }
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

        }
    };

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "AnyPost");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("AnyPost", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }

    //function to get Real Path from URI
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public void saveTimeThread(int time){
        if(time != 0 && (System.currentTimeMillis()/1000-time)/3600 < 1 ){
            SharedPreferences globalSettings = getSharedPreferences(GLOBAL_SETTINGS,Context.MODE_PRIVATE);
            if(time  <= globalSettings.getInt(TIME_THREAD,0))
                return;
            else{
                SharedPreferences.Editor Total_editor = globalSettings.edit();
                Total_editor.putInt(TIME_THREAD, time);
                Total_editor.commit();
            }
        }
    }

    private ImageView addImageView(Bitmap bmp){
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bmp);
        imageView.setId(R.id.image_selected);
        imageView.setAdjustViewBounds(true);//make imageView adjust to fit in container bounds.
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(lp);
        return imageView;
    }

    private ImageButton addDeleteView(ImageView view){
        ImageButton imageBtn = new ImageButton(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(80, 80);
        lp.addRule(RelativeLayout.ALIGN_RIGHT, view.getId());
        lp.addRule(RelativeLayout.ALIGN_TOP, view.getId());
//        lp.leftMargin=100;
//        lp.topMargin=200;
        imageBtn.setLayoutParams(lp);
        imageBtn.setBackgroundResource(R.drawable.btn_delete);
        imageBtn.setId(R.id.btn_delete);
        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout imageShower = (RelativeLayout) findViewById(R.id.image_shower);
                imageShower.removeAllViews();
//                imageShower = null;
                image_exit = false;
                mSharedBtn.setEnabled((image_exit || text_exit) ? true : false);
                mSharedBtn.setBackgroundResource((image_exit || text_exit)? R.drawable.ic_send_green_24:R.drawable.ic_send_grey_24);
                picturePath = null;//reset picturePath
            }
        });
        return imageBtn;
    }

    private void addImage(){
        //设置图片尺寸，防止图片过大无法显示
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap Image = BitmapFactory.decodeFile(picturePath, options);
        mSampleSize = 1;
        mHeight = options.outHeight;
        mWidth = options.outWidth;
        while (mHeight > 4096 || mWidth > 4096) {
            mHeight /= 2;
            mWidth /= 2;
            mSampleSize *= 2;
        }
        options.inSampleSize = mSampleSize;
        //后续可以根据layout设置固定尺寸
        options.inJustDecodeBounds = false;
        Image = BitmapFactory.decodeFile(picturePath, options);
        //设置图片展示layout,need to be modified
        RelativeLayout imageShower = (RelativeLayout) findViewById(R.id.image_shower);
        imageShower.removeAllViews();
        ImageView view = addImageView(Image);
        imageShower.addView(view);
        imageShower.addView(addDeleteView(view));
        // if set height and width here:imageShower.addView(addDeleteView(view),60,60);
        // then in function addDeleteView RelativeLayout.LayoutParams can't be reconfigured,such as relative position

        image_exit = true;
        mSharedBtn.setEnabled((image_exit||text_exit)?true:false);
        mSharedBtn.setBackgroundResource((image_exit || text_exit) ? R.drawable.ic_send_green_24 : R.drawable.ic_send_grey_24);
    }
}