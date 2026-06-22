package mvn.depgenerator;

public class ResponseInfo {

    final int statusCode;
    final String responseText;

    public ResponseInfo(int statusCode, String responseText) {
        this.statusCode = statusCode;
        this.responseText = responseText;
    }
}
