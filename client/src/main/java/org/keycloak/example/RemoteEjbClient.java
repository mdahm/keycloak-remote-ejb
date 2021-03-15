package org.keycloak.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb.client.EJBClientContext;
import org.keycloak.example.ejb.RemoteHello;
import org.keycloak.example.ejb.RemoteUserSession;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RemoteEjbClient
{
  public static void main(String[] args) throws Exception
  {
    final EJBClientContext ejbClientContext = EJBClientContext.getCurrent()
        // Two ways to add interceptor: Either programatically:
        //        .withAddedInterceptors(new ClientInterceptor())
        // or via META-INF/services/org.jboss.ejb.client.EJBClientInterceptor file
        ;

    // Step 1 : Retrieve username+password of user. It can be done anyhow by the application (eg. swing form)
    //        UsernamePasswordHolder usernamePassword = promptUsernamePassword();
    final UsernamePasswordHolder usernamePassword = new UsernamePasswordHolder("john", "password");
    //    final KeycloakToken unfugToken = new KeycloakToken("john", Collections.singletonList("user"), "jens", "hippe");

    System.out.println(
        "Will authenticate with username '" + usernamePassword.username + "' and password '" + usernamePassword.password + "'");

    // Step 2 : Keycloak DirectGrant (OAuth2 Resource Owner Password Credentials Grant) from the application
    final DirectGrantInvoker directGrant = new DirectGrantInvoker(usernamePassword.username, usernamePassword.password);
    final KeycloakToken keycloakToken1 = directGrant.authenticate();
    System.out.println("Successfully authenticated against Keycloak " + keycloakToken1);
    System.out.println("User-Info 1:" + directGrant.getUserinfo(keycloakToken1));

    // Step 3 : Push credentials to clientContext from where ClientInterceptor can retrieve them
    ejbClientContext.runCallable(() -> callRemoteEJB(keycloakToken1, 1));

    final KeycloakToken keycloakToken2 = directGrant.refresh(keycloakToken1);
    System.out.println("Successfully refreshed token");
    System.out.println("User-Info 2:" + directGrant.getUserinfo(keycloakToken1));
    System.out.println("User-Info 2:" + directGrant.getUserinfo(keycloakToken2));

    // Step 3 : Push credentials to clientContext from where ClientInterceptor can retrieve them
    ejbClientContext.runCallable(() -> callRemoteEJB(keycloakToken1, 2));
    ejbClientContext.runCallable(() -> callRemoteEJB(keycloakToken2, 2));

    directGrant.logout(keycloakToken2);

    //    System.out.println("User-Info 3:" + directGrant.getUserinfo(keycloakToken));

    // Das sollte dann knallen
    ejbClientContext.runCallable(() -> callRemoteEJB(keycloakToken2, 3));

    directGrant.shutdown();
  }

  private static Void callRemoteEJB(final KeycloakToken keycloakToken, final int number) throws Exception
  {
    System.out.println("Remote call #" + number);

    /* Fungiert quasi als Zwischenspeicher für unsere Daten, wird vom ClientInteceptor ausgelesen und den InvocationContext
     * hinzugefügt. Und von dort wird es dann serverseitig vom ServerSecurityInterceptor ausgelesen
     */
    SecurityActions.securityContextSetPrincipalCredential(null, keycloakToken);

    try
    {
      // Step 4 : EJB invoke
      final RemoteHello remoteHello = lookupRemoteStatelessHello(RemoteHello.class, RemoteHello.NAME);
      final RemoteUserSession remoteUser = lookupRemoteStatelessHello(RemoteUserSession.class, RemoteUserSession.NAME);
      System.out.println("Obtained RemoteHello for invocation");

      remoteUser.login();

      System.out.println("Going to invoke EJB");
      final String hello = remoteHello.helloSimple();
      System.out.println("HelloSimple invocation: " + hello);

      final String hello2 = remoteHello.helloAdvanced();
      System.out.println("HelloAdvanced invocation: " + hello2);

      remoteUser.logout();
    }
    finally
    {
      SecurityActions.clearSecurityContext();
    }

    return null;
  }

  /**
   * Looks up and returns the proxy to remote stateless calculator bean
   */
  @SuppressWarnings("unchecked")
  private static <T> T lookupRemoteStatelessHello(final Class<T> remoteInterface, final String beanName) throws NamingException
  {
    final Hashtable<String, Object> jndiProperties = new Hashtable<>();
    jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
    final Context context = new InitialContext(jndiProperties);

    try
    {
      // The app name is the application name of the deployed EJBs. This is typically the ear name
      // without the .ear suffix. However, the application name could be overridden in the application.xml of the
      // EJB deployment on the server.
      // Since we haven't deployed the application as a .ear, the app name for us will be an empty string
      final String appName = "ear-0.1-SNAPSHOT";
//      final String appName = "keycloak-remote-ejb";
      // This is the module name of the deployed EJBs on the server. This is typically the jar name of the
      // EJB deployment, without the .jar suffix, but can be overridden via the ejb-jar.xml
      // In this example, we have deployed the EJBs in a jboss-as-ejb-remote-app.jar, so the module name is
      // jboss-as-ejb-remote-app
      final String moduleName = "org.keycloak.example-ejb-module-0.1-SNAPSHOT";
      // the remote view fully qualified class name
      final String viewClassName = remoteInterface.getName();
      // let's do the lookup
      final String lookupKey = "ejb:"
          + appName
          + "/"
          + moduleName
          + "/"
          + beanName
          + "!"
          + viewClassName;

      return (T) context.lookup(lookupKey);
    }
    finally
    {
      context.close();
    }
  }

  private static class UsernamePasswordHolder
  {
    private final String username;
    private final String password;

    public UsernamePasswordHolder(String username, String password)
    {
      this.username = username;
      this.password = password;
    }
  }
}
