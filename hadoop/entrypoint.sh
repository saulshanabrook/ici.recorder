#!/bin/bash
set -e

/usr/sbin/sshd -D -f /etc/ssh/sshd_config &
ssh-keyscan -H localhost >> ~/.ssh/known_hosts
ssh-keyscan -H 0.0.0.0 >> ~/.ssh/known_hosts

exec "$@"
