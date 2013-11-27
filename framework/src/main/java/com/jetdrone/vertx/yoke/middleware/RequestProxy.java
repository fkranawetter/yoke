// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.middleware
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;

/**
* RequestProxy provides web client a simple way to interact with other REST service
* providers via Yoke, meanwhile Yoke could pre-handle authentication, logging and etc.<p>
*
* In order to handler the proxy request properly, Bodyparser should be disabled for the
* path matched by RequestProxy.<p>
*/
public class RequestProxy extends Middleware {

    private final String prefix;
    private final String host;
    private final int port;
    private final boolean secure;

    @Override
    public Middleware init(final Vertx vertx, final Logger logger, final String mount) {
        super.init(vertx, logger, mount);
        return this;
    }

    public RequestProxy(String prefix, String host, int port, boolean secure) {
        this.prefix = prefix;
        this.host = host;
        this.port = port;
        this.secure = secure;
    }

    public RequestProxy(String prefix, int port, boolean secure) {
        this(prefix, "localhost", port, secure);
    }

    @Override
    public void handle(final YokeRequest req, final Handler<Object> next) {
        if (!req.uri().startsWith(prefix)) {
          next.handle(null);
          return;
        }
        final String newUri = req.uri().replaceFirst(prefix, "");
        final HttpClient client = vertx.createHttpClient().setHost(host).setPort(port);

        if (secure) {
            client.setSSL(true);
        }
        
        final HttpClientRequest cReq = client.request(req.method(), newUri, new Handler<HttpClientResponse>() {
          public void handle(HttpClientResponse cRes) {
            req.response().setStatusCode(cRes.statusCode());
            req.response().headers().set(cRes.headers());
            req.response().setChunked(true);
            cRes.dataHandler(new Handler<Buffer>() {
              public void handle(Buffer data) {
                req.response().write(data);
              }
            });
            cRes.endHandler(new VoidHandler() {
              public void handle() {
                req.response().end();
              }
            });
            cRes.exceptionHandler(new Handler<Throwable>() {
              public void handle(Throwable t) {
                next.handle(t);
              }
            });
          }
        });
        cReq.headers().set(req.headers());
        cReq.setChunked(true);
        req.dataHandler(new Handler<Buffer>() {
          public void handle(Buffer data) {
            cReq.write(data);
          }
        });
        req.endHandler(new VoidHandler() {
          public void handle() {
            cReq.end();
          }
        });
    }
}
