/*
 * Copyright 2012-2013 the original author or authors.
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

package griffon.plugins.rest

import groovyx.net.http.AsyncHTTPBuilder
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient

import java.util.concurrent.ConcurrentHashMap

/**
 * @author Andres Almiray
 */
class RestClientHolder {
    private static final RestClientHolder INSTANCE

    static {
        INSTANCE = new RestClientHolder()
    }

    static RestClientHolder getInstance() {
        INSTANCE
    }

    private final Map<String, HTTPBuilder> HTTP = new ConcurrentHashMap<String, HTTPBuilder>()
    private final Map<String, RESTClient> REST = new ConcurrentHashMap<String, RESTClient>()
    private final Map<String, AsyncHTTPBuilder> ASYNC_HTTP = new ConcurrentHashMap<String, AsyncHTTPBuilder>()

    private RestClientHolder() {

    }

    String[] getHttpClientIds() {
        List<String> ids = []
        ids.addAll(HTTP.keySet())
        ids.toArray(new String[ids.size()])
    }

    HTTPBuilder getHttpClient(String id) {
        HTTP[id]
    }

    void setHttpClient(String id, HTTPBuilder client) {
        HTTP[id] = client
    }

    String[] getRestClientIds() {
        List<String> ids = []
        ids.addAll(REST.keySet())
        ids.toArray(new String[ids.size()])
    }

    RESTClient getRestClient(String id) {
        REST[id]
    }

    void setRestClient(String id, RESTClient client) {
        REST[id] = client
    }

    String[] getAsyncHttpClientIds() {
        List<String> ids = []
        ids.addAll(ASYNC_HTTP.keySet())
        ids.toArray(new String[ids.size()])
    }

    AsyncHTTPBuilder getAsyncHttpClient(String id) {
        ASYNC_HTTP[id]
    }

    void setAsyncHttpClient(String id, AsyncHTTPBuilder client) {
        ASYNC_HTTP[id] = client
    }

    // ======================================================

    HTTPBuilder fetchHttpClient(Map<String, Object> params) {
        (HTTPBuilder) fetchClient(HTTP, HTTPBuilder, params)
    }

    RESTClient fetchRestClient(Map<String, Object> params) {
        (RESTClient) fetchClient(REST, RESTClient, params)
    }

    AsyncHTTPBuilder fetchAsyncHttpClient(Map<String, Object> params) {
        (AsyncHTTPBuilder) fetchClient(ASYNC_HTTP, AsyncHTTPBuilder, params)
    }

    private fetchClient(Map clientStore, Class klass, Map<String, Object> params) {
        def client = clientStore[(params.id).toString()]
        if (client == null) {
            String id = params.id ? params.remove('id').toString() : '<EMPTY>'
            client = RestConnector.instance.createClient(klass, params)
            if (id != '<EMPTY>') clientStore[id] = client
        }

        if (params.containsKey('proxy')) {
            Map proxyArgs = [scheme: 'http', port: 80] + params.remove('proxy')
            if (!proxyArgs.host) throw new IllegalArgumentException('proxy.host cannot be null!')
            client.setProxy(proxyArgs.host, proxyArgs.port as int, proxyArgs.scheme)
        }

        client
    }
}
