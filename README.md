# `ici.recorder`

[![Build Status](https://travis-ci.org/saulshanabrook/ici.recorder.svg?branch=master)](https://travis-ci.org/saulshanabrook/ici.recorder)


For recording experiments at the [Institute for Computational Intelligence
at Hampshire College](http://faculty.hampshire.edu/lspector/ici.html).
Currently, it's status is best documented [in a blog post](https://medium.com/@saulshanabrook/data-at-hampshire-ici-8f0e0c064f14).

It is not being used by members of the group yet.


## Testing
```bash
docker-compose up
```

## Deploying

```bash
set -x TAG temp.xxx
docker-compose build
docker-compose push

eval (docker-machine env deucalion)
docker stack deploy -c docker-compose.yml ici-recorder
```