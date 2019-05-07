[![][logo]][website] 

# About

The plugin `blueriq-plugin-instanceselector-plus` used to be a part of the Blueriq Runtime but since the release of Blueriq 11, the support of this plugin is discontinued. 
Blueriq released the sources of this plugin with the intention of enabling customers to migrate to the AQ_InstanceSelectorPlus container at their own pace but without support from Blueriq. No rights reserved.

# Build from source

To compile and build war use:

```bash
mvn clean verify -DskipTests
```

To test the war, please add the Blueriq `license.aql` to `src\test\resources` and use:

```bash
mvn clean verify
```

# Run example

Deploy `Runtime.war` to Tomcat container. Create a configuration folder and add Blueriq `license.aql` or package Blueriq `license.aql` by adding it to `src\main\resources`.
Start Tomcat container with the following parameters:

```bash
-Dspring.config.additional-location=file://path_to_conf/ # URI of the configuration folder which contains the Blueriq license.
-Dspring.profiles.active=native,development-tools
```

# Studio container

| Name | Description | Type | Required |
|---|---|---|---|
| referrer-path | Attribute/relation path to determine which instances to show, starting from a particular entity. | String | false |
| entity | Name of the entity of which the instances will be shown. | String | true |
| noinstancecontainer | Container that will be shown when there are no instances to show. | String | false |
| addbuttons | deprecated: does not work anymore. You should model these buttons outside this container. | String | false |
| addbuttonevents | deprecated: does not work anymore. You should model these events on buttons outside this container. | String | false |
| headercontainer | Container of the header. | String | false |
| whereclause | Expression to determine which instances to show. Only to be used when the container is accessed through a relation. | String | false |
| sortattribute | Attribute that is used to sort the instances. | String | false |
| sortorder | The order in which the list will be sorted. | “ascending” or “descending” | false |
| directcreate | deprecated: does not work anymore. You should create your own instance, using the CreateInstance service. | Boolean | false |
| directdelete | deprecated: does not work anymore. You should delete your own instance, using the DeleteInstance service. | Boolean | false |

[logo]: https://www.blueriq.com/Static/images/logo_gradient.svg
[website]: http://www.blueriq.com
