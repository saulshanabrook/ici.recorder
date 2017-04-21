FROM openjdk:7-jdk-alpine

RUN apk add --update bash \
  &&  rm -rf /var/cache/apk/*

ARG VERSION=2.7.3
RUN wget -q http://www-us.apache.org/dist/hadoop/common/hadoop-${VERSION}/hadoop-${VERSION}.tar.gz \
 && tar -zxf hadoop-${VERSION}.tar.gz \
 && rm hadoop-${VERSION}.tar.gz

WORKDIR hadoop-${VERSION}
COPY core-site.xml hdfs-site.xml etc/hadoop/
ENTRYPOINT ["./bin/hdfs"]
