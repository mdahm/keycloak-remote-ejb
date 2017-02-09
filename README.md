# keycloak-remote-ejb

This shows how to create remote EJB beans secured by Keycloak.

There is remote EJB client, which first asks user for his username+password and then authenticate against Keycloak server via
Direct Grant (OAuth2 Resource Owner Password Credential Grant). It sets the Keycloak accessToken to the EJB context (with usage of ClientInterceptor).

The server-side is remote EJB bean, which retrieves the token from the EJB Context passed from client and put it to the ThreadLocal where JAAS 
will find it (ServerSecurityInterceptor). JAAS realm will authenticate the token (BearerTokenLoginModule) and then it also needs to 
add needed principal, which are "known" to Wildfly, so that it can authorize EJB. User with "user" role is able to invoke EJB.


How to have this running
------------------------
1. This example assumes Keycloak demo distribution downloaded somewhere ( will be referenced by $KEYCLOAK_DEMO ). It shouldn't be a problem
 to use separate Keycloak server and separate Wildfly server with installed Keycloak adapter though.
 
 
2. Build this with: 
````
mvn clean install
````

3. Deploy remote ejb to the wildfly server. 
````
cp ejb-module/target/ejb-module.jar $KEYCLOAK_DEMO_HOME/keycloak/standalone/deployment
````

4. Add new security-domain to the security-domains inside the file `$KEYCLOAK_DEMO_HOME/keycloak/standalone/configuration/standalone.xml`:
````
                <security-domain name="keycloak-ejb">
                    <authentication>
                        <login-module code="org.keycloak.adapters.jaas.BearerTokenLoginModule" flag="required">
                            <module-option name="keycloak-config-file" value="classpath:/keycloak-ejb.json"/>
                        </login-module>
                        <login-module code="org.keycloak.example.ejb.ConvertKeycloakRolesLoginModule" flag="required"/>
                    </authentication>
                </security-domain>
````

5. Run the Wildfly server

6. Create admin user in Keycloak and login to admin console (See Keycloak docs for details).

7. Import realm from file `testrealm.json` in this project

8. TODO Run the client. You can either run class `RemoteEjbClient` from IDE or use maven command like this:
````
cd client
mvn exec:java -Pclient
````

If you login as user `john` with password `password`, you should be able to see that both EJB methods were successfully invoked.
When login as `mary` with password `password`, you should see the exception due to missing role `user` .

