package com.df.dfcarchecker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.df.service.SoapService;
import com.df.service.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
    /**
     * 后台任务，用来进行登录
     */
    private UserLoginTask mAuthTask = null;

    // 储存用户名、密码
    private String mUserName;
    private String mPassword;

    // 组件们
    private EditText mUserNameView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    // 用户信息：id、key
    public static UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserNameView = (EditText) findViewById(R.id.userName);
        //mUserNameView.setText(mUserName);

        mPasswordView = (EditText) findViewById(R.id.password);

        // 当在密码填写时点击了回车，也视为进行登录操作
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    /**
     * 尝试进行登陆
     * 如果有错误（用户名、密码未填写）则不进行登陆
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mUserNameView.setError(null);
        mPasswordView.setError(null);

        mUserName = mUserNameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 检查用户名
        if (TextUtils.isEmpty(mUserName)) {
            mUserNameView.setError(getString(R.string.error_username_required));
            focusView = mUserNameView;
            cancel = true;
        }

        // 检查密码
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_password_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // 有错误，让有错误的组件获取焦点
            focusView.requestFocus();
        } else {
            // 显示一个进度画面，并启动后台任务进行登录
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mAuthTask = new UserLoginTask(this);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * 显示进度动画，隐藏登录框
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * 一个异步的登录任务
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        SoapService soapService;

        private UserLoginTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;

            try {
                // 登录
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("UserName", mUserName);
                jsonObject.put("Password", mPassword);

                soapService = new SoapService();

                // 设置soap的配置
                soapService.setUtils("http://192.168.100.6:50/userManageService.svc",
                        "http://cheyiju/IUserManageService/UserLogin",
                        "UserLogin");

                success = soapService.login(context, jsonObject.toString());

                // 登录失败，获取错误信息并显示
                if(!success) {
                    Log.d("DFCarChecker", "Login error:" + soapService.getErrorMessage());
                } else {
                    userInfo = new UserInfo();

                    try {
                        JSONObject userJsonObject = new JSONObject(soapService.getResultMessage());

                        // 保存用户的UserId和此次登陆的Key
                        userInfo.setId(userJsonObject.getString("UserId"));
                        userInfo.setKey(userJsonObject.getString("Key"));
                    } catch (Exception e) {
                        Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                        return false;
                    }
                }
            } catch (JSONException e) {
                Log.d("DFCarChecker", "Json解析错误: " + e.getMessage());
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                mPasswordView.setError(soapService.getErrorMessage());
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
