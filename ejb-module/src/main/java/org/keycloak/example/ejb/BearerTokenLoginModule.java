package org.keycloak.example.ejb;

import java.util.Map;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import org.jboss.logging.Logger;
import org.keycloak.adapters.jaas.AbstractKeycloakLoginModule;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.adapters.config.AdapterConfig;

import javax.enterprise.inject.spi.BeanManager;

/**
 * Token based Login module, which allows to authenticate Keycloak access token in environments.
 * <p>
 * KeyCloak deployment properties may be overriden via login module options configured in standalone.xml.
 * By default the configuration just relies on the configuration via a JSON file supplied within the deployment.
 * <p>
 * This is more flexible and easier to change with plain editing or CLI actions instead of a complete re-build and re-deplopyment.
 * May be extended with further options/properties.
 */
public class BearerTokenLoginModule extends AbstractKeycloakLoginModule
{
  private static final Logger log = Logger.getLogger(BearerTokenLoginModule.class);

  public static final String AUTH_SERVER_URL = "auth-server-url";
  public static final String REALM = "realm";
  public static final String RESOURCE = "resource";

  @Override
  public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState,
      final Map<String, ?> options)
  {
    super.initialize(subject, callbackHandler, sharedState, options);

    final KeyCloakDeploymentHolder holder = lookupDeploymentHolderBean();

    if (deployment != null)
    {
      if (options.containsKey(AUTH_SERVER_URL))
      {
        final AdapterConfig config = new AdapterConfig();
        config.setAuthServerUrl((String) options.get(AUTH_SERVER_URL));
        deployment.setAuthServerBaseUrl(config);
      }

      if (options.containsKey(REALM))
      {
        deployment.setRealm((String) options.get(REALM));
      }

      if (options.containsKey(RESOURCE))
      {
        deployment.setResourceName((String) options.get(RESOURCE));
      }

      holder.setKeycloakDeployment(deployment);
    }
  }

  private KeyCloakDeploymentHolder lookupDeploymentHolderBean()
  {
    final BeanManager beanManager = CDI.current().getBeanManager();
    final Bean<?> bean = beanManager.getBeans(KeyCloakDeploymentHolder.NAME).iterator().next();
    final CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);

    return (KeyCloakDeploymentHolder) beanManager.getReference(bean, KeyCloakDeploymentHolder.class, creationalContext);
  }

  @Override
  protected Auth doAuth(String username, String password) throws VerificationException
  {
    return bearerAuth(password);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
