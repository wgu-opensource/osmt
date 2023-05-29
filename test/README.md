# OSMT API TESTING
***

The OSMT Integration test folder will run API tests against the openapi.yaml file. \
it is important that you keep the openapi.yaml up to date.

## Getting Started

The automated tests uses a combination of bash scripts, newman, docker and Maven. Maven will \
call the tests during any build cycle. You can also call it manually using \
`mvn verify -pl test -Prun-api-tests`. Before getting started there are a few \
prerequisites listed below.

### prerequisites

- Docker
- Newman

Furthermore, you will need to create a `osmt-apitest.env` file under the test folder. There is \
currently an `osmt-apitest.env.example` file you can use as the template. You will need to add \
your Okta credentials for authentication.


### Caveats

In the `osmt-apitest.env` file make sure you escape any special characters or else it will not \
parse correctly when passing the environment variable to newman
