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

package griffon.plugins.rest

import groovyx.net.http.AsyncHTTPBuilder
import groovyx.net.http.HTTPBuilder

import java.lang.reflect.InvocationTargetException

/**
 * @author Andres Almiray
 */
@Singleton
class RestConnector {
    public createClient(Class klass, Map params) {
        if (klass == AsyncHTTPBuilder) {
            try {
                Map args = [:]
                for (arg in ['threadPool', 'poolSize', 'uri', 'contentType', 'timeout']) {
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
