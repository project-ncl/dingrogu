#!/bin/bash
#
# SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
# SPDX-License-Identifier: Apache-2.0
#


# TODO: once we move to external-secrets operator, we can in theory remove
#       that script

export QUARKUS_OIDC_CLIENT_CREDENTIALS_SECRET=$(cat /mnt/secrets/kieserver-${APP_ENV}/kieserver-sa-secret)
export QUARKUS_OIDC_CLIENT_CLIENT_ID=$(cat /mnt/secrets/kieserver-${APP_ENV}/kieserver-sa-client)

export sasl_jaas_conf="$(cat /mnt/secrets/${KAFKA_CLIENT_SECRET_NAME}-${APP_ENV}/kafka_jaas_conf)"

java \
    -Djava.util.logging.manager=org.jboss.logmanager.LogManager \
    -jar quarkus-run.jar
