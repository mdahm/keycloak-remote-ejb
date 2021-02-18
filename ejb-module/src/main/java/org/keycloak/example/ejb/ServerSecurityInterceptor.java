package org.keycloak.example.ejb;

import java.util.Map;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

@Interceptor
public class ServerSecurityInterceptor
{
  private static final Logger LOGGER = Logger.getLogger(ServerSecurityInterceptor.class);

  @Inject
  private KeyCloakTokenStore keyCloakTokenStore;

  @AroundInvoke
  public Object aroundInvoke(final InvocationContext invocationContext) throws Exception
  {
    LOGGER.info("Intercept");

    final Map<String, Object> contextData = invocationContext.getContextData();

    if (contextData.containsKey(KeycloakToken.TOKEN_KEY))
    {
      final KeycloakToken keycloakToken = (KeycloakToken) contextData.get(KeycloakToken.TOKEN_KEY);
      final boolean validate = keyCloakTokenStore.registerToken(keycloakToken);

      if (validate)
      {
        LOGGER.info("KeyCloak token for user " + keycloakToken.getUsername() + " needs to be validated!");
      }
    }

    return invocationContext.proceed();
  }
}
