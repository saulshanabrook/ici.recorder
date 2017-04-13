# `ici.recorder`

[![Build Status](https://travis-ci.org/saulshanabrook/ici.recorder.svg?branch=master)](https://travis-ci.org/saulshanabrook/ici.recorder)


For recording experiments at the [Institute for Computational Intelligence
at Hampshire College](http://faculty.hampshire.edu/lspector/ici.html).

*This is a proof of concept. If it works out, then I will clean up code, add some tests, and extract reusable contents to another repo*



## How it all works
Getting Alluxio to work is troublesome. We use it's code in three places:

1.  The `alluxio-server`, running twice as a master and a worker
2.  The `clojure` code, so that it can write to alluxio, using the
    [Haddop MapReduce integration](http://www.alluxio.org/docs/1.4/en/Running-Hadoop-MapReduce-on-Alluxio.html).
3.  Spark in `jupter-notebook`, so that it can read from alluxio, using the
    [spark integration](http://www.alluxio.org/docs/1.4/en/Running-Spark-on-Alluxio.html).

Ideally they should all be running the same version of Alluxio. We have to build
from source, because we want [this unreleased feature](https://github.com/Alluxio/alluxio/pull/4771/)
to get it to work with Docker. So we get the source, build a bunch of different
artifcats for the different jobs, and then use one configuration file and copy it
into `/etc/alluxio/` so everything picks it up.

This will be much easier when Docker 17.05.0 comes out and we get
[extndabe dockerfies](https://github.com/docker/docker/pull/32063).
Until then we have to:

```bash
docker-compose build build-alluxio
mkdir alluxio-out
docker-compose run --rm -T build-alluxio | tar -zxC alluxio-out -f -

cp alluxio-out/core/client/target/alluxio-core-client-1.5.0-SNAPSHOT-jar-with-dependencies.jar jupyter-notebook/alluxio.jar
cp alluxio-out/core/client/target/alluxio-core-client-1.5.0-SNAPSHOT-jar-with-dependencies.jar clojure/alluxio.jar
cp -r alluxio-out/ alluxio-server/alluxio-out

cp alluxio/alluxio-site.properties alluxio-server/
cp alluxio/alluxio-site.properties clojure/
cp alluxio/alluxio-site.properties jupyter-notebook/

docker-compose build
```

## Testing

```bash
docker-compose up -d alluxio-master alluxio-worker
docker-compose run --rm test-alluxio
docker-compose run --rm test-clojure
docker-compose run --rm test-jupyter-notebook
```
