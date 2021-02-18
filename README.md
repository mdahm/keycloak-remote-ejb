# keycloak-remote-ejb

This shows how to create remote EJB beans secured by Keycloak.

There is remote EJB client, which first asks user for his username+password and then authenticate against RHSSO/Keycloak server via
Direct Grant (OAuth2 Resource Owner Password Credential Grant). It sets the Keycloak accessToken to the EJB context (with usage of ClientInterceptor) and invokes remote EJB.

The server-side is remote EJB bean, which retrieves the token from the EJB Context passed from client and put it to the Wildfly SecurityContext where JAAS 
will find it (ServerSecurityInterceptor). JAAS realm will authenticate the token (BearerTokenLoginModule) and then it also needs to 
add needed GroupPrincipal, which is "known" to Wildfly, so that it can authorize EJB. Authenticated user with `user` role is able to invoke EJB.


How to have this running
------------------------
1. This example assumes Keycloak demo distribution downloaded somewhere ( will be referenced by $KEYCLOAK_DEMO ). It shouldn't be a problem
 to use separate RHSSO/Keycloak server and separate Wildfly server with installed Keycloak adapter though.
 
 
2. Build this project with: 
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
                        <login-module code="org.keycloak.example.ejb.BearerTokenLoginModule" flag="required">
                            <module-option name="keycloak-config-file" value="classpath:/keycloak-ejb.json"/>
                            <module-option name="auth-server-url" value="http://127.0.0.1:8080/auth"/>
                            <module-option name="realm" value="ejb-demo"/>
                        </login-module>
                        <login-module code="org.keycloak.example.ejb.ConvertKeycloakRolesLoginModule" flag="required"/>
                    </authentication>
                </security-domain>
    ````

5. Run the keycloak server

6. Create admin user in Keycloak and login to admin console (See Keycloak/RHSSO docs for details).

7. In keycloak admin console, import realm from file `testrealm.json` .

8. Run the client `RemoteEjbClient` 

If you login as user `john` with password `password`, you should be able to see that both EJB methods were successfully invoked.
When login as `mary` with password `password`, you should see the exception due to missing role `user` .

# Call chain

Call chain order:

1. ServerSecurityContainerInterceptor
2. SecurityInterceptor (JBossCachedAuthenticationManager)
3. BearerTokenLoginModule checks/verifies Token
4. ConvertKeycloakRolesLoginModule adds roles to subject
5. ServerSecurityInterceptor
6. Bean

# Secure application????

    <subsystem xmlns="urn:jboss:domain:keycloak:1.1">
            <secure-deployment name="ejb-module.jar">
                <realm>ejb-demo</realm>
                <resource>ejb-client</resource>
                <use-resource-role-mappings>false</use-resource-role-mappings>
                <public-client>true</public-client>
                <auth-server-url>http://localhost:8080/auth/</auth-server-url>
                <ssl-required>EXTERNAL</ssl-required>
                <verify-token-audience>true</verify-token-audience>
            </secure-deployment>
        </subsystem>


“DirectAccessGrantsLoginModule”, but nothing mentioned on “KeycloakLoginModule
