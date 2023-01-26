# Hilla SSO Kit

SSO Kit is an add-on for Hilla that provides all the configuration you need to add single sign-on capabilities to your applications.

SSO Kit is built upon the [OpenID Connect](https://openid.net/specs/openid-connect-core-1_0.html) specification and it comes with a [Spring Boot](https://spring.io/projects/spring-boot) starter module that takes care of configuring the security settings you need to authenticate against your identity provider.

These are the currently supported identity providers:

- [Keycloak](https://www.keycloak.org/)
- [Okta](https://okta.com/)
- [Azure Active Directory](https://azure.microsoft.com/en-us/services/active-directory/)

SSO Kit is compatible with [Hilla](https://hilla.dev/) starting from version [1.3.2](https://github.com/vaadin/hilla/releases/tag/1.3.2).

## Getting started

This guide explains how to add both authentication and SSO Kit to an existing Hilla application, in a step-by-step way, but you can also start from the demo application which is part of this repository and already includes all these changes. Just remember to change the parent POM if you want to copy that application out.

### Create a Hilla application without authentication

Create a Hilla application using this command:

```bash
npx @hilla/cli init --next <your-project-name>
```

### Add dependencies

Add the `sso-kit-starter` module and the other required dependencies to the `pom.xml` of your Vaadin application:

```xml
<dependency>
    <groupId>dev.hilla</groupId>
    <artifactId>sso-kit-hilla-starter</artifactId>
    <version>2.0.0.alpha1</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Then add authentication to your application as explained in the [relevant section of the Hilla documentation](https://hilla.dev/docs/lit/guides/security). While all the details are explained there, let's walk the necessary steps in sequence.

### Add Hilla Endpoints

An [SingleSignOnEndpoint](sso-kit-starter/src/main/java/dev/hilla/sso/endpoint/SingleSignOnEndpoint.java) is already included in the kit. To use it, you must [enable the new Hilla multi-module engine](https://hilla.dev/docs/lit/reference/configuration/#java-compiler-options). The easiest way to enable it is to create (or modify) the `src/main/resources/vaadin-featureflags.properties` file and add this line:

```properties
com.vaadin.experimental.hillaEngine=true
```

Optionally, if you want to get user information or to have a message about the back channel logout, you can use the [UserEndpoint](sso-kit-starter/src/main/java/dev/hilla/sso/endpoint/UserEndpoint.java) and the [BackChannelLogoutEndpoint](sso-kit-starter/src/main/java/dev/hilla/sso/endpoint/BackChannelLogoutEndpoint.java) too.

Otherwise, if you want to customize the returned data by the endpoints, copy the [whole package](https://github.com/vaadin/sso-kit-hilla/tree/main/sso-kit-hilla-starter/src/main/java/dev/hilla/sso/endpoint) into your application and modify it.

Unless you use the same package name as for your application (by default it is `com.example.application` in generated Hilla projects), you have to whitelist your package in Spring Boot for Hilla to be able to find the Endpoint. Open your `Application.java` and add the package to the annotation:

```java
@SpringBootApplication(scanBasePackages = {
  "com.example.application", // Application package
  "dev.hilla.sso" // SSO Kit
})
public class Application ...
```

### Protect the Endpoint

Hilla allows fine-grained authorization on Endpoints and Endpoint methods. You can use annotations like `@PermitAll` or `@RolesAllowed(...)` to declare who can access what.

To try this feature, replace the `@AnonymousAllowed` annotation in `HelloWorldEndpoint.java` with `@PermitAll`, so that unauthenticated users will be unable to access the whole Endpoint. You could also apply the same annotation at method level.

### Configure the SSO provider in Spring

As a provider is needed, let's suppose you have a local Keycloak running on your machine on port 8081. Get a realm name, a client name, and the client secret and add those values to your `application.properties` file:

```properties
spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:8081/realms/your-realm
spring.security.oauth2.client.registration.keycloak.client-id=your-client
spring.security.oauth2.client.registration.keycloak.client-secret=your-secret
spring.security.oauth2.client.registration.keycloak.scope=profile,openid,email,roles
```

### Use the Endpoint

Start the application using the `./mvnw` command (`.\mvnw` on Windows), so that Hilla generates TypeScript files.

Inside the `AppStore` class in `app-store.ts` add this code:

```typescript
user: User | undefined = undefined;
logoutUrl: string | undefined = undefined;

async fetchAuthInfo() {
  const authInfo = await AuthEndpoint.getAuthInfo();
  this.user = authInfo.user;
  this.logoutUrl = authInfo.logoutUrl;
}

clearUserInfo() {
  this.user = undefined;
  this.logoutUrl = undefined;
}

get loggedIn() {
  return !!this.user;
}

isUserInRole(role: string) {
  return this.user?.roles?.includes(role);
}
```

You should be able to add the missing imports automatically.

Open the `frontend/index.ts` file and delay the router setup until the login information has been fetched by wrapping the `setRoutes` call as follows:

```typescript
appStore.fetchAuthInfo().finally(() => {
  // Ensure router access checks are not done before we know if we are logged in
  router.setRoutes(routes);
});
```

### Add access control to existing routes

As the `HelloWorldEndpoint` is now only accessible to registered users, it makes sense to also protect the view that uses it.

Open the `frontend/routes.ts` file and enrich the `ViewRoute` type:

```typescript
export type ViewRoute = Route & {
  title?: string;
  icon?: string;
  children?: ViewRoute[];
  // add the following two properties
  requiresLogin?: boolean;
  rolesAllowed?: string[];
};
```

The `rolesAllowed` property is not used in this example, but it is good to have it, as you can protect views according to user roles, e.g. `rolesAllowed: ['admin', 'manager']`. Those roles must be configured in the SSO provider.

Then add a function to determine is the user has access to the requested view:

```typescript
export const hasAccess = (route: Route) => {
  const viewRoute = route as ViewRoute;
  if (viewRoute.requiresLogin && !appStore.loggedIn) {
    return false;
  }

  if (viewRoute.rolesAllowed) {
    return viewRoute.rolesAllowed.some((role) => appStore.isUserInRole(role));
  }
  return true;
};
```

Modify the `hello` path so that it requires login and redirects to the SSO Login page if needed:

```typescript
{
  path: 'hello',
  requiresLogin: true,
  icon: 'la la-globe',
  title: 'Hello World',
  action: async (_context, _command) => {
    return hasAccess(_context.route) ? _command.component('hello-world-view') : _command.redirect('login');
  },
},
```

Add a `login` route to the exported routes:

```typescript
{
  path: 'login',
  icon: '',
  title: 'Login',
  action: async (_context, _command) => {
    location.href = '/oauth2/authorization/keycloak';
  },
},
```

### Add login and logout to the interface

Open `frontend/views/main-layout.ts` and add a login/logout button in the `footer`:

```html
<footer slot="drawer">
  ${appStore.user
    ? html`
        <vaadin-menu-bar
          theme="tertiary-inline contrast"
          .items="${this.getUserMenuItems(appStore.user)}"
          @item-selected="${this.userMenuItemSelected}"
        ></vaadin-menu-bar>
      `
    : html`<a router-ignore href="/oauth2/authorization/keycloak">Sign in</a>`
  }
</footer>
```

Add the needed functions:

```typescript
private getUserMenuItems(user: User): MenuBarItem[] {
  return [
    {
      component: this.createUserMenuItem(user),
      children: [{ text: 'Sign out' }],
    },
  ];
}

private createUserMenuItem(user: User) {
  const item = document.createElement('div');
  item.style.display = 'flex';
  item.style.alignItems = 'center';
  item.style.gap = 'var(--lumo-space-s)';
  render( // Note: import the one from `lit`
    html`
      <span>${user.fullName}</span>
      <vaadin-icon icon="lumo:dropdown"></vaadin-icon>
    `,
    item
  );
  return item;
}

private async userMenuItemSelected(e: MenuBarItemSelectedEvent) {
  if (e.detail.value.text === 'Sign out') {
    await logout(); // Logout on the server
    appStore.logoutUrl && (location.href = appStore.logoutUrl); // Logout on the provider
  }
}
```

Filter out protected views from the menu by modifying the `getMenuRoutes` function:

```typescript
private getMenuRoutes(): RouteInfo[] {
  return views.filter((route) => route.title).filter((route) => hasAccess(route)) as RouteInfo[];
}
```

Try to customize your views further, for example to change the root view to not use `hello-world`, which is protected, or to add a new view.

Now test the application: log in, log out, and try to use the Endpoint by clicking on the "Say hello" button in both cases.

## Add support for Back-Channel Logout

Back-Channel Logout is a feature that enables the provider to close user sessions from outside the application. For example, it can be done from the provider’s user dashboard or from another application.

### Enable the feature in the application

Go back to your `application.properties` file and add the following one:

```properties
hilla.sso.back-channel-logout=true
```

Enable Push support to be able to get logout notifications from the server in real time by adding this line to `vaadin-featureflags.properties`:

```properties
com.vaadin.experimental.hillaPush=true
```

Restart your application to enable Push support.

### Modify the client application

Open `app-store.ts` again and add the following properties:

```typescript
backChannelLogoutEnabled = false;
backChannelLogoutHappened = false;
private logoutSubscription: Subscription<string> | undefined;
```

Add more code to the `fetchAuthInfo` and `clearUserInfo` functions to store values and subscribe to notifications:

```typescript
async fetchAuthInfo() {
  const authInfo = await AuthEndpoint.getAuthInfo();
  this.user = authInfo.user;
  this.logoutUrl = authInfo.logoutUrl;
  this.backChannelLogoutEnabled = authInfo.backChannelLogoutEnabled;

  if (this.user && this.backChannelLogoutEnabled) {
    this.logoutSubscription = await AuthEndpoint.backChannelLogout();

    this.logoutSubscription.onNext(async () => {
      this.backChannelLogoutHappened = true;
    });
  }
}

clearUserInfo() {
  this.user = undefined;
  this.logoutUrl = undefined;
  this.backChannelLogoutHappened = false;

  if (this.logoutSubscription) {
    this.logoutSubscription.cancel();
    this.logoutSubscription = undefined;
  }
}
```

Now, go to `main-layout.ts` and add a Confirm Dialog to notify the user, just above the empty `slot`:

```typescript
import '@vaadin/confirm-dialog';
```

```html
<vaadin-confirm-dialog
  header="Logged out"
  cancel
  @confirm="${() => this.afterLogout(true)}"
  @cancel="${() => this.afterLogout(false)}"
  .opened="${appStore.backChannelLogoutHappened}"
>
  <p>You have been logged out. Do you want to log in again?</p>
  <p>If you click on "Cancel", the application will not work correctly until you log in again.</p>
</vaadin-confirm-dialog>
```

And add the related `afterLogout` function:

```typescript
private async afterLogout(loginAgain: boolean) {
  if (loginAgain) {
    location.href = '/oauth2/authorization/keycloak';
  } else {
    await logout(); // Logout on the server
    appStore.clearUserInfo(); // Logout on the client
  }
}
```

To test this functionality, you need to log into the application, then close your session externally, for example from the Keycloak administration console.
