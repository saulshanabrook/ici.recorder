FROM openjdk:8-alpine
WORKDIR /tmp/
RUN wget http://apache.mirrors.hoobly.com/drill/drill-1.10.0/apache-drill-1.10.0.tar.gz
RUN tar -xvzf apache-drill-1.10.0.tar.gz
RUN apk add --update bash && rm -rf /var/cache/apk/*
WORKDIR apache-drill-1.10.0/bin
CMD ["/tmp/apache-drill-1.10.0/bin/drill-embedded"]
