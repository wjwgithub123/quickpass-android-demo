package nis.netease.com.quickpassdemo;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmic.sso.sdk.AuthRegisterViewConfig;
import com.cmic.sso.sdk.utils.rglistener.CustomInterface;
import com.netease.nis.quicklogin.QuickLogin;
import com.netease.nis.quicklogin.helper.CMLoginUiConfig;
import com.netease.nis.quicklogin.helper.CULoginUiConfig;
import com.netease.nis.quicklogin.listener.QuickLoginPreMobileListener;
import com.netease.nis.quicklogin.listener.QuickLoginTokenListener;
import com.sdk.base.api.OnCustomViewListener;
import com.sdk.mobile.handler.UiHandler;
import com.sdk.mobile.manager.login.cucc.ConstantCucc;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "QuickLogin";
    private static String BUSINESS_ID;
    private static String mSecretKey;
    private static String mSecretId;
    private static String mVerifyUrl, mOnePassUrl;
    private boolean isTest = false;

    private String mMobileNumber;
    private TextView tvMobileNumber;
    private EditText etMobileNumber;
    private Button btnVerify, btnPrefetchNumber, btnOnePass;
    private QuickLogin login;
    private boolean isUsedCustomCUUi = true;//是否使用自定义的联通登录界面
    private boolean isUsedCustomCMUi = true;//是否使用自定义的移动登录界面
    private boolean isHadPrefetchNumber = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initArgs();
        initViews();
        initData();

    }

    private void initArgs() {
        if (isTest) {
            //BUSINESS_ID = "35d60d532b4f4c4c84f3e243c1989a27"; // 本机校验
            BUSINESS_ID = "3cc9408f47414f03a75947c108e60034"; // 一键登录
            mSecretKey = "abf908daf58a9737a9205142b81e1606";
            mSecretId = "e535e1074974d472a12f5d6e55e521bc";
            mVerifyUrl = "http://eredar-server-test.nis.netease.com/v1/check";
            mOnePassUrl = "http://eredar-server-test.nis.netease.com/v1/oneclick/check";
        } else {
            // BUSINESS_ID = "1412f24fcadc4f1e9b11590221a3e4eb"; // 本机校验
            BUSINESS_ID = "b55f3c7d4729455c9c3fb23872065401"; // 一键登录
            mSecretKey = "72b2db9cb89c5c9d9efb1d1d9950a38e";
            mSecretId = "a4c49cbb2b2420492e132b4c2e03634f";
            mVerifyUrl = "http://ye.dun.163yun.com/v1/check";
            mOnePassUrl = "http://ye.dun.163yun.com/v1/oneclick/check";
        }
    }

    private void initViews() {
        tvMobileNumber = findViewById(R.id.tv_mobile_number);
        etMobileNumber = findViewById(R.id.et_mobile_number);
        btnVerify = findViewById(R.id.btn_verify);
        btnPrefetchNumber = findViewById(R.id.btn_prefetch_number);
        btnOnePass = findViewById(R.id.btn_one_pass);
    }

    private void initData() {
        initOnePass();
        // 本机校验
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMobileNumber = etMobileNumber.getText().toString();
                mobileNumberVerify(mMobileNumber);
            }
        });
        // 一键登登录
        btnPrefetchNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefetchNumber();
                isHadPrefetchNumber = true;
            }
        });
        btnOnePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isHadPrefetchNumber) {
                    Utils.showToast(LoginActivity.this, "一键登录前请先调用预取号接口");
                } else {
                    doOnePass();
                    isHadPrefetchNumber = false;
                }
            }
        });
    }

    private void initOnePass() {
        login = QuickLogin.getInstance(getApplicationContext(), BUSINESS_ID);
        JSONObject extData = new JSONObject();
        try {
            extData.put("parameter1", "param1");
            extData.put("parameter2", "param2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isUsedCustomCUUi) {
            setCULoginUi();
        }
        if (isUsedCustomCMUi) {
            setCMLoginUi();
        }
        login.setDebugMode(true);
        login.setExtendData(extData);
    }

    private void mobileNumberVerify(String mobileNumber) {
        // 本机校验获取token
        login.getToken(mobileNumber, new QuickLoginTokenListener() {
            @Override
            public boolean onExtendMsg(JSONObject extendMsg) {
                Log.d(TAG, "获取的扩展字段内容为:" + extendMsg.toString());
                return super.onExtendMsg(extendMsg);
            }

            @Override
            public void onGetTokenSuccess(final String YDToken, final String accessCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "获取Token成功,yd toke is:" + YDToken + " 运营商token is:" + accessCode);
                        tokenValidate(YDToken, accessCode, false);
                    }
                });
            }

            @Override
            public void onGetTokenError(final String YDToken, final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "获取Token失败,yd toke is:" + YDToken + " msg is:" + msg);
                        Toast.makeText(getApplicationContext(), "获取Token失败,yd toke is:" + YDToken + " msg is:" + msg, Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
    }

    private void prefetchNumber() {
        // 预取号与一键登录
        login.prefetchMobileNumber(new QuickLoginPreMobileListener() {
            @Override
            public void onGetMobileNumberSuccess(String YDToken, final String mobileNumber) {
                Log.d(TAG, "[onGetMobileNumberSuccess]callback mobileNumber is:" + mobileNumber);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(LoginActivity.this, "预取号成功,可一键登录!");
                        tvMobileNumber.setText(mobileNumber);
                    }
                });
            }

            @Override
            public void onGetMobileNumberError(String YDToken, final String msg) {
                Log.e(TAG, "[onGetMobileNumberError]callback error msg is:" + msg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMobileNumber.setText(msg);
                    }
                });
            }
        });
    }

    private void doOnePass() {
        login.onePass(new QuickLoginTokenListener() {
            @Override
            public void onGetTokenSuccess(final String YDToken, final String accessCode) {
                Log.d(TAG, String.format("yd token is:%s accessCode is:%s", YDToken, accessCode));
                tokenValidate(YDToken, accessCode, true);
            }

            @Override
            public void onGetTokenError(String YDToken, String msg) {
                Utils.showToast(LoginActivity.this, "获取运营商token失败:" + msg);
                Log.d(TAG, "获取运营商token失败:" + msg);
            }

            @Override
            public boolean onExtendMsg(JSONObject extendMsg) {
                return super.onExtendMsg(extendMsg);
            }

            @Override
            public void onCancelGetToken() {
                Log.d(TAG, "用户取消登录");
                Utils.showToast(LoginActivity.this, "用户取消登录");
            }
        });
    }

    private void setCULoginUi() {
        // 以下所有接口中，如果某个参数不打算修改默认值，int类型传0值，String类型传null即可
        CULoginUiConfig cuLoginUiConfig = new CULoginUiConfig()
                // 设置导航栏属性
                .setNavigationBar(Color.WHITE, 0, 20, Color.BLACK, true, true)
                // 设置Logo属性
                .setLogo(R.drawable.ic_launcher_background, 100, 100, true, 40)
                // 设置App名属性
                .setAppName(true, Color.BLACK, 100)
                // 设置登录按钮属性
                .setLoginButton(500, 100, 10, "一键登录/注册")
                // 设置手机掩码属性
                .setMobileMaskNumber(Color.BLACK, 20, 40)
                // 设置品牌商属性
                .setBrand(Color.BLACK, 20, true)
                // 设置其它登录按钮属性
                .setOtherLogin("其它方式登录", Color.RED, true, true, 0)
                .setLoading("正在加载，请稍后...", 100, 200, 15, Color.BLUE, true)
                .setViewsVisibility(new String[]{"btn_right"}, new boolean[]{true})
                .setViewsText(new String[]{ConstantCucc.OAUTH_TITLE}, new String[]{"一键登录/注册"})
                // .setViewsTextColor(new String[]{ConstantCucc.OAUTH_CONTENT}, new int[]{0xAC5FF9})
                .setShowProtocolBox(true)
                // 设置其它登录监听
                .setOtherLoginListener(new OnCustomViewListener() {
                    @Override
                    public void onClick(View view, UiHandler uiHandler) {
                        Toast.makeText(getApplicationContext(), "点击了其他登录按钮", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCustomViewListener("btn_right", new OnCustomViewListener() {
                    @Override
                    public void onClick(View view, UiHandler uiHandler) {
                        Toast.makeText(getApplicationContext(), "点击了右上角跳过按钮", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCustomViewListener("custom_view_id", new OnCustomViewListener() {
                    @Override
                    public void onClick(View view, UiHandler uiHandler) {
                        Toast.makeText(getApplicationContext(), "点击了动态添加的View", Toast.LENGTH_SHORT).show();
                    }
                })
                // 设置隐私协议属性
                .setProtocol(20, Color.BLACK, 12, "custom_protocol_1", "自定义条款协议名称1", "https://www.baidu.com", "custom_protocol_2", "自定义条款协议名称2", "https://www.baidu.com");
        login.setCULoginUiConfig(cuLoginUiConfig);

//        // 更细化的控制,单独设置每一项，注意更细化设置和上面不可同时混用
//        UiConfig uiConfig = new UiConfig();
//        NavigationBar navigationBar = new NavigationBar();
//        navigationBar.setText("登录/注册");
//        navigationBar.setBackgroundColor(Color.RED);
//        uiConfig.setNavigationBar(navigationBar);
//        Logo logo = new Logo();
//        logo.setShow(true);
//        logo.setWidth(100);
//        logo.setHeight(80);
//        uiConfig.setLogo(logo);
//        LoginButton loginButton = new LoginButton();
//        loginButton.setWidth(308);
//        loginButton.setHeight(100);
//        loginButton.setText("本机号码一键绑定");
//        uiConfig.setLoginButton(loginButton);
//        Protocol protocol = new Protocol();
//        protocol.setIsCheck(true);
//        protocol.setCheckBoxStyle(R.drawable.checkbox_false);
//        protocol.setCustomProtocol1_id("custom_protocol_1");
//        protocol.setCustomProtocol1_text("自定义条款协议名称1");
//        protocol.setCustomProtocol1_Link("https://www.baidu.com");
//        protocol.setCustomProtocol2_id("custom_protocol_2");
//        protocol.setCustomProtocol2_text("自定义条款协议名称2");
//        protocol.setCustomProtocol2_Link("https://www.baidu.com");
//        uiConfig.setProtocol(protocol);
//        CULoginUiConfig cuLoginUiConfig2 = new CULoginUiConfig().setUiConfig(uiConfig);
//        cuLoginUiConfig2.setOtherLoginListener(new OnCustomViewListener() {
//            @Override
//            public void onClick(View view, UiHandler uiHandler) {
//                Toast.makeText(getApplicationContext(), "点击了其他登录按钮", Toast.LENGTH_SHORT).show();
//            }
//        });
//        login.setCULoginUiConfig(cuLoginUiConfig2);
    }

    private void setCMLoginUi() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        layoutParams.setMargins(0, Utils.dip2px(this, 450), 0, 0);
        View otherLoginView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_custom_view, null, false);
        otherLoginView.setLayoutParams(layoutParams);
        CMLoginUiConfig cmLoginUiConfig = new CMLoginUiConfig()
                // 设置导航栏属性
                .setNavigationBar(Color.RED, "登录/注册", Color.WHITE, "back", false)
                // 设置Logo属性
                .setLogo("ico_logo", 100, 100, true, 100, 0)
                // 设置预取号掩码属性
                .setMobileMaskNumber(Color.BLACK, 20, 170, 0)
                // 设置Slogan属性
                .setSlogan(Color.BLACK, 200, 0)
                // 设置登录按钮属性
                .setLoginButton(400, 50, "一键登录/注册", Color.WHITE, null, 380, 0)
                // 设置动态添加自定义View属性
                .setCustomView(otherLoginView, "R.layout.layout_custom_view", AuthRegisterViewConfig.RootViewId.ROOT_VIEW_ID_BODY, new CustomInterface() {
                    @Override
                    public void onClick(Context context) {
                        Toast.makeText(context, "点击了动态注册的View", Toast.LENGTH_SHORT).show();
                    }
                })
//                .setCustomView(otherLoginView, "R.layout.layout_custom_view", AuthRegisterViewConfig.RootViewId.ROOT_VIEW_ID_TITLE_BAR, new CustomInterface() {
//                    @Override
//                    public void onClick(Context context) {
//                        Toast.makeText(context, "点击了TitleBar上注册的View", Toast.LENGTH_SHORT).show();
//                    }
//                })
                // 设置隐私条款文案
                .setClauseText("登录即同意", "自定义条款协议名称1", "https://www.baidu.com", "自定义条款协议名称2", "https://www.baidu.com", "授权登录")
                // 设置隐私条款属性
                .setClause(10, Color.BLACK, Color.RED, true, "checkbox_true", "checkbox_false", 15, 15, 0, 10, false);

        login.setCMLoginUiConfig(cmLoginUiConfig);

//        // 更细化的控制,单独设置每一项，注意更细化设置和上面不可同时混用
//        AuthThemeConfig config = new AuthThemeConfig.Builder()
//                .setAuthNavTransparent(false)
//                .setLogoHidden(true)
//                .setSloganOffsetY_B(-100)
//                .setNumFieldOffsetY(50)
//                .setLogBtnOffsetY(105)
//                .setNavColor(Color.BLUE)
//                .setPrivacyState(true) // 协议框默认勾选
//                .build();
//        CMLoginUiConfig cmLoginUiConfig2 = new CMLoginUiConfig().setAuthThemeConfig(config);
//        login.setCMLoginUiConfig(cmLoginUiConfig2);

    }

    // 本机校验与一键登录check校验，接入者应该将该操作放到自己服务端，Demo为了完整流程直接写在客户端
    private void tokenValidate(String token, String accessCode, final boolean isOnePass) {

        String nonce = Utils.getRandomString(32);
        String timestamp = String.valueOf(System.currentTimeMillis());
        //生成签名信息
        final HashMap<String, String> map = new HashMap<>();
        map.put("accessToken", accessCode);
        map.put("businessId", BUSINESS_ID);
        map.put("token", token);
        map.put("nonce", nonce);
        map.put("timestamp", timestamp);
        map.put("version", "v1");
        map.put("secretId", mSecretId);
        if (!isOnePass) {
            map.put("phone", mMobileNumber);
        }
        String sign = Utils.generateSign(mSecretKey, map);

        StringBuffer sburl = new StringBuffer();
        if (isOnePass) {
            sburl.append(mOnePassUrl);
        } else {
            sburl.append(mVerifyUrl);
        }
        sburl.append("?accessToken=" + accessCode);
        sburl.append("&businessId=" + BUSINESS_ID);
        sburl.append("&token=" + token);
        sburl.append("&signature=" + sign);
        sburl.append("&nonce=" + nonce);
        sburl.append("&timestamp=" + timestamp);
        sburl.append("&version=" + "v1");
        sburl.append("&secretId=" + mSecretId);
        sburl.append("&phone=" + mMobileNumber);
        final String reqUrl = sburl.toString();
        HttpUtil.doGetRequest(reqUrl, new HttpUtil.ResponseCallBack() {
            @Override
            public void onSuccess(String result) {
                try {
                    Log.e(QuickLogin.TAG, result);
                    JSONObject j = new JSONObject(result);
                    int retCode = j.getInt("code");
                    if (retCode == 200) {
                        if (isOnePass) {
                            String msg = j.getString("msg");
                            JSONObject data = j.getJSONObject("data");
                            String mobileNumber = data.getString("phone");
                            if (!TextUtils.isEmpty(mobileNumber)) {
                                Utils.showToast(LoginActivity.this, "一键登录通过");
                            } else {
                                Utils.showToast(LoginActivity.this, "一键登录不通过" + msg);
                            }
                        } else {
                            JSONObject data = j.getJSONObject("data");
                            int result2 = data.getInt("result");
                            if (result2 == 1) {
                                Utils.showToast(LoginActivity.this, "本机校验通过");
                            } else if (result2 == 2) {
                                Utils.showToast(LoginActivity.this, "本机校验不通过");
                            } else {
                                Utils.showToast(LoginActivity.this, "无法确认校验是否通过");
                            }
                        }

                    } else {
                        String tip = isOnePass ? "一键登录校验token失败：" : "本机校验token失败：";
                        Utils.showToast(LoginActivity.this, tip + j.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(QuickLogin.TAG, "error:" + e.toString());
                }
            }

            @Override
            public void onError(String errorCode, String msg) {
                Log.e(QuickLogin.TAG, "校验token出现错误" + msg);
            }
        });
    }

}
