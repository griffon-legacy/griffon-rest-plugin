/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.plugins.rest;

import groovyx.net.http.AsyncHTTPBuilder;
import groovyx.net.http.HTTPBuilder;
import groovyx.net.http.RESTClient;

import java.util.Map;

/**
 * @author Andres Almiray
 */
public class DefaultRestProvider extends AbstractRestProvider {
    private static final DefaultRestProvider INSTANCE;

    static {
        INSTANCE = new DefaultRestProvider();
    }

    public static DefaultRestProvider getInstance() {
        return INSTANCE;
    }

    @Override
    protected HTTPBuilder getHttpClient(Map<String, Object> params) {
        return RestClientHolder.getInstance().fetchHttpClient(params);
    }

    @Override
    protected RESTClient getRestClient(Map<String, Object> params) {
        return RestClientHolder.getInstance().fetchRestClient(params);
    }

    @Override
    protected AsyncHTTPBuilder getAsyncHttpClient(Map<String, Object> params) {
        return RestClientHolder.getInstance().fetchAsyncHttpClient(params);
    }
}
