package com.example.shenyichong.anypost;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.utils.LogUtil;
import com.sina.weibo.sdk.utils.UIUtils;

import java.text.SimpleDateFormat;

/**
 * ������Ҫ��ʾ���ʹ�� Code �ֶ�������Ȩ��
 *
 * <b>��ע�⣺</b>�������¼���ԭ�������ܲ���ʹ��ǰ���������Ȩ��ʽ��
 * <li>���ڰ�ȫ�ԵĿ��ǣ���������Ŀǰʹ��Ӧ�õİ�����ǩ������ȡ Token �ķ�ʽ</li>
 * <li>�����밲װ΢���ͻ���</li></br>
 * ����������ͨ�� Code ����ȡ Token��ͨ�����ַ�ʽ��Ҫʹ�õ� APP_SECRET����������Ʊ��ܺ�
 * �Լ��� APP_SECRET��<b>й¶�з���</b>��
 * ����ϸ����鿴��</b><a href="http://t.cn/zldm9tt">��֤��Ȩ����</a>
 *
 * @author SINA
 * @since 2013-10-18
 */
@SuppressWarnings("unused")
public class WBAuthCodeActivity extends Activity {

    private static final String TAG = "WBAuthCodeActivity";

    /**
     * WeiboSDKDemo ����� APP_SECRET��
     * ��ע�⣺��������Ʊ��ܺ��Լ��� APP_SECRET����Ҫֱ�ӱ�¶�ڳ����У��˴�����Ϊһ��DEMO����ʾ��
     */
    private static final String WEIBO_DEMO_APP_SECRET = "4e47e691a516afad0fc490e05ff70ee5";

    /** ͨ�� code ��ȡ Token �� URL */
    private static final String OAUTH2_ACCESS_TOKEN_URL = "https://open.weibo.cn/oauth2/access_token";

    /** UI Ԫ���б� */
    private TextView mNote;
    private TextView mCodeText;
    private TextView mTokenText;
    private Button mCodeButton;
    private Button mAuthCodeButton;

    /** ΢�� Web ��Ȩ�ӿ��࣬�ṩ��½�ȹ���  */
    private AuthInfo mAuthInfo;
    /** ��ȡ���� Code */
    private String mCode;
    /** ��ȡ���� Token */
    private Oauth2AccessToken mAccessToken;

    /**
     * @see {@link Activity#onCreate}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_code);

        // ��ʼ���ؼ�
        mNote = (TextView) findViewById(R.id.note);
        mNote.setMovementMethod(LinkMovementMethod.getInstance());
        mCodeText = (TextView) findViewById(R.id.code_text);
        mTokenText = (TextView) findViewById(R.id.token_text);
        mCodeButton = (Button) findViewById(R.id.code);
        mAuthCodeButton = (Button) findViewById(R.id.auth_code);
        mAuthCodeButton.setEnabled(false);

        // ��ʼ��΢������
        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);

        // ��һ������ȡ Code
        mCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mWeiboAuth.authorize(new AuthListener(), WeiboAuth.OBTAIN_AUTH_CODE);
            }
        });

        // �ڶ�����ͨ�� Code ��ȡ Token
        mAuthCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchTokenAsync(mCode, WEIBO_DEMO_APP_SECRET);
            }
        });
    }

    /**
     * ΢����֤��Ȩ�ص��ࡣ
     */
    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            if (null == values) {
                Toast.makeText(WBAuthCodeActivity.this,
                        R.string.weibosdk_demo_toast_obtain_code_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            String code = values.getString("code");
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(WBAuthCodeActivity.this,
                        R.string.weibosdk_demo_toast_obtain_code_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            mCode = code;
            mCodeText.setText(code);
            mAuthCodeButton.setEnabled(true);
            mTokenText.setText("");
            Toast.makeText(WBAuthCodeActivity.this,
                    R.string.weibosdk_demo_toast_obtain_code_success, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(WBAuthCodeActivity.this,
                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            UIUtils.showToast(WBAuthCodeActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    /**
     * �첽��ȡ Token��
     *
     * @param authCode  ��Ȩ Code���� Code ��һ���Եģ�ֻ�ܱ���ȡһ�� Token
     * @param appSecret Ӧ�ó���� APP_SECRET����������Ʊ��ܺ��Լ��� APP_SECRET��
     *                  ��Ҫֱ�ӱ�¶�ڳ����У��˴�����Ϊһ��DEMO����ʾ��
     */
    public void fetchTokenAsync(String authCode, String appSecret) {

        WeiboParameters requestParams = new WeiboParameters(Constants.APP_KEY);
        requestParams.put(WBConstants.AUTH_PARAMS_CLIENT_ID,     Constants.APP_KEY);
        requestParams.put(WBConstants.AUTH_PARAMS_CLIENT_SECRET, appSecret);
        requestParams.put(WBConstants.AUTH_PARAMS_GRANT_TYPE,    "authorization_code");
        requestParams.put(WBConstants.AUTH_PARAMS_CODE,          authCode);
        requestParams.put(WBConstants.AUTH_PARAMS_REDIRECT_URL,  Constants.REDIRECT_URL);

        // �첽���󣬻�ȡ Token
        new AsyncWeiboRunner(getApplicationContext()).requestAsync(OAUTH2_ACCESS_TOKEN_URL, requestParams, "POST", new RequestListener() {
            @Override
            public void onComplete(String response) {
                LogUtil.d(TAG, "Response: " + response);

                // ��ȡ Token �ɹ�
                Oauth2AccessToken token = Oauth2AccessToken.parseAccessToken(response);
                if (token != null && token.isSessionValid()) {
                    LogUtil.d(TAG, "Success! " + token.toString());

                    mAccessToken = token;
                    String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                            new java.util.Date(mAccessToken.getExpiresTime()));
                    String format = getString(R.string.weibosdk_demo_token_to_string_format_1);
                    mTokenText.setText(String.format(format, mAccessToken.getToken(), date));
                    mAuthCodeButton.setEnabled(false);

                    Toast.makeText(WBAuthCodeActivity.this,
                            R.string.weibosdk_demo_toast_obtain_token_success, Toast.LENGTH_SHORT).show();
                } else {
                    LogUtil.d(TAG, "Failed to receive access token");
                }
            }

            @Override
            public void onWeiboException(WeiboException e) {
                LogUtil.e(TAG, "onWeiboException�� " + e.getMessage());
                Toast.makeText(WBAuthCodeActivity.this,
                        R.string.weibosdk_demo_toast_obtain_token_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}