FROM openjdk:8-jdk

# Criando e configurando usuário docker
RUN useradd -m docker && echo "docker:docker" | chpasswd \
  && adduser docker sudo \
  && adduser docker root \
  && adduser www-data docker

ARG KOTLIN_VERSION=1.2.61
	
ENV KOTLIN_HOME=/usr/local/kotlin \ 
	MAVEN_VERSION=3.5.4 \ 
	USER_HOME_DIR="/home/docker"

ENV home=/usr/local/kotlin

RUN apt-get update && \
    apt-get install -y wget unzip htop

RUN cd  /tmp && \
    wget https://github.com/JetBrains/kotlin/releases/download/v$KOTLIN_VERSION/kotlin-compiler-$KOTLIN_VERSION.zip  && \
    unzip kotlin-compiler-$KOTLIN_VERSION.zip && \
    mkdir -p "${KOTLIN_HOME}" && \
    mv "/tmp/kotlinc/bin" "/tmp/kotlinc/lib" "${KOTLIN_HOME}" && \
    rm ${KOTLIN_HOME}/bin/*.bat && \
    chmod +x ${KOTLIN_HOME}/bin/* && \
    ln -s "${KOTLIN_HOME}/bin/"* "/usr/bin/" && \
    rm -rf /tmp/* /var/cache/apt/*

RUN rm -f /var/cache/apt/*

WORKDIR /workspace/kotlin

#RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
#  && curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
#    | tar -xzC /usr/share/maven --strip-components=1 \
#  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
#
#ENV MAVEN_HOME /usr/share/maven
#ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
#
#COPY entrypointscript.sh /usr/local/bin/mvn-entrypoint.sh
#
#RUN mkdir $USER_HOME_DIR/.m2
#RUN chown docker:docker -R $USER_HOME_DIR
#RUN chown docker:docker -R $USER_HOME_DIR/.m2
#VOLUME "$USER_HOME_DIR/.m2"
#
#COPY . /usr/src/mymaven

#WORKDIR /usr/src/mymaven
#ENTRYPOINT ["sh", "/usr/local/bin/mvn-entrypoint.sh"]