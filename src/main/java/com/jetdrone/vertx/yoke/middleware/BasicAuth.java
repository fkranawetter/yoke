/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;

import javax.xml.bind.DatatypeConverter;

public class BasicAuth extends Middleware {

    public abstract static class AuthHandler {
        public abstract void handle(String username, String password, Handler<Boolean> result);
    }

    private final String realm;
    private final AuthHandler authHandler;

    public BasicAuth(final String username, final String password, String realm) {
        this.realm = realm;
        authHandler = new AuthHandler() {
            @Override
            public void handle(String _username, String _password, Handler<Boolean> result) {
                result.handle(username.equals(_username) && password.equals(_password));
            }
        };
    }

    public BasicAuth(String username, String password) {
        this (username, password, "Authentication required");

    }

    public BasicAuth(AuthHandler authHandler, String realm) {
        this.realm = realm;
        this.authHandler = authHandler;
    }

    public BasicAuth(AuthHandler authHandler) {
        this(authHandler, "Authentication required");
    }

    @Override
    public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
        String authorization = request.headers().get("authorization");

        if (authorization == null) {
            YokeHttpServerResponse response = request.response();
            response.putHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
            response.setStatusCode(401);
            next.handle("No authorization token");
        } else {
            String[] parts = authorization.split(" ");
            String scheme = parts[0];
            String[] credentials = new String(DatatypeConverter.parseBase64Binary(parts[1])).split(":");
            final String user = credentials[0];
            final String pass = credentials[1];

            if (!"Basic".equals(scheme)) {
                next.handle(400);
            } else {
                authHandler.handle(user, pass, new Handler<Boolean>() {
                    @Override
                    public void handle(Boolean valid) {
                        if (valid) {
                            request.put("user", user);
                            next.handle(null);
                        } else {
                            YokeHttpServerResponse response = request.response();
                            response.putHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
                            response.setStatusCode(401);
                            next.handle("No authorization token");
                        }
                    }
                });
            }
        }
    }
}
