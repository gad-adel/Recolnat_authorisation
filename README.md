![logo Recolnat](./src/test/resources/images/logo.png)  

### Setup Requirements  
   
####  Docker compose Run    
##### lancez le Docker-Compose:   
..\..\recolnat-backend-stack\authorisation>**docker compose up -d**   

##### lancement projet en local:
soit modifiez le pom :   

```xml
 <plugin>  
        <groupId>org.springframework.boot</groupId>   
        <artifactId>spring-boot-maven-plugin</artifactId>  
        <version>2.0.1.RELEASE</version>  
        <configuration>  
          <profiles>  
            <profile>local</profile>   
          </profiles>  
        </configuration>  
        ...   
      </plugin>
```
    
Puis lancez mvn spring-boot:run   
   
Ou lancez via l'une des commandes suivantes:   

 - mvn spring-boot:run -Dspring-boot.run.profiles=local   
 - java -jar .\target\authorisation-0.0.1-SNAPSHOT.jar  --spring.profiles.active=local  ou   java -Dspring.profiles.active=local -jar  ./target/authorisation-0.0.1-SNAPSHOT.jar    

#### View :   
Swagger: http://localhost:8081/swagger-ui/index.html  
KeyCloak: http://localhost:8089/auth/    
acces base ( via Dbeaver): Host: localhost  Databases: (Même login/pwd):   authdb(authdb/authdb), itv(itv/itv), syncdb(syncdb/syncdb), keycloak(adminkc/adminkc)   

##### Pour arrêter le docker compose:  
..\..\recolnat-backend-stack\authorisation>**docker compose down -v**

### Test the API:
##### Postman:
faites un import dans Postman, via l url http://localhost:8081/swagger-ui/index.htm de Swagger.

Sql:  
https://www.ibm.com/docs/en/db2/10.5?topic=table-examples-subselect-queries-joins





----
Run with K8s

cd postgres
docker build -t docker.mnhn.fr/recolnat/postgres .

cd keycloak
docker build -t docker.mnhn.fr/recolnat/keycloak .

cd k8s
k create -f dev

# App running at : 
http://localhost:8081/swagger-ui/index.html#/

kubectl kustomize  --load-restrictor='LoadRestrictionsNone'  ./k8s/base/overlays/dev/ |\kubectl apply -f -

kustomize build $OVERLAYS/dev |\kubectl apply -f -

k kustomize  k8s/overlays/dev  --reorder='legacy' -o  app.yml
k kustomize  k8s/overlays/prod  --reorder='legacy' -o  app.yml

IMAGE_TAG=1.0.0 envsubst '${IMAGE_TAG}' < deployment.yaml > final_deployment.yaml


