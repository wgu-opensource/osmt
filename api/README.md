# OSMT API
This Maven module represents the Spring Boot API application. 

## Spring Boot Configuration and Profiles
OSMT configures Spring boot with property files. These are located at `./api/src/main/resources/config/`. It has 2 kinds of Spring profiles:
* Configuration Profiles - these contextualize an SDLC environment (i.e., the `dev` profile for local development). `dev` is the only profile that is fleshed out at this time.
* Component profiles - these active certain Spring Boot `@Component`(s). You can find them in `@Profile` annotations.

| Configuration Profile     | Properties file |
| -----------               | ----------- |
| (none)                    | application.properties |
| dev                       | application-dev.properties |
| staging                   | application-staging.properties |
| review                    | application-review.properties |
| test                      | application-test.properties |
| staging                   | application-staging.properties |

| Component Profile         | Note |
| ---                       | --- |
| apiserver                 | starts the API server |
| oauth2-okta               | includes required configuration for OAuth2 OIDC with Okta |
| noauth                    | bypasses API authentication |
| import                    | runs the batch import process, expects `--csv=` argument and `--import-type=` argument. | 
| reindex                   | runs the Elasticsearch re-index process |

## Running from the Command Line
See [Using the OSMT development utility](../README.md#using-the-osmt-development-utility-osmt_devsh) in the project [README.md](/../README.md) for using `osmt_dev.sh` to start and stop the Development Docker services and the Spring API application. `osmt_dev.sh` automatically sources the environment variables from `api/osmt-dev-stack.env`.

* You are not required to use the `osmt_dev.sh` utility. Many will prefer to run `mvn` and `java jar` commands against the jars in the api/target directory. Examples are given below. You will probably need to use a configuration profile (i.e., `dev`) and at least one component profile (i.e., `apiserver`). 
* To override specific properties with JVM arguments when developing with Maven, pass the JVM arguments as the value to `-Dspring-boot.run.jvmArguments=`
* Depending on the configuration you use, you may need to source the environment variables from `api/osmt-dev-stack.env`.
  * These command will help source an environment file into a shell session (`omst_dev.sh -s` does this for you automatically:
    ```
    set -o allexport; source api/osmt-dev-stack.env; set +o allexport;
    ``` 

Examples:
* Using Maven to start the API server overriding the `spring.flyway.enabled` property: 
    ```
      mvn -Dspring-boot.run.profiles=dev,apiserver,oauth2-okta \
          -Dspring-boot.run.jvmArguments="-Dspring.flyway.enabled=false" \
          spring-boot:run
    ```
* Using Java to import BLS metadata
    ```
      java -jar -Dspring.profiles.active=dev,import \
          api/target/osmt-api-<version>.jar --csv=path/to/csv --import-type=bls  
    ```

## OAuth2 
An example profile and Spring Boot components (edu.wgu.osmt.security.SecurityConfig) are provided to support OAuth2 with Okta. To use a different provider, create a separate Spring Boot profile to contain the configuration. Additional Spring Boot components may also be required to support the chosen provider. See [Okta Configuration](../README.md#oauth2-and-okta-configuration) in the project [README](../README.md) for more details.

## Database Configurations
This project uses [FlywayDb](https://flywaydb.org/). SQL Migrations can be placed in `./api/src/main/resources/db/migration/`.
Scripts in this folder will be automatically processed when the app is started with the appropriate `application.properties` settings in `spring.flyway.*`.

By default, only `test` and `dev` configuration profiles will automatically run migrations. To enable migrations for other environments, i.e. a single production server, start the server with this JVM argument: `-Dspring.flyway.enabled=true`. 

## Allowing anonymous API to search and list endpoints for published skills and collections
This feature is enabled by default. These settings are in application.properties
* `app.allowPublicSearching=true`
* `app.allowPublicLists=true`

## Caching and Rate Limiting with bucket4j
OSMT uses Bucket4j for caching and rate limiting on the API. You can learn more about this [here](https://www.baeldung.com/spring-bucket4j). These features are enabled by including "bucket4j" as a run profile, which pull in [./src/main/resources/config/application-bucket4j.properties](./src/main/resources/config/application-bucket4j.properties). All of these properties can be adjusted at runtime as Spring application properties.

## Importing Data
You can use the import component profile to import RSDs, and BLS/O*NET job code metadata. If running from the command line, your active Spring profiles will look like this:
```-Dspring.profiles.active=dev,import```

Imports via the command line require these 2 arguments:

| Command Line Arguments    | Values                     |
| -----------               | -----------                |
| --import-type             | RSDs - `batchskill`<br>BLS - `bls`<br>O*NET - `onet`| 
| --csv                     | a valid path to a CSV file |

So command will look like this:
```
  java -jar -Dspring.profiles.active=dev,import \
      api/target/osmt-api-<version>.jar \
      --csv=path/to/csv --import-type=batchskill  
```
or
```
  mvn -Dspring-boot.run.profiles=dev,import \
      -Dspring-boot.run.arguments="--import-type=bls,--csv=path/to/csv" \
      spring-boot:run
```

Please see the `import` folder in the project root for sample files. The general import sequence should be:
1. RSDs (```--import-type=batchskill```)
2. BLS (```--import-type=bls```)
3. O*NET (```--import-type=onet```)
4. Reindex Elasticsearch

### Importing RSDs


### Importing BLS codes:
BLS codes will not be duplicated if imported multiple times
* Note - BLS codes should be imported before O*NET codes
1. Download BLS codes in Excel format from [https://www.bls.gov/soc/2018/#materials]("https://www.bls.gov/soc/2018/#materials")
2. Remove the content before header row. 
3. Convert Excel to CSV format
4. Import the CSV with the following command:
    ```
    java -jar -Dspring.profiles.active=dev,import api/target/osmt-api-<version>.jar --csv=path/to/bls_csv --import-type=bls    
    ```

### Importing O*NET
O*NET codes will not be duplicated if imported multiple times
* Note - BLS codes should be imported before O*NET codes
1. Download O*NET `Occupation Data` in Excel format from [https://www.onetcenter.org/database.html#occ]("https://www.onetcenter.org/database.html#occ")
2. Convert Excel to CSV format
3. Import the CSV with the following command:
    ```
    java -jar -Dspring.profiles.active=dev,import api/target/osmt-api-<version>.jar --csv=path/to/onet_csv --import-type=onet    
    ```

## Elasticsearch indexing
After importing, it is necessary to run Spring Boot with the `reindex` profile to generate Elasticsearch index mappings and documents. The `<environment profile>` in the following examples can be `dev`,`review` or omitted for production. 

Via the compiled jar:
```
java -Dspring.profiles.active=<environment profile>,reindex -jar api/target/osmt-api-<version>.jar 
``` 

Via mvn:
```
mvn -DSpring.profiles.active=<environment profile>,reindex
```

### Reindex after changes to Elasticsearch `@Document` index classes
If changes are made to @Document annotated classes, the indexes need to be deleted and re-indexed. 
* Delete all of the indices in Elasticsearch by executing the following:
    ```
    curl -X DELETE "http://<elasticsearch host>:<elasticsearch port>/*" 
    ``` 
    Where `host` and `port` represent the Elasticsearch server you are targeting, e.g. `localhost:9200` 
* Run the reindex command from above to generate the new mappings and documents
