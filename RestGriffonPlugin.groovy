/*
 * Copyright 2009-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */
class RestGriffonPlugin {
    // the plugin version
    String version = '1.0.0'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.2.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [lombok: '0.4']
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-rest-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'REST client support'

    String description = '''
The Rest plugin enables the usage of [groovy-rest][1] on a Griffon application.

Usage
-----

The plugin will inject the following dynamic methods:

 * `<R> R withRest(Map<String, Object> params, Closure<R> stmts)` - executes stmts using a RESTClient
 * `<R> R withHttp(Map<String, Object> params, Closure<R> stmts)` - executes stmts using an HTTPBuilder
 * `<R> R withAsyncHttp(Map<String, Object> params, Closure<R> stmts)` - executes stmts using a AsyncHTTPBuilder
 * `<R> R withRest(Map<String, Object> params, CallableWithArgs<R> stmts)` - executes stmts using a RESTClient
 * `<R> R withHttp(Map<String, Object> params, CallableWithArgs<R> stmts)` - executes stmts using an HTTPBuilder
 * `<R> R withAsyncHttp(Map<String, Object> params, CallableWithArgs<R> stmts)` - executes stmts using a AsyncHTTPBuilder

Where params may contain

| Property     | Type            | Required |
| ------------ | --------------- | -------- |
| uri          | String          | yes      |
| contentType  | String          |          |
| id           | String          |          |

All dynamic methods will create a new client when invoked unless you define an
`id:` attribute. When this attribute is supplied the client will be stored in a
cache managed by the `RestProvider` that handled the call. You may specify
parameters for configuring an HTTP proxy, for example

| Property     | Type            | Required | Default |
| ------------ | --------------- | -------- | ------- |
| scheme       | String          |          | http    |
| port         | int             |          | 80      |
| host         | String          | yes      |         |
| username     | String          |          |         |
| password     | String          |          |         |

Configuring a proxy host for `http://acme.com:81` can be done in this way

    withRest(uri: 'http://foo.com/', proxy: [host: 'acme.com', port: 81]) {
        ...
    }

The method `withAsyncHttp` accepts the following additional properties

| Property   | Type            |
| ---------- | --------------- |
| threadPool | ExecutorService |
| poolSize   | int             |
| timeout    | int             |

These methods are also accessible to any component through the singleton
`griffon.plugins.rest.RestEnhancer`. You can inject these methods to
non-artifacts via metaclasses. Simply grab hold of a particular metaclass and
call `RestEnhancer.enhance(metaClassInstance)`.

Configuration
-------------

### RestAware AST Transformation

The preferred way to mark a class for method injection is by annotating it with
`@griffon.plugins.rest.RestAware`. This transformation injects the
`griffon.plugins.rest.RestContributionHandler` interface and default behavior
that fulfills the contract.

### Dynamic Method Injection

Dynamic methods will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.rest.injectInto = ['controller', 'service']

Dynamic method injection wil skipped for classes implementing
`griffon.plugins.rest.RestContributionHandler`.

### Example

This example relies on [Grails][2] as the service provider. Follow these steps
to configure the service on the Grails side:

1. Download a copy of [Grails][3] and install it.
2. Create a new Grails application. We'll pick 'exporter' as the application name.

        grails create-app exporter
    
3. Create a controller named `Calculator`

        grails create-controller calculator
    
4. Paste the following code in `grails-app/controllers/exporter/CalculatorController.groovy`

        package exporter
        import grails.converters.JSON
        class CalculatorController {
            def add() {
                double a = params.a.toDouble()
                double b = params.b.toDouble()
                render([result: (a + b)] as JSON)
            }
        }

5. Run the application

        grails run-app

Now we're ready to build the Griffon application

1. Create a new Griffon application. We'll pick `calculator` as the application
   name

        griffon create-app calculator

2. Install the rest plugin

        griffon install-plugin rest

3. Fix the view script to look like this

        package calculator
        application(title: 'Rest Plugin Example',
          pack: true,
          locationByPlatform: true,
          iconImage: imageIcon('/griffon-icon-48x48.png').image,
          iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                       imageIcon('/griffon-icon-32x32.png').image,
                       imageIcon('/griffon-icon-16x16.png').image]) {
            gridLayout(cols: 2, rows: 4)
            label('Num1:')
            textField(columns: 20, text: bind(target: model, targetProperty: 'num1'))
            label('Num2:')
            textField(columns: 20, text: bind(target: model, targetProperty: 'num2'))
            label('Result:')
            label(text: bind{model.result})
            button(calculateAction, enabled: bind{model.enabled})
        }

4. Let's add required properties to the model

        package calculator
        @Bindable
        class CalculatorModel {
            String num1
            String num2
            String result
            boolean enabled = true
        }

5. Now for the controller code. Notice that there is minimal error handling in
   place. If the user types something that is not a number the client will
   surely break, but the code is sufficient for now.

        package calculator
        import rest.rest.ContentType
        @griffon.plugins.rest.RestAware
        class CalculatorController {
            def model

            def calculate = { evt = null ->
                String a = model.num1
                String b = model.num2
                execInsideUIAsync { model.enabled = false }
                try {
                    def result = withRest(url: 'http://localhost:8080/exporter/calculator/', id: 'client') {
                        def response = get(path: 'add', query: [a: a, b: b], accept: ContentType.JSON)
                        response.json.result
                    }
                    execInsideUIAsync { model.result = result }
                } finally {
                    execInsideUIAsync { model.enabled = true }
                }
            }
        }
    
6. Run the application

        griffon run-app

The plugin exposes a Java friendly API to make the exact same calls from Java,
or any other JVM language for that matter. Here's for example the previous code
rewritten in Java. Note the usage of @RestWare on a Java class

    package calculator;
    import static griffon.util.CollectionUtils.newMap;
    import griffon.util.CallableWithArgs;
    import griffon.util.CollectionUtils;
    import groovyx.net.http.HttpResponseDecorator;
    import groovyx.net.http.RESTClient;
    import java.awt.event.ActionEvent;
    import java.util.Map;
    import net.sf.json.JSONObject;
    import org.codehaus.griffon.runtime.core.AbstractGriffonController;
    @griffon.plugins.rest.RestAware
    public class CalculatorController extends AbstractGriffonController {
        private CalculatorModel model;

        public void setModel(CalculatorModel model) {
            this.model = model;
        }

        public void calculate(ActionEvent event) {
            final String a = model.getNum1();
            final String b = model.getNum2();
            enableModel(false);
            try {
                Map<String, Object> params = CollectionUtils.<String, Object> map()
                        .e("uri", "http://localhost:8080/exporter/calculator/")
                        .e("id", "client");
                final String result = withRest(params,
                        new CallableWithArgs<String>() {
                            public String call(Object[] args) {
                                RESTClient client = (RESTClient) args[0];
                                try {
                                    HttpResponseDecorator response = (HttpResponseDecorator) client.get(
                                        newMap(
                                            "path", "add",  
                                            "query", newMap("a", a, "b", b)));
                                    JSONObject json = (JSONObject) response.getData();
                                    return json.getString("result");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return "";
                            }
                        });
                execInsideUIAsync(new Runnable() {
                    public void run() {
                        model.setResult(result);
                    }
                });
            } finally {
                enableModel(true);
            }
        }

        private void enableModel(final boolean enabled) {
            execInsideUIAsync(new Runnable() {
                public void run() {
                    model.setEnabled(enabled);
                }
            });
        }
    }


Testing
-------

Dynamic methods will not be automatically injected during unit testing, because
addons are simply not initialized for this kind of tests. However you can use
`RestEnhancer.enhance(metaClassInstance, restProviderInstance)` where
`restProviderInstance` is of type `griffon.plugins.rest.RestProvider`.
The contract for this interface looks like this

    public interface RestProvider {
        <R> R withAsyncHttp(Map<String, Object> params, Closure<R> closure);
        <R> R withHttp(Map<String, Object> params, Closure<R> closure);
        <R> R withRest(Map<String, Object> params, Closure<R> closure);
        <R> R withAsyncHttp(Map<String, Object> params, CallableWithArgs<R> callable);
        <R> R withHttp(Map<String, Object> params, CallableWithArgs<R> callable);
        <R> R withRest(Map<String, Object> params, CallableWithArgs<R> callable);
    }

It's up to you define how these methods need to be implemented for your tests.
For example, here's an implementation that never fails regardless of the
arguments it receives

    class MyRestProvider implements RestProvider {
        public <R> R withAsyncHttp(Map<String, Object> params, Closure<R> closure) { null }
        public <R> R withHttp(Map<String, Object> params, Closure<R> closure) { null }
        public <R> R withRest(Map<String, Object> params, Closure<R> closure) { null }
        public <R> R withAsyncHttp(Map<String, Object> params, CallableWithArgs<R> callable) { null }
        public <R> R withHttp(Map<String, Object> params, CallableWithArgs<R> callable) { null }
        public <R> R withRest(Map<String, Object> params, CallableWithArgs<R> callable) { null }
    }
    
This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            RestEnhancer.enhance(service.metaClass, new MyRestProvider())
            // exercise service methods
        }
    }

On the other hand, if the service is annotated with `@RestAware` then usage
of `RestEnhancer` should be avoided at all costs. Simply set
`restProviderInstance` on the service instance directly, like so, first the
service definition

    @griffon.plugins.rest.RestAware
    class MyService {
        def serviceMethod() { ... }
    }

Next is the test

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            service.restProvider = new MyRestProvider()
            // exercise service methods
        }
    }

Tool Support
------------

### DSL Descriptors

This plugin provides DSL descriptors for Intellij IDEA and Eclipse (provided
you have the Groovy Eclipse plugin installed). These descriptors are found
inside the `griffon-rest-compile-x.y.z.jar`, with locations

 * dsdl/rest.dsld
 * gdsl/rest.gdsl

### Lombok Support

Rewriting Java AST in a similar fashion to Groovy AST transformations is
posisble thanks to the [lombok][4] plugin.

#### JavaC

Support for this compiler is provided out-of-the-box by the command line tools.
There's no additional configuration required.

#### Eclipse

Follow the steps found in the [Lombok][4] plugin for setting up Eclipse up to
number 5.

 6. Go to the path where the `lombok.jar` was copied. This path is either found
    inside the Eclipse installation directory or in your local settings. Copy
    the following file from the project's working directory

         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/rest-<version>/dist/griffon-rest-compile-<version>.jar .

 6. Edit the launch script for Eclipse and tweak the boothclasspath entry so
    that includes the file you just copied

        -Xbootclasspath/a:lombok.jar:lombok-pg-<version>.jar:\
        griffon-lombok-compile-<version>.jar:griffon-rest-compile-<version>.jar

 7. Launch Eclipse once more. Eclipse should be able to provide content assist
    for Java classes annotated with `@RestAware`.

#### NetBeans

Follow the instructions found in [Annotation Processors Support in the NetBeans
IDE, Part I: Using Project Lombok][5]. You may need to specify
`lombok.core.AnnotationProcessor` in the list of Annotation Processors.

NetBeans should be able to provide code suggestions on Java classes annotated
with `@RestAware`.

#### Intellij IDEA

Follow the steps found in the [Lombok][4] plugin for setting up Intellij IDEA
up to number 5.

 6. Copy `griffon-rest-compile-<version>.jar` to the `lib` directory

         $ pwd
           $USER_HOME/Library/Application Support/IntelliJIdea11/lombok-plugin
         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/rest-<version>/dist/griffon-rest-compile-<version>.jar lib

 7. Launch IntelliJ IDEA once more. Code completion should work now for Java
    classes annotated with `@RestAware`.

[1]: http://groovy.codehaus.org/modules/http-builder
[2]: http://grails.org
[3]: http://grails.org/Download
[4]: /plugin/lombok
[5]: http://netbeans.org/kb/docs/java/annotations-lombok.html
'''
}
