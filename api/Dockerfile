FROM oraclelinux:9 AS base

ENV USER=osmt
ENV BASE_DIR=/opt/${USER}
ENV JAVA_HOME=/etc/alternatives/jre

# Install EPEL / Useful packages /
RUN /usr/bin/yum install -y epel-release \
    && /usr/bin/yum update -y \
    && /usr/bin/yum remove -y java-1.8.0-openjdk* \
    && /usr/bin/yum install -y curl java-21-openjdk

# Add in configuration files
ADD ./api/docker/etc /etc

# Set a DNS lookup TTL to 10 seconds
RUN sed -i 's/#networkaddress.cache.ttl=-1/networkaddress.cache.ttl=10/' ${JAVA_HOME}/conf/security/java.security

# Create the osmt non-root user
RUN /usr/sbin/useradd -r -d ${BASE_DIR} -s /bin/bash ${USER} -k /etc/skel -m -U \
    && mkdir -p ${BASE_DIR}/bin ${BASE_DIR}/build ${BASE_DIR}/logs ${BASE_DIR}/etc \
    && chown -R ${USER}:${USER} ${BASE_DIR}


FROM base AS build

ENV USER=osmt
ENV BASE_DIR=/opt/${USER}
ENV JAVA_HOME=/etc/alternatives/jre

# Set Maven environment variables
ENV M2_VERSION=3.9.6
ENV M2_HOME=/usr/local/maven
ENV PATH=${M2_HOME}/bin:${PATH}

# Install compile-time extra packages
RUN /usr/bin/yum install -y java-21-openjdk-devel wget make gcc g++ chromium

# Download / Install Maven
ADD https://dlcdn.apache.org/maven/maven-3/${M2_VERSION}/binaries/apache-maven-${M2_VERSION}-bin.tar.gz /usr/share/src/

WORKDIR /usr/share/src

RUN tar -xf apache-maven-${M2_VERSION}-bin.tar.gz \
    && mv apache-maven-${M2_VERSION} ${M2_HOME}/

COPY . ${BASE_DIR}

# Descend into the API directory
WORKDIR ${BASE_DIR}/api

COPY --from=osmt-ui:latest --chown=${USER}:${USER} /usr/share/nginx/html/* ./src/main/resources/ui/

# Compile the API application
RUN --mount=type=cache,id=mvncache,target=/root/.m2 \
    mvn -B -P dockerfile-build -DskipTests package

FROM build AS test

# Set Maven and Test environment variables
ENV M2_HOME=/usr/local/maven
ENV PATH=${M2_HOME}/bin:${PATH}
ENV CHROME_BIN=/usr/bin/chromium-browser

# Test the API application
RUN --mount=type=cache,id=mvncache,target=${BASE_DIR}/.m2 \
    mvn -B -P dockerfile-build test

FROM base AS deploy

LABEL Maintainer="OSMT Contributors"

ENV USER=osmt
ENV BASE_DIR=/opt/${USER}

# Become the osmt user
USER ${USER}

COPY --from=build --chown=${USER}:${USER} ${BASE_DIR}/api/target/*.jar ${BASE_DIR}/bin/app.jar
