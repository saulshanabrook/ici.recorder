FROM jupyter/pyspark-notebook

ENV ALLUXIO_JAR_PATH /tmp/alluxio.jar
ADD http://downloads.alluxio.org/downloads/files/1.4.0/alluxio-1.4.0-spark-client-jar-with-dependencies.jar ${ALLUXIO_JAR_PATH} 
USER root
RUN chown ${NB_USER} ${ALLUXIO_JAR_PATH}
USER ${NB_USER}

COPY spark-defaults.conf ${SPARK_HOME}/conf/
COPY core-site.xml ${SPARK_HOME}/conf/
# RUN conda install jupyterlab
# CMD ["start.sh", "jupyter", "lab"]