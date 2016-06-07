package cn.zhoujia.appupdate.Bean;

/**
 * Created by Zhoujia on 2016/6/7.
 */
public class Update {

    /**
     * AppUpdater : true
     * errMsg : success
     * force : false
     * latestVersion : 1.2.2
     * updateinfo : 1.这里是更新内容1/n 2.这里是更新内容2
     * url : https://github.com/javiersantos/AppUpdater/releases.apk
     */

    private boolean AppUpdater;
    private String errMsg;
    private boolean force;
    private String latestVersion;
    private String updateinfo;
    private String url;

    public boolean isAppUpdater() {
        return AppUpdater;
    }

    public void setAppUpdater(boolean AppUpdater) {
        this.AppUpdater = AppUpdater;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getUpdateinfo() {
        return updateinfo;
    }

    public void setUpdateinfo(String updateinfo) {
        this.updateinfo = updateinfo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
