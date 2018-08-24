FROM openjdk:8-jdk

# Criando e configurando usuário docker
RUN useradd -m docker && echo "docker:docker" | chpasswd \
  && adduser docker sudo \
  && adduser docker root \
  && adduser www-data docker

ARG version=0.3.0
ARG hadoop=hadoop2

RUN apt-get update && \
    apt-get install -y wget unzip htop && \
    mkdir /workspace && \
    cd /workspace && \
    wget https://github.com/JanusGraph/janusgraph/releases/download/v$version/janusgraph-$version-$hadoop.zip && \
    unzip janusgraph-$version-$hadoop.zip && \
    rm janusgraph-$version-$hadoop.zip && \
    mv janusgraph-* janusgraph

COPY config/run.sh /workspace/janusgraph
COPY config/gremlin-server.yaml /workspace/janusgraph/conf/gremlin-server/gremlin-server.yaml
COPY config/janusgraph.properties /workspace/janusgraph/janusgraph.properties
COPY config/janusgraph-cassandra-es.properties /workspace/janusgraph/conf/janusgraph-cassandra-es.properties
COPY config/janusgraph-cql-es.properties /workspace/janusgraph/conf/janusgraph-cql-es.properties
COPY config/empty-sample.groovy /workspace/janusgraph/scripts/empty-sample.groovy
COPY config/JanusGraphSchemaImporter.groovy /workspace/janusgraph/scripts/JanusGraphSchemaImporter.groovy

WORKDIR /workspace/janusgraph
RUN bin/gremlin-server.sh -i org.apache.tinkerpop gremlin-python 3.2.6

COPY janusgraph/janusgraph-schema /workspace/janusgraph/schema
CMD ["/bin/bash", "-e", "/workspace/janusgraph/run.sh"]
