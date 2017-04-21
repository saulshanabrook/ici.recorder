FROM openjdk:7-jdk-alpine

RUN apk add --update bash \
  &&  rm -rf /var/cache/apk/*

ARG VERSION=2.7.3
RUN wget -q http://www-us.apache.org/dist/hadoop/common/hadoop-${VERSION}/hadoop-${VERSION}.tar.gz \
 && tar -zxf hadoop-${VERSION}.tar.gz \
 && rm hadoop-${VERSION}.tar.gz

# ssh install modified from
# https://github.com/macropin/docker-sshd/blob/bfac17f1ca2c80ec3a61248bcdf6d62e4d95a7a7/Dockerfile#L5-L12
RUN   apk add --update openssh rsync bash wget \
  &&  rm -rf /var/cache/apk/*
# RUN mkdir -p ~/.ssh
# RUN chmod 700 ~/.ssh/
# RUN ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
# RUN ssh-keygen -t rsa -P '' -f /etc/ssh/ssh_host_rsa_key
# RUN cp ~/.ssh/id_rsa.pub ~/.ssh/authorized_keys
#
#


# RUN apk add --no-cache openssl
#
# ENV DOCKERIZE_VERSION v0.4.0
# RUN wget --no-verbose https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
#     && tar -C /usr/local/bin -xzvf dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
#     && rm dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz
#
# RUN /usr/sbin/sshd
# RUN dockerize -wait tcp://localhost:21 ssh-keyscan -H localhost,0.0.0.0 >> ~/.ssh/known_hosts
# COPY entrypoint.sh /entrypoint.sh
# ENTRYPOINT ["/entrypoint.sh"]

WORKDIR hadoop-${VERSION}
COPY core-site.xml hdfs-site.xml etc/hadoop/

# CMD ["dockerize", "sbin/start-dfs.sh"]

ENTRYPOINT ["./bin/hdfs"]
#
# RUN mvn -q --batch-mode -T 4C install package -Dspark -Dhadoop.version=2.3.0 \
# -Dmaven.javadoc.skip=true -DskipTests -Dlicense.skip=true \
# -Dcheckstyle.skip=true -Dfindbugs.skip=true
# RUN tar --exclude .git --exclude logs --exclude dev -czf /tmp/alluxio.tgz .
# CMD ["cat", "/tmp/alluxio.tgz"]
