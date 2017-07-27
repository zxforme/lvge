package lvge.com.myapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;

import com.zhy.http.okhttp.callback.Callback;

import lvge.com.myapp.model.LoginResultModel;
import okhttp3.CookieJar;
import okhttp3.Response;

public class MainActivity extends Activity{

    private SharedPreferences preferences;
    private String action;
    private final static String FILE_NAME = "login_file";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button login_submit = (Button) findViewById(R.id.login_submit);

        action = "auto_submit";
        Login(action);
        login_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action = "submit";
                Login(action);
            }
        });


    }

    void Login(String action) {

        final String submit_action = action;
        try {
            preferences = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

            final EditText et_username = (EditText) findViewById(R.id.username);
            final EditText et_password = (EditText) findViewById(R.id.password);
            if (action.equals("auto_submit")) {
                String username = preferences.getString("username", "");
                String password = preferences.getString("password", "");

                if (username.equals("")&&password.equals(""))
                {
                    return ;
                }
                et_username.setText(username);
                et_password.setText(password);
            }

            OkHttpUtils.get()//get 方法
                    .url("http://www.lvgew.com/obdcarmarket/sellerapp/login.do") //地址
                    .addParams("userName", et_username.getText().toString()) //需要传递的参数
                    .addParams("pwd", et_password.getText().toString())
                    .build()
                    .execute(new Callback() {//通用的callBack

                        //从后台获取成功后，对相应进行类型转化
                        @Override
                        public Object parseNetworkResponse(Response response, int i) throws Exception {

                            String string = response.body().string();//获取相应中的内容Json格式
                            //把json转化成对应对象
                            //LoginResultModel是和后台返回值类型结构一样的对象
                            LoginResultModel result = new Gson().fromJson(string, LoginResultModel.class);
                            return result;
                        }

                        @Override
                        public void onError(okhttp3.Call call, Exception e, int i) {

                        }

                        @Override
                        public void onResponse(Object object, int i) {

                            //object 是 parseNetworkResponse的返回值
                            if (null != object) {
                                LoginResultModel result = (LoginResultModel) object;//把通用的Object转化成指定的对象
                                if (result.getOperationResult().getResultCode() == 0) {//当返回值为0时可登录
                                    Intent intent = new Intent(MainActivity.this, MainPageActivity.class);
                                    Bundle bundle = new Bundle();

                                    if (submit_action.equals("submit")) {
                                        SharedPreferences.Editor editor = preferences.edit();
                                        // 设置数据 第一 个参数是key 第二个是相应数据
                                        editor.putString("username", et_username.getText().toString());
                                        editor.putString("password", et_password.getText().toString());
                                        // 提交 不提交设置的数据将无效
                                        editor.apply();
                                    }

                                    bundle.putString("name", et_username.getText().toString());
                                    intent.putExtras(bundle);
                                    startActivity(intent);

                                } else {
                                    Toast.makeText(MainActivity.this, result.getOperationResult().getResultMsg(), Toast.LENGTH_SHORT).show();
                                }
                            } else {//当没有返回对象时，表示网络没有联通
                                Toast.makeText(MainActivity.this, "网络异常！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "网络异常！", Toast.LENGTH_SHORT).show();
        }

    }

}
