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
Include the `jsf-crud-lib` JSF library within your WAR (JPA, CDI, BV, and JSF enabled) as follows:
```
<dependency>
    <groupId>be.e-contract.jsf-crud</groupId>
    <artifactId>jsf-crud-lib</artifactId>
    <version>0.6.0</version>
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

# Tutorial

Once you have your JPA entity defined, you can start with the most simple construct:
```
<crud:crud entity="YourEntity"/>
```
This gives you a table with basic create, read, update, and delete functionality.
The CRUD component uses reflection to construct a UI based on the JPA annotations.

You can change the displayed name of a field via:
```
<crud:crud entity="YourEntity">
    <crud:field name="yourField" label="A human readable label"/>
</crud:crud>
```

You can sort and filter on a field via:
```
<crud:crud entity="YourEntity">
    <crud:field name="yourField" sort="true" filter="true"/>
</crud:crud>
```

You can hide a field from the main table, but still have it displayed via a read operation as follows:
```
<crud:crud entity="YourEntity">
    <crud:field name="yourField" hide="true"/>
    <crud:read/>
</crud:crud>
```

If you don't want a certain CRUD functionality, you can disable it via:
```
<crud:crud entity="YourEntity">
    <crud:create disabled="true"/>
    <crud:update disabled="true"/>
    <crud:delete disabled="true"/>
</crud:crud>
```

If you don't want a certain field to be updated, you can hide it as follows:
```
<crud:crud entity="YourEntity">
    <crud:update>
        <crud:field name="yourField" hide="true"/>
    </crud:update>
</crud:crud>
```

Besides fields, you can also display properties via:
```
<crud:crud entity="YourEntity">
    <crud:property name="yourProperty"/>
</crud:crud>
```

Besides the CRUD operations, you can easily add custom actions via:
```
<crud:crud entity="YourEntity">
    <crud:action value="Your custom action" action="..." oncomplete="..." update="..."/>
</crud:crud>
```
The `action` method receives the selected entity as parameter.
The `crud:action` component can handle various return types. For example, if the `action` method returns the entity, it will get saved in the database.

If your custom action opens a dialog (via `oncomplete="PF('yourDialog').show()"`), you can access the currently selected entity within the dialog as follows:
```
<crud:entity var="entity">
    <h:outputText value="#{entity.yourField}"/>
</crud:entity>
```

Within your dialogs, you can save the selected entity via:
```
<crud:entity var="entity">
    ...
    <crud:saveButton value="Your Action" action="#{yourController.yourAction}"/>
</crud:entity>
```
Where the `action` method can make the entity to get saved within the database by simply returning it.