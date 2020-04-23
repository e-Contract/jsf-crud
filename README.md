JSF CRUD Project
================

The JSF CRUD Project delivers a JSF component, based on PrimeFaces, to perform simple CRUD operations on JPA entities.
The component has been tested on the following application servers:
* JBoss EAP 6.4.22
* JBoss EAP 7.3.0
* WildFly 19.0.0
* Payara 5.201
* Oracle WebLogic Server 14.1.1
* Apache TomEE 8.0.1
* Open Liberty 20.0.0.4 (EL FunctionMapper not available)

The component has been tested with PrimeFaces versions:
* 7.0
* 7.0.12
* 8.0

# Usage

If you are using Maven, refer to the e-contract.be Maven repository via:
```
<repository>
    <id>e-contract</id>
    <url>https://www.e-contract.be/maven2/</url>
    <releases>
        <enabled>true</enabled>
    </releases>
</repository>
```
Include the `jsf-crud-lib` JSF library within your WAR (JPA, CDI, and JSF enabled) as follows:
```
<dependency>
    <groupId>be.e-contract.jsf-crud</groupId>
    <artifactId>jsf-crud-lib</artifactId>
    <version>0.2.0</version>
</dependency>
```

Within a JSF page you can now add the following:
```
xmlns:crud="urn:be:e-contract:crud:jsf"
...
<crud:crud entity="YourEntity"/>
```
It cannot get easier than this.

# Demo

Checkout the source code via:
```
git clone https://github.com/e-Contract/jsf-crud.git
```

## WildFly

Start a WildFly via:
```
cd wildfly-19.0.0.Final/bin/
./standalone.sh --server-config=standalone-full.xml
```

Build the project via Maven:
```
cd jsf-crud
mvn clean install
```

Deploy the demo web application to the local running WildFly via:
```
cd jsf-crud-demo
mvn wildfly:deploy
```

Navigate your web browser to:
http://localhost:8080/jsf-crud-demo/


## Payara

Start Payara via:
```
cd payara5/bin/
./asadmin start-domain --verbose
```

Build the project via Maven:
```
cd jsf-crud
mvn clean install -Pglassfish
```

Deploy the demo web application to the local running Payara via:
```
./asadmin deploy ~/jsf-crud/jsf-crud-demo/target/jsf-crud-demo-1.0.0-SNAPSHOT.war
```

Navigate your web browser to:
http://localhost:8080/jsf-crud-demo/

Undeploy the demo web application via:
```
./asadmin undeploy jsf-crud-demo-1.0.0-SNAPSHOT
```

## Open Liberty

Build the project via Maven:
```
cd jsf-crud
mvn clean install
```

Run the demo web application on Open Liberty via:
```
cd jsf-crud-demo
mvn clean test -Pliberty
```

Navigate your web browser to:
http://localhost:9080/jsf-crud-demo/