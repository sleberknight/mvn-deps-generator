package mvn.depgenerator;

import javax.ws.rs.core.Response;

public class ResponseInfo {

    final int statusCode;
    final Response.StatusType status;
    final String responseText;

    public ResponseInfo(Response.StatusType status, String responseText) {
        this.statusCode = status.getStatusCode();
        this.status = status;
        this.responseText = responseText;
    }
}
