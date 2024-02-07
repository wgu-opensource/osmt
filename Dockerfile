######################
## SPRING APP IMAGE ##
######################
FROM wguopensource/osmt-base:latest

ARG MVN_POM_VERSION

COPY --chown=${USER}:${USER} ./import ${BASE_DIR}/import/
RUN curl --output ${BASE_DIR}/bin/osmt.jar https://repo1.maven.org/maven2/edu/wgu/osmt/osmt-api/${MVN_POM_VERSION}/osmt-api-${MVN_POM_VERSION}.jar
RUN chown ${USER}:${USER} ${BASE_DIR}/bin/osmt.jar


ADD ./docker/ /${BASE_DIR}/

EXPOSE 8080

ENTRYPOINT ["/opt/osmt/bin/docker_entrypoint.sh"]
