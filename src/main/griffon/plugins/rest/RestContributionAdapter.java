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

import griffon.util.CallableWithArgs;
import groovy.lang.Closure;

import java.util.Map;

/**
 * @author Andres Almiray
 */
public class RestContributionAdapter implements RestContributionHandler {
    private RestProvider provider = DefaultRestProvider.getInstance();

    public void setRestProvider(RestProvider provider) {
        this.provider = provider != null ? provider : DefaultRestProvider.getInstance();
    }

    public RestProvider getRestProvider() {
        return provider;
    }

    public <R> R withAsyncHttp(Map<String, Object> params, Closure<R> closure) {
        return provider.withAsyncHttp(params, closure);
    }

    public <R> R withHttp(Map<String, Object> params, Closure<R> closure) {
        return provider.withHttp(params, closure);
    }

    public <R> R withRest(Map<String, Object> params, Closure<R> closure) {
        return provider.withRest(params, closure);
    }

    public <R> R withAsyncHttp(Map<String, Object> params, CallableWithArgs<R> callable) {
        return provider.withAsyncHttp(params, callable);
    }

    public <R> R withHttp(Map<String, Object> params, CallableWithArgs<R> callable) {
        return provider.withHttp(params, callable);
    }

    public <R> R withRest(Map<String, Object> params, CallableWithArgs<R> callable) {
        return provider.withRest(params, callable);
    }
}
