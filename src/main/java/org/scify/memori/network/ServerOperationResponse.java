package org.scify.memori.network;

public class ServerOperationResponse {
    private int code;
    private String message;
    private Object parameters;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getParameters() {
        return parameters;
    }

    public ServerOperationResponse(int code, String message, Object parameters) {
        this.code = code;
        this.message = message;
        this.parameters = parameters;

    }

    public void setParameters(Object parameters) {
        this.parameters = parameters;
    }
}
