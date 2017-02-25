FROM jupyter/datascience-notebook

# from https://github.com/jupyter/docker-stacks/blob/a090558811da100473948be3cb89e0cb8cbc79b9/pyspark-notebook/Dockerfile#L7-L25
USER root

# Temporarily add jessie backports to get openjdk 8, but then remove that source
RUN echo 'deb http://cdn-fastly.deb.debian.org/debian jessie-backports main' > /etc/apt/sources.list.d/jessie-backports.list && \
    apt-get -y update && \
    apt-get install --no-install-recommends -t jessie-backports -y openjdk-8-jre-headless ca-certificates-java && \
    rm /etc/apt/sources.list.d/jessie-backports.list && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
# endfrom


ENV PATH /home/jovyan/bin/:$PATH
ADD https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein /home/jovyan/bin/
RUN chmod +x /home/jovyan/bin/lein


ADD https://github.com/roryk/clojupyter/archive/master.zip clojupyter.zip
RUN unzip clojupyter.zip && \
		cd clojupyter-master && \
		sed -i -- 's/clojure "1.8.0"/clojure "1.9.0-alpha14"/g' project.clj && \
		make && \
		make install

USER $NB_USER
