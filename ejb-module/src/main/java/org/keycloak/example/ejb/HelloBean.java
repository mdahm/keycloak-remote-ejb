package org.keycloak.example.ejb;

import java.security.Principal;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.security.auth.Subject;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.SecurityContext;
import org.jboss.security.SimplePrincipal;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.jaas.RolePrincipal;
import org.keycloak.representations.AccessToken;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@SuppressWarnings("rawtypes")
@Stateless(name = RemoteHello.NAME)
@Remote(RemoteHello.class)
@RolesAllowed({ "user", "UBI_ENTWICKLER" })
//@SecurityDomain("keycloak-ejb")
public class HelloBean extends AbstractBean implements RemoteHello
{
  @Override
  public String helloSimple()
  {
    final Principal principal = ctx.getCallerPrincipal();
    return "Simple - Hello " + principal.getName();
  }

  // Use Keycloak-specific API to retrieve KeycloakPrincipal and the underlying token from it
  @Override
  public String helloAdvanced()
  {
    final SecurityContext context = getSecurityContext();
    final AuthenticationManager authenticationManager = context.getAuthenticationManager();
    final Subject subject = context.getSubjectInfo().getAuthenticatedSubject();
    final Set<SimplePrincipal> principals = subject.getPrincipals(SimplePrincipal.class);
    //        authenticationManager.logout(principal, subject);
    final Set<RolePrincipal> keycloakRoles = subject.getPrincipals(RolePrincipal.class);
    final Set<KeycloakPrincipal> keycloakPrincipals = subject.getPrincipals(KeycloakPrincipal.class);
    final KeycloakPrincipal kcPrincipal = keycloakPrincipals.iterator().next();
    final AccessToken accessToken = kcPrincipal.getKeycloakSecurityContext().getToken();

    return "Advanced - Hello " + accessToken.getName();
  }
}
