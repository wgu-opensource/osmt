################
## BASE IMAGE ##
################
FROM centos:centos8.2.2004 as osmt-base

LABEL Maintainer="Francisco Gray, <fgray@concentricsky.com>"
LABEL Version="1.0"

ENV JAVA_VERSION=11.0.8.10
ENV JAVA_HOME=/etc/alternatives/jre
ENV USER=osmt
ENV BASE_DIR=/opt/${USER}

# Install EPEL / Useful packages /
RUN /usr/bin/yum install -y https://dl.fedoraproject.org/pub/epel/8/Everything/x86_64/Packages/e/epel-release-8-8.el8.noarch.rpm \
    && /usr/bin/yum update -y \
    && /usr/bin/yum remove -y java-1.8.0-openjdk* \
    && /usr/bin/yum install -y curl java-11-openjdk-headless-${JAVA_VERSION} wget

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

ENV JAVA_HOME=/etc/alternatives/jre
ENV JAVA_VERSION=11.0.8.10
ENV M2_VERSION=3.6.3
ENV M2_HOME=/usr/local/maven
ENV PATH=${M2_HOME}/bin:${PATH}
ENV USER=osmt
ENV BASE_DIR=/opt/${USER}

# Install OpenJDK Development Packages
RUN /usr/bin/yum install -y java-11-openjdk-devel-${JAVA_VERSION}

# Download / Install Maven
ADD https://www-eu.apache.org/dist/maven/maven-3/${M2_VERSION}/binaries/apache-maven-${M2_VERSION}-bin.tar.gz /usr/share/src/

WORKDIR /usr/share/src

RUN tar -xf apache-maven-${M2_VERSION}-bin.tar.gz \
    && mv apache-maven-${M2_VERSION} ${M2_HOME}/

# Copy in source code.
COPY --chown=${USER}:${USER} ./ ${BASE_DIR}/build/

WORKDIR ${BASE_DIR}/build/api

USER ${USER}

RUN mvn clean package -Dmaven.test.skip.exec

######################
### PACKAGING IMAGE ##
######################
FROM osmt-base

ENV JAVA_HOME=/etc/alternatives/jre
ENV JAVA_VERSION=11.0.8.10
ENV USER=osmt
ENV BASE_DIR=/opt/${USER}

COPY --from=build --chown=${USER}:${USER} ${BASE_DIR}/build/api/target/osmt-*.jar ${BASE_DIR}/bin/osmt.jar

ADD ./docker/ /${BASE_DIR}/

EXPOSE 8080

ENTRYPOINT ["/opt/osmt/bin/docker_entrypoint.sh"]
