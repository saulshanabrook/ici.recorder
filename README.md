# `ici.recorder`

[![Build Status](https://travis-ci.org/saulshanabrook/ici.recorder.svg?branch=master)](https://travis-ci.org/saulshanabrook/ici.recorder)


For recording experiments at the [Institute for Computational Intelligence
at Hampshire College](http://faculty.hampshire.edu/lspector/ici.html).

*This is a proof of concept. If it works out, then I will clean up code, add some tests, and extract reusable contents to another repo*


## Testing
```bash
docker-compose up -d hadoop-namenode hadoop-datanode
docker-compose run --rm test-clojure
docker-compose run --rm test-jupyter-notebook
```


## Helpful commands

Copy all data to local machine

```bash
docker-compose run --no-deps --rm -v (pwd):/data/ hadoop-namenode dfs -D dfs.client.use.datanode.hostname=true -copyToLocal hdfs://$REMOTE_HOST:8020/clojush/ /data/
```
