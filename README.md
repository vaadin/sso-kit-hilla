# Hilla SSO Kit

SSO Kit is an add-on for Hilla that provides all the configuration you need to add single sign-on capabilities to your applications.

SSO Kit is built upon the [OpenID Connect](https://openid.net/specs/openid-connect-core-1_0.html) specification and it comes with a [Spring Boot](https://spring.io/projects/spring-boot) starter module that takes care of configuring the security settings you need to authenticate against your identity provider.

These are the currently supported identity providers:

- [Keycloak](https://www.keycloak.org/)
- [Okta](https://okta.com/)
- [Azure Active Directory](https://azure.microsoft.com/en-us/services/active-directory/)

SSO Kit is compatible with [Hilla](https://hilla.dev/) starting from version [2.0](https://github.com/hilla/platform/releases/tag/2.0).

## Getting Started

To get started with SSO Kit you just need to add the `sso-kit-starter` module as a dependency to your Vaadin application, e.g. in your `pom.xml`:

```xml
<dependency>
    <groupId>dev.hilla</groupId>
    <artifactId>sso-kit-hilla</artifactId>
</dependency>
```
