package edu.csh.cshwebnews.exceptions;

public class ResponseException extends Exception {
    public ResponseException() {
        super();
    }

    public ResponseException(String detailMessage) {
        super(detailMessage);
    }
}
