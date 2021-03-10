package kollus.test.media.download;

public class DownloadDRM {
    private String mRequest;
    private String mResponse;

    public DownloadDRM(String request, String response) {
        mRequest = request;
        mResponse = response;
    }

    public String getRequest() {
        return mRequest;
    }

    public String getResponse() {
        return mResponse;
    }
}
