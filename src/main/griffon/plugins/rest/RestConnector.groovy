/*
 * Copyright 2009-2012 the original author or authors.
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

import griffon.util.CallableWithArgs
import groovyx.net.http.AsyncHTTPBuilder
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient
import java.util.concurrent.ConcurrentHashMap

import java.lang.reflect.InvocationTargetException

/**
 * @author Andres Almiray
 */
@Singleton
class RestConnector implements RestProvider { 
    private final Map BUILDERS = new ConcurrentHashMap()
    
    Object withAsyncHttp(Map params, Closure closure) {
        return doWithBuilder(AsyncHTTPBuilder, params, closure)
    }
       
    Object withHttp(Map params, Closure closure) {
        return doWithBuilder(HTTPBuilder, params, closure)
    } 
       
    Object withRest(Map params, Closure closure) {
        return doWithBuilder(RESTClient, params, closure)
    }
    
    public <T> T withAsyncHttp(Map params, CallableWithArgs<T> callable) {
        return doWithBuilder(AsyncHTTPBuilder, params, callable)
    } 
  
    public <T> T withHttp(Map params, CallableWithArgs<T> callable) {
        return doWithBuilder(HTTPBuilder, params, callable)
    } 

    public <T> T withRest(Map params, CallableWithArgs<T> callable) {
        return doWithBuilder(RESTClient, params, callable)
    }

    // ======================================================

    private Object doWithBuilder(Class klass, Map params, Closure closure) {
        def builder = configureBuilder(klass, params)

        if (closure) {
            closure.delegate = builder
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            return closure()
        }
        return null
    }

    private <T> T doWithBuilder(Class klass, Map params, CallableWithArgs<T> callable) {
        def builder = configureBuilder(klass, params)

        if (callable) {
            callable.args = [builder] as Object[]
            return callable.run()
        }
        return null
    }

    private configureBuilder(Class klass, Map params) {
        def builder = null
        if (params.id) {
            String id = params.remove('id').toString()
            builder = BUILDERS[id]
            if(builder == null) {
                builder = makeBuilder(klass, params)
                BUILDERS[id] = builder 
            }
        } else {
            builder = makeBuilder(klass, params)
        }

        if (params.containsKey('proxy')) {
            Map proxyArgs = [scheme: 'http', port: 80] + params.remove('proxy')
            if (!proxyArgs.host) throw new IllegalArgumentException('proxy.host cannot be null!')
            builder.setProxy(proxyArgs.host, proxyArgs.port as int, proxyArgs.scheme)
        }
        
        builder
    }

    private makeBuilder(Class klass, Map params) {
        if (klass == AsyncHTTPBuilder) {
            try {
                Map args = [:]
                for(arg in ['threadPool', 'poolSize', 'uri', 'contentType', 'timeout']) {
                    if (params[(arg)] != null) args[(arg)] = params[(arg)]
                }
                return klass.newInstance(args)
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Failed to create async http builder, reason: $e", e)
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException("Failed to create async http builder, reason: $e", e)
            }
        }
        try {
            def builder = klass.newInstance()
            if (params.uri) builder.uri = params.remove('uri')
            if (params.contentType) builder.contentType = params.remove('contentType')
            return builder
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to create ${(klass == HTTPBuilder ? 'http' : 'rest')} builder, reason: $e", e)
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to create ${(klass == HTTPBuilder ? 'http' : 'rest')} builder, reason: $e", e)
        }
    }
}
