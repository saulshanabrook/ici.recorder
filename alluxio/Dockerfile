FROM openjdk:8-alpine

RUN apk add --update bash && rm -rf /var/cache/apk/*

WORKDIR /tmp/
RUN wget http://alluxio.org/downloads/files/1.4.0/alluxio-1.4.0-bin.tar.gz && \
  tar -xzf alluxio-1.4.0-bin.tar.gz && \
  rm alluxio-1.4.0-bin.tar.gz
WORKDIR alluxio-1.4.0

RUN apk add --update openssh && rm -rf /var/cache/apk/*
# RUN sed -i -e 's@^AuthorizedKeysFile.*@@g' /etc/ssh/sshd_config
RUN ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key
RUN     mkdir -p ~root/.ssh /etc/authorized_keys && chmod 700 ~root/.ssh/ && \
    sed -i -e 's@^AuthorizedKeysFile.*@@g' /etc/ssh/sshd_config  && \
    echo -e "AuthorizedKeysFile\t.ssh/authorized_keys /etc/authorized_keys/%u" >> /etc/ssh/sshd_config && \
    echo -e "Port 22\n" >> /etc/ssh/sshd_config && \
    cp -a /etc/ssh /etc/ssh.cache
RUN /usr/sbin/sshd


COPY alluxio-site.properties /tmp/alluxio-1.4.0/conf/alluxio-site.properties
CMD ./bin/alluxio-start.sh -N local NoMount -f && tail -F logs/*

EXPOSE 19999 19998 30000
