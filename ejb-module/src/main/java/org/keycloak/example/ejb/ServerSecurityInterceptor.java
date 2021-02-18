package org.keycloak.example.ejb;

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
    return invocationContext.proceed();
  }
}
