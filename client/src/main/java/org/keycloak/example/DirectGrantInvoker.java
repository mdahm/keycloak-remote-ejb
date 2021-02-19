package org.keycloak.example;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.keycloak.OAuth2Constants;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.common.util.StreamUtil;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.example.KeycloakToken;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DirectGrantInvoker
{
  private static final String USERINFO = "/realms/{realm-name}/protocol/openid-connect/userinfo";

  private static final String KEYCLOAK_ROOT = "http://localhost:8080/auth";
  private static final String KEYCLOAK_REALM = "ejb-demo";
  private static final String KEYCLOAK_CLIENT = "ejb-client";
  private final String username;
  private final String password;
  private final CloseableHttpClient _httpClient = HttpClientBuilder.create().build();

  public DirectGrantInvoker(final String username, final String password)
  {
    this.username = username;
    this.password = password;
  }

  public String getUserinfo(final KeycloakToken keycloakToken) throws IOException
  {
    final HttpGet httpGet = new HttpGet(KeycloakUriBuilder.fromUri(KEYCLOAK_ROOT)
        .path(USERINFO).build(KEYCLOAK_REALM));
    httpGet.addHeader("Authorization", "Bearer " + keycloakToken.getToken());

    return checkResponse(_httpClient, httpGet);
  }

  @SuppressWarnings("unchecked")
  public KeycloakToken keycloakAuthenticate() throws IOException, VerificationException
  {
    final HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(KEYCLOAK_ROOT)
        .path(ServiceUrlConstants.TOKEN_PATH).build(KEYCLOAK_REALM));
    final List<NameValuePair> formparams = new ArrayList<>();
    formparams.add(new BasicNameValuePair("username", username));
    formparams.add(new BasicNameValuePair("password", password));
    formparams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "password"));
    formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, KEYCLOAK_CLIENT));
    final UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
    post.setEntity(form);

    final String json = checkResponse(_httpClient, post);
    final AccessTokenResponse accessTokenResponse = JsonSerialization.readValue(json, AccessTokenResponse.class);
    final AccessToken accessToken = TokenVerifier.create(accessTokenResponse.getToken(), AccessToken.class).getToken();
    final List<String> roles = (List<String>) accessToken.getOtherClaims().get("Roles");

    return new KeycloakToken(accessToken.getPreferredUsername(), roles, accessTokenResponse.getToken(),
        accessTokenResponse.getRefreshToken());
  }

  private String checkResponse(final CloseableHttpClient client, final HttpUriRequest post) throws IOException
  {
    final HttpResponse response = client.execute(post);
    final int status = response.getStatusLine().getStatusCode();
    final HttpEntity entity = response.getEntity();

    System.out.println("Response: " + status + " " + entity);

    if (status >= 300)
    {
      final String json = StreamUtil.readString(entity.getContent(), Charset.defaultCharset());
      throw new IOException("Bad status: " + status + " response: " + json);
    }

    return (entity != null) ? StreamUtil.readString(entity.getContent(), Charset.defaultCharset()) : "";
  }

  public void logout(final KeycloakToken keycloakToken) throws IOException
  {
    final HttpPost request = new HttpPost(KeycloakUriBuilder.fromUri(KEYCLOAK_ROOT)
        .path(ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH).build(KEYCLOAK_REALM));
    final List<NameValuePair> formparams = new ArrayList<>();
    formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, KEYCLOAK_CLIENT));
    formparams.add(new BasicNameValuePair(OAuth2Constants.REFRESH_TOKEN, keycloakToken.getRefreshToken()));
    formparams.add(new BasicNameValuePair(OAuth2Constants.USERNAME, username));
    final UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");

    request.setEntity(form);
//    request.addHeader("Authorization", "Bearer " + keycloakToken.getToken());

    // https://stackoverflow.com/questions/48274251/keycloak-access-token-validation-end-point

    final String response = checkResponse(_httpClient, request);
    System.out.println("Logout Response: " + response);
  }

  public void shutdown() throws IOException
  {
    _httpClient.close();
  }
}
