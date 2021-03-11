package org.keycloak.example.ejb;

import java.util.Map;

import javax.ejb.EJBAccessException;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.jboss.as.security.api.ContextStateCache;
import org.jboss.logging.Logger;
import org.jboss.security.SimplePrincipal;
import org.keycloak.example.KeycloakToken;

/**
 * The server side security interceptor responsible for handling any security token propagated from the client.
 *
 * Container interceptors are specific to JBoss:
 * https://docs.jboss.org/author/display/WFLY/Container%20interceptors.html
 *
 * It is in front of the call chain, dependency injection thus does not work here.
 *
 * It is registered in jboss-ejb3.xml.
 */
public class ServerSecurityContainerInterceptor
{
  private static final Logger LOGGER = Logger.getLogger(ServerSecurityContainerInterceptor.class);

  @AroundInvoke
  public Object aroundInvoke(final InvocationContext invocationContext) throws Exception
  {
    final Map<String, Object> contextData = invocationContext.getContextData();

    if (contextData.containsKey(KeycloakToken.TOKEN_KEY))
    {
      final KeycloakToken keycloakToken = (KeycloakToken) contextData.get(KeycloakToken.TOKEN_KEY);
      ContextStateCache stateCache = null;

      LOGGER.info("Found KeycloakToken passed from client");

      try
      {
        try
        {
          // We have been requested to use an authentication token so now we attempt the switch.
          // This userPrincipal and credential will then be found by JAAS login modules
          final SimplePrincipal userPrincipal = new SimplePrincipal(keycloakToken.getUsername());
          final String accessToken = keycloakToken.getToken();

          stateCache = SecurityActions.pushIdentity(userPrincipal, accessToken);
          LOGGER.infof("Successfully pushed userPrincipal %s with roles %s his credential", userPrincipal.getName(),
              keycloakToken.getRoles());
        }
        catch (Exception e)
        {
          LOGGER.error("Failed to switch security context for user", e);
          // Don't propagate the exception stacktrace back to the client for security reasons
          throw new EJBAccessException("Unable to attempt switching of user.");
        }

        return invocationContext.proceed();
      }
      finally
      {
        // switch back to original context
        if (stateCache != null)
        {
          SecurityActions.popIdentity(stateCache);
        }
      }
    }
    else
    {
      LOGGER.warn("No Keycloak token found for " + invocationContext.getMethod());
      return invocationContext.proceed();
    }
  }
}
