###########################
## TEMPORARY BUILD IMAGE ##
###########################
FROM wguopensource/osmt-build:latest as build

# Copy in source code.
USER ${USER}
COPY --chown=${USER}:${USER} ./ ${BASE_DIR}/build/
WORKDIR ${BASE_DIR}/build

# The dockerfile-build Maven profile excludes certain api integration tests that require access to the Docker service.
RUN mvn clean install -P dockerfile-build

######################
## SPRING APP IMAGE ##
######################
FROM wguopensource/osmt-base:latest

COPY --from=build --chown=${USER}:${USER} ${BASE_DIR}/build/api/target/osmt-*.jar ${BASE_DIR}/bin/osmt.jar

ADD ./docker/ /${BASE_DIR}/

EXPOSE 8080

ENTRYPOINT ["/opt/osmt/bin/docker_entrypoint.sh"]
