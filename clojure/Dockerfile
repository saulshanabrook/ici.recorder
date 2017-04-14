FROM clojure:lein

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY project.clj alluxio.jar /usr/src/app/
RUN env LOCALREPO_USERNAME= LOCALREPO_PASSWORD= lein deploy localrepo1 org.alluxio/alluxio-core-client dev alluxio.jar
RUN lein deps

COPY . /usr/src/app
ENTRYPOINT ["lein"]
CMD ["test"]

# ENV TIMBRE_LEVEL :error
