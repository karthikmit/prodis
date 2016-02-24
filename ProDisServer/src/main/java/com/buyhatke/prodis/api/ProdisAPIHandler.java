package com.buyhatke.prodis.api;

import com.buyhatke.core.Entry;
import com.buyhatke.prodis.exceptions.NoKeyFoundException;
import com.buyhatke.prodis.handlers.event.EventType;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.Reactor;
import reactor.event.Event;
import reactor.function.Consumer;
import reactor.net.NetChannel;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static reactor.event.selector.Selectors.$;

/**
 * ProdisAPIHandler is responsible for HTTP Request processing and send the response back to the callers.
 * Reactor is being used to dispatch the call to concerned handlers to fetch the response from service layers.
 */
@Component
public class ProdisAPIHandler {
    private Reactor reactor;
    private Logger logger = LoggerFactory.getLogger(ProdisAPIHandler.class);

    @Autowired
    public ProdisAPIHandler(Reactor reactor) {
        this.reactor = reactor;
    }

    public Consumer<FullHttpRequest> handleApi(NetChannel<FullHttpRequest, FullHttpResponse> channel) {
        return req -> {
            reactor.on($(NoKeyFoundException.class), ev -> {
                logger.error("No key found exception received");
                final FullHttpResponse httpResponse = resourceNotFoundResponse("Key not found");
                channel.send(httpResponse);
            });

            if(req.getMethod() == HttpMethod.GET && req.getUri().startsWith("/get")) {
                // Fetch  API ...
                handleFetchGetApi(channel, req);
            } else if(req.getMethod() == HttpMethod.POST && req.getUri().startsWith("/put")) {
                handleSavePostApi(channel, req);
            }
        };
    }

    private void handleSavePostApi(NetChannel<FullHttpRequest, FullHttpResponse> channel, FullHttpRequest req) {
        Entry entry = null;
        try {
            String valueJson = readContent(req.content());
            final Map<String, Object> valuesMap = new Gson().fromJson(valueJson, Map.class);

            if(valuesMap.containsKey("key") && valuesMap.containsKey("value")) {
                entry = new Entry().setKey((String) valuesMap.get("key"))
                            .setValue((String) valuesMap.get("value"));
                if(valuesMap.containsKey("expiresIn") && valuesMap.containsKey("expiresTimeUnit")) {
                    String expiresInString = (String) valuesMap.get("expiresIn");
                    Integer expiresIn = Integer.parseInt(expiresInString);
                    entry.setExpiresIn(expiresIn);
                    String expiresTimeUnit = ((String) valuesMap.get("expiresTimeUnit")).toLowerCase();

                    if(expiresTimeUnit.equals("d")) {
                        entry.setExpiresInUnit(TimeUnit.DAYS);
                    } else if(expiresTimeUnit.equals("h")) {
                        entry.setExpiresInUnit(TimeUnit.HOURS);
                    } else if(expiresTimeUnit.equals("m")) {
                        entry.setExpiresInUnit(TimeUnit.MINUTES);
                    } else {
                        // Default is one day ...
                        entry.setExpiresInUnit(TimeUnit.DAYS);
                        entry.setExpiresIn(1);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        reactor.sendAndReceive(EventType.CACHE_SAVE, Event.wrap(entry), ev -> {
            final Entry entryStored = (Entry) ev.getData();
            channel.send(successfulKeyStoreResponse(entryStored));
        });
    }

    private void handleFetchGetApi(NetChannel<FullHttpRequest, FullHttpResponse> channel, FullHttpRequest req) {
        String key;
        final String uri = req.getUri();
        final String[] uriTokens = uri.split("/");
        key = uriTokens[uriTokens.length - 1];

        reactor.sendAndReceive(EventType.CACHE_FETCH, Event.wrap(key), ev -> {
            final Entry entryStored = (Entry) ev.getData();
            channel.send(successfulKeyFetchResponse(entryStored));
        });
    }

    private FullHttpResponse successfulAllStoresResponse(List<Map<String, Object>> storeInfoList) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK);

        String text = new Gson().toJson(storeInfoList);
        resp.headers().set(CONTENT_TYPE, "text/plain");
        resp.headers().set(CONTENT_LENGTH, text.length());

        byte[] bytes = text.getBytes();
        resp.content().writeBytes(bytes);

        return resp;
    }

    /**
     * Respond to errors occurring on a Reactor by redirecting them to the client via an HTTP 500 error response.
     *
     * @param channel
     *     the channel on which to send an HTTP response
     *
     * @return a consumer to handle HTTP requests
     */
    public Consumer<Throwable> errorHandler(NetChannel<FullHttpRequest, FullHttpResponse> channel) {
        return ev -> {
            DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR);
            resp.content().writeBytes(ev.getMessage().getBytes());
            resp.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
            resp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, resp.content().readableBytes());
            channel.send(resp);
        };
    }

    /*
     * Read POST content to a String
     */
    private static String readContent(ByteBuf content) throws IOException {
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        content.release();

        String value = new String(bytes);

        return value;
    }

    /*
     * Create an HTTP 500 Server Error response.
     */
    public static FullHttpResponse internalServerErrorResponse(String msg) {
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
    public static FullHttpResponse resourceNotFoundResponse(String msg) {
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
    public static FullHttpResponse badRequest(String msg) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
        resp.content().writeBytes(msg.getBytes());
        resp.headers().set(CONTENT_TYPE, "text/plain");
        resp.headers().set(CONTENT_LENGTH, resp.content().readableBytes());
        return resp;
    }

    /*
     * Create an HTTP 301 redirect response.
     */
    public static FullHttpResponse redirect(String location) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, MOVED_PERMANENTLY);
        resp.headers().set(CONTENT_LENGTH, 0);
        resp.headers().set(LOCATION, location);
        return resp;
    }

    public static FullHttpResponse successfulKeyStoreResponse(Entry entry) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK);

        String text = new Gson().toJson(entry);
                resp.headers().set(CONTENT_TYPE, "text/plain");
        resp.headers().set(CONTENT_LENGTH, text.length());

        byte[] bytes = text.getBytes();
        resp.content().writeBytes(bytes);

        return resp;
    }

    public static FullHttpResponse successfulKeyFetchResponse(Entry entry) {
        final String storedValue = new Gson().toJson(entry);

        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK);
        resp.headers().set(CONTENT_TYPE, "text/plain");
        resp.headers().set(CONTENT_LENGTH, storedValue.length());

        byte[] bytes = storedValue.getBytes();
        resp.content().writeBytes(bytes);

        return resp;
    }
}
