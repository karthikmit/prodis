package com.buyhatke.prodis.handlers;

import com.buyhatke.core.Entry;
import com.google.gson.Gson;
import io.netty.handler.codec.http.*;
import org.springframework.stereotype.Component;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * HTTPResponseBuilder builds various possible HTTP Responses.
 */
@Component
public class HttpResponseBuilder {

    public HttpResponseBuilder() {

    }

    /*
     * Create an HTTP 500 Server Error response.
     */
    public FullHttpResponse internalServerErrorResponse(String msg) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR);
        resp.content().writeBytes(msg.getBytes());
        resp.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        resp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, resp.content().readableBytes());
        return resp;
    }

    /*
     * Create an HTTP 404 Server Error response.
     */
    public FullHttpResponse resourceNotFoundResponse(String msg) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.NOT_FOUND);
        resp.content().writeBytes(msg.getBytes());
        resp.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        resp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, resp.content().readableBytes());
        return resp;
    }

    /*
     * Create an HTTP 400 bad request response.
     */
    public FullHttpResponse badRequest(String msg) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
        resp.content().writeBytes(msg.getBytes());
        resp.headers().set(CONTENT_TYPE, "text/plain");
        resp.headers().set(CONTENT_LENGTH, resp.content().readableBytes());
        return resp;
    }

    /*
     * Create an HTTP 301 redirect response.
     */
    public FullHttpResponse redirect(String location) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, MOVED_PERMANENTLY);
        resp.headers().set(CONTENT_LENGTH, 0);
        resp.headers().set(LOCATION, location);
        return resp;
    }

    public FullHttpResponse successfulKeyStoreResponse(Entry entry) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK);

        String text = new Gson().toJson(entry);
        resp.headers().set(CONTENT_TYPE, "text/plain");
        resp.headers().set(CONTENT_LENGTH, text.length());

        byte[] bytes = text.getBytes();
        resp.content().writeBytes(bytes);

        return resp;
    }

    public FullHttpResponse successfulKeyFetchResponse(Entry entry) {
        final String storedValue = new Gson().toJson(entry);

        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK);
        resp.headers().set(CONTENT_TYPE, "text/plain");
        resp.headers().set(CONTENT_LENGTH, storedValue.length());

        byte[] bytes = storedValue.getBytes();
        resp.content().writeBytes(bytes);

        return resp;
    }
}
