FROM quay.io/keycloak/keycloak:23.0.1
ADD recolnat-oauth2-realm.json /opt/keycloak/
RUN /opt/keycloak/bin/kc.sh  import --file /opt/keycloak/recolnat-oauth2-realm.json
RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev"]





