package kollus.test.media.download;

import com.kollus.sdk.media.content.KollusContent;

public class DownloadInfo {
    private String mFolder;
    private String mUrl;
    private KollusContent mContent;

    public DownloadInfo(String folder, String url) {
        mFolder = folder;
        mUrl = url;
        mContent = new KollusContent();
    }

    public String getFolder() {
        return mFolder;
    }

    public String getUrl() {
        return mUrl;
    }

    public KollusContent getKollusContent() {
        return mContent;
    }
}
