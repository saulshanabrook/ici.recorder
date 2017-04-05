# based off of
# https://github.com/jupyterhub/jupyterhub-deploy-docker/blob/2a43a89dfb49b218760eee3d11794a3bdccdb100/Dockerfile.jupyterhub
FROM jupyterhub/jupyterhub:latest

# install docker on the jupyterhub container
RUN wget https://get.docker.com -q -O /tmp/getdocker && \
    chmod +x /tmp/getdocker && \
    sh /tmp/getdocker && \
    rm -rf /var/lib/apt/lists/*

# Install dockerspawner and its dependencies
RUN /opt/conda/bin/pip install \
    jupyterhub-dummyauthenticator \
    dockerspawner==0.7.*

# Copy TLS certificate and key
ENV SSL_CERT /srv/jupyterhub/secrets/jupyterhub.crt
ENV SSL_KEY /srv/jupyterhub/secrets/jupyterhub.key
COPY ./secrets/mycert.pem $SSL_CERT
COPY ./secrets/mykey.key $SSL_KEY
RUN chmod 700 /srv/jupyterhub/secrets && \
    chmod 600 /srv/jupyterhub/secrets/*

COPY ./jupyterhub_config.py /srv/jupyterhub/jupyterhub_config.py