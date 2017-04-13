# modified from
# https://github.com/Alluxio/alluxio/blob/fae727c2236d0c5dfe53c04006dd70509cb38b94/integration/docker/Dockerfile

#
# The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
# (the "License"). You may not use this work except in compliance with the License, which is
# available at www.apache.org/licenses/LICENSE-2.0
#
# This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
# either express or implied, as more fully set forth in the License.
#
# See the NOTICE file distributed with this work for information regarding copyright ownership.
#

# must switch to JDK 8 in order to apk install maven
FROM openjdk:8-jdk-alpine

ARG ALLUXIO_REF=master

# download and build alluxio
RUN   apk add --update ca-certificates wget maven bash \
  &&  update-ca-certificates \
  &&  rm -rf /var/cache/apk/*

ADD https://github.com/Alluxio/alluxio/archive/${ALLUXIO_REF}.zip alluxio.zip
RUN unzip -qq alluxio.zip

# this part modified from aluxio/dev/scripts/generate-tarball.sh
WORKDIR alluxio-${ALLUXIO_REF}
RUN mvn -q --batch-mode -T 4C install package -Dspark -Dhadoop.version=2.3.0 \
-Dmaven.javadoc.skip=true -DskipTests -Dlicense.skip=true \
-Dcheckstyle.skip=true -Dfindbugs.skip=true
RUN tar --exclude .git --exclude logs --exclude dev -czf /tmp/alluxio.tgz .
CMD ["cat", "/tmp/alluxio.tgz"]
