# Hilla SSO Kit

SSO Kit is an add-on for Hilla that provides all the configuration you need to add single sign-on capabilities to your applications.

SSO Kit is built upon the [OpenID Connect](https://openid.net/specs/openid-connect-core-1_0.html) specification and it comes with a [Spring Boot](https://spring.io/projects/spring-boot) starter module that takes care of configuring the security settings you need to authenticate against your identity provider.

These are the currently supported identity providers:

- [Keycloak](https://www.keycloak.org/)
- [Okta](https://okta.com/)
- [Azure Active Directory](https://azure.microsoft.com/en-us/services/active-directory/)

SSO Kit is compatible with [Hilla](https://hilla.dev/) starting from version [2.0](https://github.com/hilla/platform/releases/tag/2.0).

## Getting started

Create a Hilla application using this command:

```bash
npx @hilla/cli init --next sso-kit-hilla-demo
```

### Add dependencies

Add the `sso-kit-starter` module and the other required dependencies to the `pom.xml` of your Vaadin application:

```xml
<dependency>
    <groupId>dev.hilla</groupId>
    <artifactId>sso-kit-hilla-starter</artifactId>
    <version>1.3-SNAPSHOT</version>
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

### Add a Hilla Endpoint

An [AuthEndpoint](sso-kit-hilla-starter/src/main/java/dev/hilla/sso/endpoint/AuthEndpoint.java) is already included in the kit. To use it, you must [enable the new Hilla multi-module engine](https://hilla.dev/docs/lit/reference/configuration/#java-compiler-options). The easiest way to enable it is to create (or modify) the `src/main/resources/vaadin-featureflags.properties` file and add this line:

```properties
com.vaadin.experimental.hillaEngine=true
```

Otherwise, or if you want to customize the returned data, copy the [whole package](https://github.com/vaadin/sso-kit-hilla/tree/main/sso-kit-hilla-starter/src/main/java/dev/hilla/sso/endpoint) into your application and modify it.

Unless you use the same package name as for your application (by default it is `com.example.application` in generated Hilla projects), you have to whitelist your package in Spring Boot for Hilla to be able to find the Endpoint. Open your `Application.java` and add the package to the annotation:

```java
@SpringBootApplication(scanBasePackages = { "com.example.application", "dev.hilla.sso.starter", "dev.hilla.sso.endpoint" })
```

Also, remove the `@AnonymousAllowed` annotation in `HelloWorldEndpoint.java` with `@PermitAll`, so that unauthenticated users will be unable to use that Endpoint.

### Configure the SSO provider in Spring

As a provider is needed, let's suppose you have a local Keycloak running on your machine on port 8081. Get a realm name, a client name, and the client secret and add those values to your `application.properties` file:

```properties
spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:8081/realms/your-realm
spring.security.oauth2.client.registration.keycloak.client-id=your-client
spring.security.oauth2.client.registration.keycloak.client-secret=your-secret
spring.security.oauth2.client.registration.keycloak.scope=profile,openid,email,roles
hilla.sso.login-route=/oauth2/authorization/keycloak
```

### Use the Endpoint

Start the application using the `./mvnw` command (`.\mvnw` on Windows), so that Hilla generates TypeScript files.

Inside the `AppStore` class in `app-store.ts` add this code:

```typescript
user: User | undefined = undefined;

async fetchUserInfo() {
  this.user = await AuthEndpoint.getAuthenticatedUser();
}

clearUserInfo() {
  this.user = undefined;
}

get loggedIn() {
  return !!this.user;
}

isUserInRole(role: string) {
  return this.user?.roles?.includes(role);
}
```

You should be able to add the missing imports automatically.

Open the `frontend/index.ts` file and add this TypeScript declaration to allow easier access to configuration parameters:

```typescript
// Creates a type for the global Hilla object which is defined in index.html
declare global {
  const Hilla: {
    SSO: ClientParameters;
  };
}
```

Also, delay the router setup until the login information has been fetched by wrapping the `setRoutes` call as follows:

```typescript
appStore.fetchUserInfo().finally(() => {
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
    location.href = Hilla.SSO.loginURL;
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
    : html`<a router-ignore href="login">Sign in</a>`}
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
  render(
    html`
      <span>${user.name}</span>
      <vaadin-icon icon="lumo:dropdown"></vaadin-icon>
    `,
    item
  );
  return item;
}

private async userMenuItemSelected(e: MenuBarItemSelectedEvent) {
  if (e.detail.value.text === 'Sign out') {
    appStore.clearUserInfo();
    await logout();
    location.href = '';
  }
}
```

Filter out protected views from the menu by modifying the `getMenuRoutes` function:

```typescript
private getMenuRoutes(): RouteInfo[] {
  return views.filter((route) => route.title).filter((route) => hasAccess(route)) as RouteInfo[];
}
```

Try to customize your views further, for example to change the root view to not use `hello-world`, or to add a new view.
