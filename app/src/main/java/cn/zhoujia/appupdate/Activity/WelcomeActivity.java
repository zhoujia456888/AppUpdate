package cn.zhoujia.appupdate.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.IOException;

import cn.zhoujia.appupdate.Bean.Update;
import cn.zhoujia.appupdate.R;
import cn.zhoujia.appupdate.Service.DownloadFileService;
import cn.zhoujia.appupdate.Util.Util;
import cn.zhoujia.appupdate.Util.VersionStringComparator;
import sexy.code.Callback;
import sexy.code.HttPizza;
import sexy.code.Request;
import sexy.code.Response;

/**
 * Created by Zhoujia on 2016/6/7.
 */
public class WelcomeActivity extends Activity {

    Activity activity = WelcomeActivity.this;
    private VersionStringComparator comparator;
    HttPizza client;

    protected static final int forceupdater = 0;
    protected static final int unforceupdater = 1;

    String UpdateUrl = "http://www.jloveh.com/update.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        client = new HttPizza();

        //检测是否链接上网络
        if (Util.checkNetType(activity)) {
            checkVersion();
        } else {
            new MaterialDialog.Builder(activity)
                    .title(getString(R.string.neterror))
                    .content(getString(R.string.neterrorinfo))
                    .positiveText(R.string.ok)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            switch (which) {
                                case POSITIVE:
                                    checkVersion();
                                    break;
                            }
                        }
                    })
                    .show();
        }

    }

    //检查版本更新
    private void checkVersion() {
        if (comparator == null) {
            comparator = new VersionStringComparator();
        }

        Request request = client.newRequest()
                .url(UpdateUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(final Response response) {
                if (response.isSuccessful()) {
                    //请求成功
                    new Thread() {
                        @Override
                        public void run() {
                            //把网络访问的代码放在这里
                            try {
                                AnalyzeData(response.body().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } else {
                    //请求失败
                    //失败了就再来一次
                    checkVersion();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                //失败了就再来一次
                checkVersion();
            }
        });
    }

    //解析获取到的数据
    private void AnalyzeData(String responseString) throws JSONException {
        Gson gson = new Gson();
        Update update = gson.fromJson(responseString, Update.class);
        Boolean AppUpdater = update.isAppUpdater();//是否有更新
        String errMsg = update.getErrMsg();//
        Boolean force = update.isForce();//是否强制更新
        String updateinfo = update.getUpdateinfo();//更新信息
        String latestVersion = update.getLatestVersion();//新的版本号
        String url = update.getUrl();//软件地址

        if (comparator.compare(latestVersion, Util.getVersion(activity)) >= 1) {
            Message msg = new Message();
            if (force) {
                msg.what = forceupdater;

            } else {
                msg.what = unforceupdater;
            }
            Bundle bundle = new Bundle();
            bundle.putString("updateinfo", updateinfo);
            bundle.putString("latestVersion", latestVersion);
            bundle.putString("url", url);
            msg.setData(bundle);
            handler.sendMessage(msg);
        } else {
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
            WelcomeActivity.this.finish();
        }


    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String updateinfo = msg.getData().getString("updateinfo");
            String latestVersion = msg.getData().getString("latestVersion");
            final String url = msg.getData().getString("url");
            switch (msg.what) {
                case forceupdater:
                    new MaterialDialog.Builder(activity)
                            .title(R.string.uptate_title)
                            .content("新版本：" + latestVersion + "\n" + updateinfo)
                            .positiveText(R.string.ok)
                            .negativeText(R.string.cancel)
                            .autoDismiss(false)
                            .canceledOnTouchOutside(false)
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    switch (which) {
                                        case POSITIVE:
                                            if (!DownloadFileService.isDownload) {
                                                Intent intent = new Intent(getApplicationContext(), DownloadFileService.class);
                                                Bundle bundle = new Bundle();
                                                bundle.putString("fileName", "文件名");
                                                bundle.putString("fileUrl", url);
                                                intent.putExtras(bundle);
                                                startService(intent);

                                                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(mainIntent);
                                                activity.finish();
                                                Toast.makeText(activity, "转入后台下载", Toast.LENGTH_SHORT).show();
                                            }
                                            break;
                                        case NEGATIVE:
                                            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(mainIntent);
                                            activity.finish();
                                            break;
                                    }
                                }
                            })
                            .show();
                    break;
                case unforceupdater:
                    new MaterialDialog.Builder(activity)
                            .title(getString(R.string.uptate_title))
                            .content(updateinfo)
                            .positiveText(R.string.ok)
                            .autoDismiss(false)
                            .canceledOnTouchOutside(false)
                            .keyListener(Util.keylistener)//dialog不消失
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    switch (which) {
                                        case POSITIVE:
                                            if (!DownloadFileService.isDownload) {
                                                Intent intent = new Intent(getApplicationContext(), DownloadFileService.class);
                                                Bundle bundle = new Bundle();
                                                bundle.putString("fileName", "文件名");
                                                bundle.putString("fileUrl", url);
                                                intent.putExtras(bundle);
                                                startService(intent);

                                                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(mainIntent);
                                                activity.finish();

                                                Toast.makeText(activity, "转入后台下载", Toast.LENGTH_SHORT).show();
                                            }
                                            break;
                                    }
                                }
                            })
                            .show();
                    break;
            }
        }
    };

}
