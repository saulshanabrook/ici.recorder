# modified to use unreleased spark 2.2.0 so we can write JSON arrays
# from https://github.com/jupyter/docker-stacks/blob/master/pyspark-notebook/Dockerfile
FROM jupyter/scipy-notebook

USER root


# Temporarily add jessie backports to get openjdk 8, but then remove that source
RUN echo 'deb http://cdn-fastly.deb.debian.org/debian jessie-backports main' > /etc/apt/sources.list.d/jessie-backports.list && \
    apt-get -y update && \
    apt-get install --no-install-recommends -t jessie-backports -y openjdk-8-jre-headless ca-certificates-java && \
    rm /etc/apt/sources.list.d/jessie-backports.list && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
RUN cd /tmp && \
        wget -q http://people.apache.org/~pwendell/spark-nightly/spark-branch-2.2-bin/spark-2.2.0-SNAPSHOT-2017_04_24_14_16-cf16c32-bin/spark-2.2.0-SNAPSHOT-bin-hadoop2.7.tgz && \
        tar xzf spark-2.2.0-SNAPSHOT-bin-hadoop2.7.tgz -C /usr/local && \
        rm spark-2.2.0-SNAPSHOT-bin-hadoop2.7.tgz
RUN cd /usr/local && ln -s spark-2.2.0-SNAPSHOT-bin-hadoop2.7 spark


# to fix docker volume thing
RUN mkdir /output && \
  chown ${NB_USER} /output

ENV SPARK_HOME /usr/local/spark
ENV PYTHONPATH $SPARK_HOME/python:$SPARK_HOME/python/lib/py4j-0.10.4-src.zip

COPY requirements.conda.txt requirements.conda.txt
RUN conda install -y --channel conda-forge --file requirements.conda.txt

# modified to add more memory
ENV SPARK_OPTS  --driver-memory 3g --driver-java-options=-Xms3G --driver-java-options=-Xmx3G --driver-java-options=-Dlog4j.logLevel=info

USER ${NB_USER}
