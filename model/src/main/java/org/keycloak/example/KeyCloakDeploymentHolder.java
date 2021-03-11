package org.keycloak.example;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.keycloak.adapters.KeycloakDeployment;

@ApplicationScoped
@Named(KeyCloakDeploymentHolder.NAME)
public class KeyCloakDeploymentHolder
{
  public static final String NAME = "keyCloakDeploymentHolder";

  private KeycloakDeployment keycloakDeployment;

  public KeycloakDeployment getKeycloakDeployment()
  {
    return keycloakDeployment;
  }

  public void setKeycloakDeployment(final KeycloakDeployment keycloakDeployment)
  {
    this.keycloakDeployment = keycloakDeployment;
  }
}
