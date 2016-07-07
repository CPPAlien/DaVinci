package cn.hadcn.davinci.other;

/**
 *
 * Created by 90Chris on 2016/7/6.
 */
public interface OnVinciDownloadListener {
    void onVinciDownloadSuccess();
    void onVinciDownloadFailed(String reason);
    void onVinciDownloadProgress(int progress);
}
