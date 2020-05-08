JSF CRUD Project
================

The JSF CRUD Project delivers a JSF component, based on PrimeFaces, to perform simple CRUD operations on JPA entities.
The JSF CRUD component is a perfect match for the construction of administrator portals where you basically want simple and fast CRUD operations on the JPA entities within your Java EE application.

The component has been tested on the following application servers:
* JBoss EAP 6.4.22
* JBoss EAP 7.3.0
* WildFly 19.0.0
* Payara 5.201
* Oracle WebLogic Server 14.1.1
* Apache TomEE 8.0.1
* Open Liberty 20.0.0.5

The component has been tested with PrimeFaces versions:
* 6.2.27
* 7.0
* 7.0.13
* 8.0
* 8.0.1

# Usage

If you are using Maven, refer to the e-contract.be Maven repository via:
```xml
<repository>
    <id>e-contract</id>
    <url>https://www.e-contract.be/maven2/</url>
    <releases>
        <enabled>true</enabled>
    </releases>
</repository>
```
Include the `jsf-crud-lib` JSF library within your WAR (JPA, CDI, BV, and JSF enabled) as follows:
```xml
<dependency>
    <groupId>be.e-contract.jsf-crud</groupId>
    <artifactId>jsf-crud-lib</artifactId>
    <version>0.9.1</version>
</dependency>
```

Within a JSF page you can now add the following:
```xml
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
./standalone.sh
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
mvn clean install
```

Deploy the demo web application to the local running Payara via:
```
./asadmin deploy ~/jsf-crud/jsf-crud-demo/target/jsf-crud-demo-x.y.z-SNAPSHOT.war
```

Navigate your web browser to:
http://localhost:8080/jsf-crud-demo/

Undeploy the demo web application via:
```
./asadmin undeploy jsf-crud-demo-x.y.z-SNAPSHOT
```

List all deployed applications via:
```
./asadmin list-applications
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

This tutorial gives you an overview of the most important features of the JSF CRUD component.

Once you have your JPA entity defined, you can start with the most simple construct:
```xml
<crud:crud entity="YourEntity"/>
```
This gives you a table with basic create, read, update, and delete functionality.
The JSF CRUD component uses reflection to construct a UI based on the JPA annotations.

You can change the displayed name of a field via:
```xml
<crud:crud entity="YourEntity">
    <crud:field name="yourField" label="A human readable label"/>
</crud:crud>
```

Change the size of the field input component via:
```xml
<crud:crud entity="YourEntity">
    <crud:field name="yourField" size="60"/>
</crud:crud>
```

You can sort and filter on a field within the main table via:
```xml
<crud:crud entity="YourEntity">
    <crud:field name="yourField" sort="true" filter="true"/>
</crud:crud>
```

You can hide a field from the main table, but still have it displayed via a read operation as follows:
```xml
<crud:crud entity="YourEntity">
    <crud:field name="yourField" hide="true"/>
    <crud:read/>
</crud:crud>
```

You can change the order of the fields within the main table using:
```xml
<crud:crud entity="YourEntity">
    <crud:order>
        <crud:field name="firstField"/>
        <crud:field name="secondField"/>
        ...
    </crud:order>
</crud:crud>
```

If you don't want a certain CRUD functionality, you can disable it via:
```xml
<crud:crud entity="YourEntity">
    <crud:create disabled="true"/>
    <crud:update disabled="true"/>
    <crud:delete disabled="true"/>
</crud:crud>
```

If you don't want a certain field to be visible within the update dialog, you can hide it as follows:
```xml
<crud:crud entity="YourEntity">
    <crud:update>
        <crud:field name="yourField" hide="true"/>
    </crud:update>
</crud:crud>
```

Mark a field as a password field via:
```xml
<crud:crud entity="YourEntity">
    <crud:field name="yourField">
        <crud:password feedback="true" match="true"/>
    </crud:field>
</crud:crud>
```

Mark a field as a binary field (with upload/download functionality) via:
```xml
<crud:crud entity="YourEntity">
    <crud:field name="yourField">
        <crud:binary contentType="text/plain"/>
    </crud:field>
</crud:crud>
```

Besides fields, you can also display properties via:
```xml
<crud:crud entity="YourEntity">
    <crud:property name="yourProperty"/>
</crud:crud>
```

You can also change the default input component used for a field.
For example, to change the input component of a field on the update dialog, you can define:
```xml
<crud:crud entity="YourEntity">
    <crud:update>
        <crud:field name="yourField">
            <f:facet name="input">
                <p:spinner/>
            </f:facet>
        </crud:field>
    </crud:update>
</crud:crud>
```

## Custom Actions

Besides the CRUD operations, you can easily add custom actions via:
```xml
<crud:crud entity="YourEntity">
    <crud:action value="Your custom action" action="..." oncomplete="..." update="..."/>
</crud:crud>
```
The `action` method expression receives the selected entity as parameter.
The `crud:action` component can handle various return types. For example, if the `action` method returns the entity, it will get saved in the database.

If your custom action opens a dialog (via `oncomplete="PF('yourDialog').show()"`), you can access the currently selected entity within the dialog as follows:
```xml
<crud:entity var="entity">
    <h:outputText value="#{entity.yourField}"/>
</crud:entity>
```

Within your dialogs, you can save the selected entity via:
```xml
<crud:entity var="entity">
    ...
    <crud:saveButton value="Your Action" action="#{yourController.yourAction}"/>
</crud:entity>
```
Where the `action` method expression can make the entity to get saved within the database by simply returning it.

To mark an action as a download, you use:
```xml
<crud:crud entity="YourEntity">
    <crud:action value="Download something">
        <crud:fileDownload value=#{yourController.yourMethodWithEntityParameterAndStreamedContentReturnType}>
    </crud:action>
</crud:crud>
```

You can even let the JSF CRUD component create a dialog for your custom action itself via:
```xml
<crud:crud entity="YourEntity">
    <crud:action value="Custom Action">
        <f:facet name="dialog">
            <h:form>
                <crud:entity var="entity">
                    ...
                </crud:entity>
                ...
            </h:form>
        </f:facet>
    </crud:action>
</crud:crud>
```

Besides custom row actions, you can also define custom global actions that will be visible within the main table footer as follows:
```xml
<crud:crud entity="YourEntity">
    <crud:globalAction value="Your custom global action" action="..." oncomplete="..." update="..."/>
</crud:crud>
```

Define a global download action as follows:
```xml
<crud:crud entity="YourEntity">
    <crud:globalAction value="Download something">
        <crud:fileDownload value=#{yourController.yourMethodWithStreamedContentReturnType}>
    </crud:globalAction>
</crud:crud>
```

## Events

You can define listeners for different CRUD events:
```xml
<crud:crud entity="YourEntity">
    <crud:createListener action="#{yourController.createdEventHandler}"/>
    <crud:updateListener action="#{yourController.updatedEventHandler}"/>
    <crud:deleteListener action="#{yourController.deletedEventHandler}"/>
</crud:crud>
```
where you have the following event listeners defined in you backing bean:
```java
public void createdEventHandler(CreateEvent createEvent) {
    // do something after the entity has been created
}

public void updatedEventHandler(UpdateEvent updateEvent) {
    // do something after the entity has been updated
}

public void deletedEventHandler(DeleteEvent deleteEvent) {
    // do something after the entity has been deleted
}
```

The JSF CRUD component also fires CDI events, which you can observe as follows:
```java
public void handlePreCreateEvent(@Observes @HandlesEntity(YourEntity.class) PreCreateEvent preCreateEvent) {
    // do something before the entity is created
}

public void handlePreUpdateEvent(@Observes @HandlesEntity(YourEntity.class) PreUpdateEvent preUpdateEvent) {
    // do something before the entity is updated
}

public void handlePreDeleteEvent(@Observes @HandlesEntity(YourEntity.class) PreDeleteEvent preDeleteEvent) {
    // do something before the entity is deleted
}
```

## Security

Because the JSF CRUD component directly performs JPA operations, we might lose the RBAC security features normally provided by the CDI/EJB backing beans. To compensate for this, the JSF CRUD component can be directed to perform RBAC security verifications as follows:
```xml
<crud:crud entity="YourEntity" roleAllowed="administrator"/>
```

This can even be configured globally via the following `web.xml` context parameter:
```xml
<context-param>
    <param-name>crud.roleAllowed</param-name>
    <param-value>administrator</param-value>
</context-param>
```

## Custom Query

Per default the JSF CRUD component will display all your entities within the main table.
You can customize this main query via:
```xml
<crud:crud entity="YourEntity">
    <crud:query query="SELECT e FROM YourEntity AS e WHERE e.yourField = :yourParam">
        <crud:queryParameter name="yourParam" value="#{...}"/>
    </crud:query>
</crud:crud>
```