################
## BASE IMAGE ##
################
FROM centos:centos8.3.2011 as osmt-base

LABEL Maintainer="WGU / OSN"
LABEL Version="1.0"

ARG OSMT_WHITELABEL_URL
ENV OSMT_WHITELABEL_URL=${OSMT_WHITELABEL_URL}

ENV JAVA_HOME=/etc/alternatives/jre
ENV USER=osmt
ENV BASE_DIR=/opt/${USER}

# Do some housekeeping for yum
RUN rm /var/lib/rpm/.rpm.lock
RUN rm /var/lib/rpm/.dbenv.lock
RUN /usr/bin/yum clean all
RUN /usr/bin/yum update -y

# Install EPEL / useful packages
RUN /usr/bin/yum install -y epel-release
RUN /usr/bin/yum remove -y java-1.8.0-openjdk*
RUN /usr/bin/yum install -y curl java-11-openjdk wget

# Add in configuration files
ADD ./docker/etc /etc

# Set a DNS lookup TTL to 10 seconds
RUN sed -i 's/#networkaddress.cache.ttl=-1/networkaddress.cache.ttl=10/' ${JAVA_HOME}/conf/security/java.security

# Create user
RUN /usr/sbin/useradd -r -d ${BASE_DIR} -s /bin/bash ${USER} -k /etc/skel -m -U \
    && mkdir -p ${BASE_DIR}/bin ${BASE_DIR}/build ${BASE_DIR}/logs ${BASE_DIR}/etc \
    && chown -R ${USER}:${USER} ${BASE_DIR}

###########################
## BUILD / COMPILE IMAGE ##
###########################
FROM osmt-base as build

ENV M2_VERSION=3.8.3
ENV M2_HOME=/usr/local/maven
ENV PATH=${M2_HOME}/bin:${PATH}

# Download / Install Maven
ADD https://dlcdn.apache.org/maven/maven-3/${M2_VERSION}/binaries/apache-maven-${M2_VERSION}-bin.tar.gz /usr/share/src/

WORKDIR /usr/share/src

RUN tar -xf apache-maven-${M2_VERSION}-bin.tar.gz \
    && mv apache-maven-${M2_VERSION} ${M2_HOME}/

# Copy in source code.
COPY --chown=${USER}:${USER} ./ ${BASE_DIR}/build/

WORKDIR ${BASE_DIR}/build

USER ${USER}

RUN echo "$(env|sort)"
# The dockerfile-build Maven profile excludes certain API integration tests that require access to the Docker service.
RUN mvn clean install -P dockerfile-build

######################
### PACKAGING IMAGE ##
######################
FROM osmt-base as app

COPY --from=build --chown=${USER}:${USER} ${BASE_DIR}/build/api/target/osmt-*.jar ${BASE_DIR}/bin/osmt.jar

ADD ./docker/ /${BASE_DIR}/

EXPOSE 8080

ENTRYPOINT ["/opt/osmt/bin/docker_entrypoint.sh"]
