griffon.project.dependency.resolution = {
    inherits "global"
    repositories {
        griffonHome()
        mavenCentral()
        mavenRepo name: 'Codehaus', root: 'http://repository.codehaus.org', m2compatible: true
    }
    dependencies {
        compile('org.codehaus.groovy.modules.http-builder:http-builder:0.5.2') {
            excludes 'commons-logging', 'xml-apis', 'groovy', 'log4j', 'xercesImpl'
        }
        compile('org.apache.httpcomponents:httpclient:4.1.2',
                'net.sf.json-lib:json-lib:2.4:jdk15',
                'net.sf.ezmorph:ezmorph:1.0.6',
                'commons-beanutils:commons-beanutils:1.8.0',
                'commons-lang:commons-lang:2.6',
                'commons-collections:commons-collections:3.2.1',
                'commons-codec:commons-codec:1.6',
                'org.apache.httpcomponents:httpcore:4.1.2',
                'xml-resolver:xml-resolver:1.2',
                'xerces:xercesImpl:2.9.1') {
            excludes 'commons-logging'
        }
    }
}

griffon {
    doc {
        logo = '<a href="http://griffon.codehaus.org" target="_blank"><img alt="The Griffon Framework" src="../img/griffon.png" border="0"/></a>'
        sponsorLogo = "<br/>"
        footer = "<br/><br/>Made with Griffon (@griffon.version@)"
    }
}

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }

    error 'org.codehaus.griffon',
          'org.springframework',
          'org.apache.karaf',
          'groovyx.net'
    warn  'griffon'
}
