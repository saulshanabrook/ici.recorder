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

RUN apk add --update bash && \
    rm -rf /var/cache/apk/*


COPY alluxio-out /alluxio
WORKDIR /alluxio

COPY entrypoint.sh /
ENTRYPOINT ["/entrypoint.sh"]

COPY alluxio-site.properties conf/
