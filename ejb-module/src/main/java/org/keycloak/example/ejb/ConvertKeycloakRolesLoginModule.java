package org.keycloak.example.ejb;

import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.jboss.logging.Logger;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.keycloak.adapters.jaas.RolePrincipal;

/**
 * This login module is supposed to be in the chain after Keycloak BearerTokenLoginModule or DirectAccessGrantsLoginModule.
 * <p>
 * It just converts Keycloak roles to the Wildfly-specific principal, which Wildfly is able to recognize and
 * establish EJB authorization based on that.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@SuppressWarnings("unused")
public class ConvertKeycloakRolesLoginModule implements LoginModule
{
  private static final Logger LOGGER = Logger.getLogger(ConvertKeycloakRolesLoginModule.class);

  private Subject subject;

  @Override
  public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState,
      Map<String, ?> options)
  {
    this.subject = subject;
  }

  @Override
  public boolean login()
  {
    LOGGER.debug("login");
    return true;
  }

  @Override
  public boolean commit()
  {
    final Set<RolePrincipal> kcRoles = subject.getPrincipals(RolePrincipal.class);
    LOGGER.info("commit invoked. Keycloak roles: " + kcRoles);

    final SimpleGroup wfRoles = new SimpleGroup("Roles");
    for (RolePrincipal kcRole : kcRoles)
    {
      wfRoles.addMember(new SimplePrincipal(kcRole.getName()));
    }

    subject.getPrincipals().add(wfRoles);

    return true;
  }

  @Override
  public boolean abort()
  {
    return true;
  }

  @Override
  public boolean logout()
  {
    LOGGER.debug("logout");
    return true;
  }
}
