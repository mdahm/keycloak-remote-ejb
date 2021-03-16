package org.keycloak.example.ejb;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.security.auth.Subject;

import org.jboss.security.SimplePrincipal;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.SecurityContext;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.jaas.RolePrincipal;

@Stateless(name = RemoteUserSession.NAME)
@Remote(RemoteUserSession.class)
// Necessary, if not configured via jboss-app.xml
//@SecurityDomain("keycloak-ejb")
@PermitAll
@SuppressWarnings("rawtypes")
public class UserSessionBean extends AbstractBean implements RemoteUserSession
{
  private static final Logger LOGGER = Logger.getLogger(UserSessionBean.class);

  public void login()
  {
    final SecurityContext context = getSecurityContext();
    final Subject subject = context.getSubjectInfo().getAuthenticatedSubject();
    final List<String> keycloakRoles = subject.getPrincipals(RolePrincipal.class).stream()
        .map(RolePrincipal::getName).collect(Collectors.toList());
    final SimplePrincipal principal = subject.getPrincipals(SimplePrincipal.class).iterator().next();
    final KeycloakPrincipal kcPrincipal = subject.getPrincipals(KeycloakPrincipal.class).iterator().next();

    LOGGER.infov("User {0}({1}) logged in with roles {2}", principal.getName(), kcPrincipal.getName(), keycloakRoles);
  }

  @Override
  public void logout()
  {
    final SecurityContext context = getSecurityContext();
    final AuthenticationManager authenticationManager = context.getAuthenticationManager();
    final Subject subject = context.getSubjectInfo().getAuthenticatedSubject();
    final KeycloakPrincipal kcPrincipal = subject.getPrincipals(KeycloakPrincipal.class).iterator().next();
    final SimplePrincipal principal = subject.getPrincipals(SimplePrincipal.class).iterator().next();
    final String token = kcPrincipal.getKeycloakSecurityContext().getTokenString();

    keyCloakTokenStore.invalidate(token);
    authenticationManager.logout(kcPrincipal, subject);

    LOGGER.infov("User {0}({1}) logged out", principal.getName(), kcPrincipal.getName());
  }
}
