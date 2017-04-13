FROM jupyter/pyspark-notebook

ENV ALLUXIO_JAR_PATH /tmp/alluxio.jar
COPY alluxio.jar ${ALLUXIO_JAR_PATH}
USER root
RUN chown ${NB_USER} ${ALLUXIO_JAR_PATH}
USER ${NB_USER}


COPY spark-defaults.conf ${SPARK_HOME}/conf/
COPY core-site.xml ${SPARK_HOME}/conf/
COPY alluxio-site.properties /etc/alluxio/
# RUN conda install jupyterlab
# CMD ["start.sh", "jupyter", "lab"]
