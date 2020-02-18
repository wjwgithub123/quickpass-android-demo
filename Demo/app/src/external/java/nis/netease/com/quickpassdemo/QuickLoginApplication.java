package nis.netease.com.quickpassdemo;

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.netease.nis.quicklogin.QuickLogin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by hzhuqi on 2019/10/24
 */
public class QuickLoginApplication extends Application {
    public static final String TAG = "QuickLoginDemo";
    private boolean isTest = false;
    public QuickLogin quickLogin;
    public QuickLogin trialQuickLogin;
    /**
     * 一键登录业务id
     */
    private static String onePassId;
    /**
     * 本机校验业务id
     */
    public static String mobileVerifyId;
    /**
     * 试用一键登录业务id
     */
    private static String trialOnePassId;
    /**
     * 试用本机校验id
     */
    public static String trialMobileVerifyId;
    private static String secretKey;
    private static String secretId;
    private static String verifyUrl, onePassUrl;

    @Override
    public void onCreate() {
        super.onCreate();
        initArgs();
        initOnePass();
        initTrialQuickLogin();
    }

    private void initArgs() {
        if (isTest) {
            mobileVerifyId = "35d60d532b4f4c4c84f3e243c1989a27";
            onePassId = "3cc9408f47414f03a75947c108e60034";
            secretKey = "abf908daf58a9737a9205142b81e1606";
            secretId = "e535e1074974d472a12f5d6e55e521bc";
            verifyUrl = "http://eredar-server-test.nis.netease.com/v1/check";
            onePassUrl = "http://eredar-server-test.nis.netease.com/v1/oneclick/check";
        } else {
            trialMobileVerifyId = "1cf3bbb5769e4285aba195b091b83cf3";
            trialOnePassId = "218c3e131c61450082d9cbf95124d121";

            mobileVerifyId = "1412f24fcadc4f1e9b11590221a3e4eb";
            onePassId = "b55f3c7d4729455c9c3fb23872065401";
            secretKey = "72b2db9cb89c5c9d9efb1d1d9950a38e";
            secretId = "a4c49cbb2b2420492e132b4c2e03634f";
            verifyUrl = "http://ye.dun.163yun.com/v1/check";
            onePassUrl = "http://ye.dun.163yun.com/v1/oneclick/check";
        }
    }

    private void initOnePass() {
        quickLogin = QuickLogin.getInstance(getApplicationContext(), onePassId);
        JSONObject extData = new JSONObject();
        try {
            extData.put("parameter1", "param1");
            extData.put("parameter2", "param2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        quickLogin.setUnifyUiConfig(QuickLoginUiConfig.getUiConfig(getApplicationContext()));
        quickLogin.setExtendData(extData);
        quickLogin.setDebugMode(true);
    }

    private void initTrialQuickLogin() {
        trialQuickLogin = QuickLogin.getInstance(getApplicationContext(), trialOnePassId);
        trialQuickLogin.setUnifyUiConfig(QuickLoginUiConfig.getUiConfig(getApplicationContext()));
        trialQuickLogin.setDebugMode(true);
    }

    // 本机校验与一键登录check校验，接入者应该将该操作放到自己服务端，Demo为了完整流程直接写在客户端
    public void tokenValidate(String token, String accessCode, final String mobileNumber, final Activity activity, final boolean isTrialId) {
        boolean isOnePass = true;
        if (!TextUtils.isEmpty(mobileNumber)) {
            isOnePass = false; // 本机校验
        }
        String nonce = Utils.getRandomString(32);
        String timestamp = String.valueOf(System.currentTimeMillis());
        //生成签名信息
        final HashMap<String, String> map = new HashMap<>();
        map.put("accessToken", accessCode);
        if (isOnePass) {
            if (isTrialId) {
                map.put("businessId", trialOnePassId);
            } else {
                map.put("businessId", onePassId);
            }
        } else {
            if (isTrialId) {
                map.put("businessId", trialMobileVerifyId);
            } else {
                map.put("businessId", mobileVerifyId);
            }
        }
        map.put("token", token);
        map.put("nonce", nonce);
        map.put("timestamp", timestamp);
        map.put("version", "v1");
        map.put("secretId", secretId);
        if (!isOnePass) {
            map.put("phone", mobileNumber);
        }
        String sign = Utils.generateSign(secretKey, map);

        StringBuffer sburl = new StringBuffer();
        if (isOnePass) {
            sburl.append(onePassUrl);
        } else {
            sburl.append(verifyUrl);
        }
        sburl.append("?accessToken=" + accessCode);
        if (isOnePass) {
            if (isTrialId) {
                sburl.append("&businessId=" + trialOnePassId);
            } else {
                sburl.append("&businessId=" + onePassId);
            }
        } else {
            if (isTrialId) {
                sburl.append("&businessId=" + trialMobileVerifyId);
            } else {
                sburl.append("&businessId=" + mobileVerifyId);
            }

        }
        sburl.append("&token=" + token);
        sburl.append("&signature=" + sign);
        sburl.append("&nonce=" + nonce);
        sburl.append("&timestamp=" + timestamp);
        sburl.append("&version=" + "v1");
        sburl.append("&secretId=" + secretId);
        sburl.append("&phone=" + mobileNumber);
        final String reqUrl = sburl.toString();
        Log.d(QuickLogin.TAG, "request url: " + reqUrl);
        final boolean finalIsOnePass = isOnePass;
        HttpUtil.doGetRequest(reqUrl, new HttpUtil.ResponseCallBack() {
            @Override
            public void onSuccess(String result) {
                try {
                    Log.e(QuickLogin.TAG, result);
                    JSONObject j = new JSONObject(result);
                    int retCode = j.getInt("code");
                    if (retCode == 200) {
                        if (finalIsOnePass) {
                            String msg = j.getString("msg");
                            JSONObject data = j.getJSONObject("data");
                            String mobileNumber = data.getString("phone");
                            if (!TextUtils.isEmpty(mobileNumber)) {
                                Utils.showToast(activity, "一键登录通过");
                            } else {
                                Utils.showToast(activity, "一键登录不通过");
                            }
                        } else {
                            JSONObject data = j.getJSONObject("data");
                            int result2 = data.getInt("result");
                            if (result2 == 1) {
                                Utils.showToast(activity, "本机校验通过");
                            } else if (result2 == 2) {
                                Utils.showToast(activity, "本机校验不通过");
                            } else {
                                Utils.showToast(activity, "无法确认校验是否通过");
                            }
                        }

                    } else {
                        String tip = finalIsOnePass ? "一键登录校验token失败：" : "本机校验token失败：";
                        Utils.showToast(activity, tip + j.toString());
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
