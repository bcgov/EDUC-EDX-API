# EDUC-PEN-REQUEST-API

## Pre-build Setup

To run this API locally, you will need to install the Oracle Java Database Connecter (specifically ojdbc 8). Steps on how to do this are located [here](https://www.mkyong.com/maven/how-to-add-oracle-jdbc-driver-in-your-maven-local-repository/).

## Build Setup

``` bash
#Run application with local properties
mvn clean install -Dspring.profiles.active=dev

#Run application with default properties
mvn clean install
