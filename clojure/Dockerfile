FROM clojure:lein

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

RUN apt-get update && apt-get install -y maven
COPY alluxio /tmp/alluxio
RUN cd /tmp/alluxio && mvn install -Dhadoop.version=2.2.0 -DskipTests

COPY project.clj /usr/src/app/
RUN lein deps

COPY . /usr/src/app
ENTRYPOINT ["lein"]
CMD ["test"]

# ENV TIMBRE_LEVEL :error
