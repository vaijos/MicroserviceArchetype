# Microservice Archetype

## Installing Microservice Archetype

Follow these steps to install the archetype in your local maven repository:

1. If you don't have Maven, install Maven.
2. Open a terminal and clone this repository.
3. Change to the directory of the repository and execute the command `$ mvn install`.

## Executing archetype to start new Microservice
1. Be sure to have the latest version of the Maven Archetype. Go to your local repository of the Maven Archetype and execute `$ git fetch` and `$ git pull` to update the repository. If it updated then execute `$ mvn clean install`
2. Open a terminal and change to the directory in which you will create the new microservice project.
3. Run the following command: `$ mvn archetype:generate -DarchetypeArtifactId=maven-archetype-microservice -DarchetypeGroupId=com.membership.archetype -DarchetypeVersion=1.0`
4. The archetype will as for the following values, provide the values for the parameters:
  - groupId = Path where the files and directories will be created. Path is delimited by dots. (example: com.membership.authzs)
  - artifactId = Name of the project, this will be name of the directory in which the project will be craeted (example:Gatekeeper)
  - version = Version of the project (Default = 1.0-SNAPSHOT)
  - package = Package in which the files will be contained (Default = value of groupId; The default value is the recommended value)
  - route-class = Name for the Java class that will contain the HTTP routes (example: AuthzController)

## Creating a new Microservice with GitHub repository example
*Note: If you want to only want to create a new microservice without a GitHub repository follow the steps from __Executing archetype to start new Microservice__*

 1. Create a new repository in Github. For this example we will name the repository **NewMicro**.
 2. Be sure to have the latest version of the Maven Archetype. Go to your local repository of the Maven Archetype and execute `$ git fetch` and `$ git pull` to update the repository. If it updated then execute `$ mvn clean install`
 3. Open a terminal and go to the directory in which you will create the new microservice. *Note: It is recommended that it is the same directory in which you have your other GitHub local repositories.*
 4. Execute `$ mvn archetype:generate -DarchetypeArtifactId=maven-archetype-microservice -DarchetypeGroupId=com.membership.archetype -DarchetypeVersion=1.0` and provided the parameters required. For this example we will use the following values:
   - groupId = com.membership.newmicro
   - artifactId = NewMicro
   - version = 1.0
   - package = com.membership.newmicro
   - route-class = MyRoutes
 5. Once the project is built successfully move into its directory and execute `$ git init`. Then execute `$ git add .` to add all files and execute `$ git commit -m "First commit"`
 6. Go to GitHub and copy the URL used to clone the repository. For this example the URL of our repository is `https://github.dowjones.net/djin-platform-membership/NewMicro.git`
 7. Execute `$ git remote add origin <URL>`. For this example we execute `$ git remote add origin https://github.dowjones.net/djin-platform-membership/NewMicro.git`. You can check that the repository was set correctly with `$ git remote -v`
 8. Execute `$ git push -u origin master` to push the new microservice into the GitHub repository.

## Structure of Microservice

If you run the archetype with the following parameters you will get the following structure:
 - groupId = com.membership.newmicro
 - artifactId = NewMicro
 - version = 1.0
 - package = com.membership.newmicro
 - route-class = MyRoutes

```
Newmicro
|-- pom.xml
|-- Dockerfile
|-- scripts
    |-- splunk_monitor.sh
    |-- start.sh
    |-- stop.sh
|-- config
    |-- deploymentclient.conf
    |-- dev
        |-- config.properties
        |-- djca_truststore.jks
        |-- error_mappings.dat
        |-- logback.xml
        |-- newrelic.yml
        |-- spark.jks
        |-- swagger.json
    |-- int
        |-- (same content as dev)
    |-- prod
        |-- (same content as dev)
    |-- stag
        |-- (same content as dev)
|-- src
    |-- main
        |-- java
            |-- com
                |-- membership
                    |-- newmicro
                        |-- App.java
                        |-- ErrorMapper.java
                        |-- controller
                            |-- MyRoutes.java
                        |-- model
                            |-- ErrorMsg.java
                        |-- service
                            |-- HttpMessagejava
                        |-- util
                            |-- JsonUtil.java
                            |-- ResponseError.java
        |-- resources
            |-- public
                |-- swagger-ui
                    |-- (files required for swagger ui)
    |-- test
        |-- java
            |-- com
                |-- membership
                    |-- newmicro
                        |-- AppTest.java
```
