# Prerequisites

* Download & install WildFly
* Install keycloak-adapter in WildFly (keycloak-oidc-wildfly-adapter-12.0.4.zip)
* Install security domain with CLI script (install-security-domain.cli)
* Deploy KeyCloak WAR from keycloak-web project (https://github.com/akquinet/keycloak-web)
* Import realm from file `testrealm.json` in KeyCloak dmin Console
* Deploy WAR from this project
* Run the client `RemoteEjbClient`

# How it works

There is remote EJB client, which authenticates against RHSSO/Keycloak server via
Direct Grant (OAuth2 Resource Owner Password Credential Grant). It sets the Keycloak accessToken to the EJB context (with usage of ClientInterceptor) and invokes remote EJB.

The server-side is remote EJB bean, which retrieves the token from the EJB Context passed from client and put it to the Wildfly SecurityContext where JAAS 
will find it (ServerSecurityInterceptor). JAAS realm will authenticate the token (BearerTokenLoginModule) and then it also needs to 
add needed GroupPrincipal, which is "known" to Wildfly, so that it can authorize EJB. Authenticated user with `user` role is able to invoke EJB.


## Security domain
------------------------
    ````
                <security-domain name="keycloak-ejb">
                    <authentication>
                        <login-module code="org.keycloak.example.ejb.BearerTokenLoginModule" flag="required">
                            <module-option name="keycloak-config-file" value="classpath:/keycloak-ejb.json"/>
                        </login-module>
   
                        <login-module code="org.keycloak.example.ejb.ConvertKeycloakRolesLoginModule" flag="required"/>
                    </authentication>
                </security-domain>
    ````
# Call chain

Call chain order:

0. ClientInterceptor
1. ServerSecurityContainerInterceptor
2. SecurityInterceptor (JBossCachedAuthenticationManager)
3. BearerTokenLoginModule checks/verifies Token offline
4. ConvertKeycloakRolesLoginModule adds roles to subject
5. ServerSecurityInterceptor checks/verifies Token online once
6. Bean
