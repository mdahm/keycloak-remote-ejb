package org.keycloak.example.ejb;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SimplePrincipal;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.jaas.RolePrincipal;
import org.keycloak.representations.AccessToken;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@SuppressWarnings("rawtypes")
@Stateless
@Remote(RemoteHello.class)
@RolesAllowed({ "user", "UBI_ENTWICKLER" })
@SecurityDomain("keycloak-ejb")
@Interceptors({ ServerSecurityInterceptor.class })
public class HelloBean implements RemoteHello
{
  @Resource
  private SessionContext ctx;

  @Inject
  private KeyCloakTokenStore keyCloakTokenStore;

  @Override
  public String helloSimple()
  {
    final Principal principal = ctx.getCallerPrincipal();
    return "Simple - Hello " + principal.getName();
  }

  @Override
  public void logout()
  {
    final SecurityContext context = getSecurityContext();
    final AuthenticationManager authenticationManager = context.getAuthenticationManager();
    final Subject subject = context.getSubjectInfo().getAuthenticatedSubject();
    final Set<KeycloakPrincipal> keycloakPrincipals = subject.getPrincipals(KeycloakPrincipal.class);
    final KeycloakPrincipal kcPrincipal = keycloakPrincipals.iterator().next();
    final String token = kcPrincipal.getKeycloakSecurityContext().getTokenString();

    keyCloakTokenStore.invalidate(token);
    authenticationManager.logout(kcPrincipal, subject);
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

  // JBossCachedAuthenticationManager??
  private SecurityContext getSecurityContext()
  {
    return AccessController.doPrivileged(
        (PrivilegedAction<SecurityContext>) SecurityContextAssociation::getSecurityContext);
  }
}
