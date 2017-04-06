# `ici.recorder`

For recording experiments at the [Institute for Computational Intelligence
at Hampshire College](http://faculty.hampshire.edu/lspector/ici.html).

*This is a proof of concept. If it works out, then I will clean up code, add some tests, and extract reusable contents to another repo*


## Testing

```bash
docker-compose up -d alluxio
docker-compose run --rm clojure
docker-compose run --rm -w /tmp/test jupyter-notebook python -m unittest
```
