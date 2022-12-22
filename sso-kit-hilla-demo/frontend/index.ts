import { Router } from '@vaadin/router';
import ClientParameters from './generated/dev/hilla/sso/endpoint/ClientParameters';
import { routes } from './routes';
import { appStore } from './stores/app-store';

export const router = new Router(document.querySelector('#outlet'));

// Creates a type for the global Hilla object which is defined in index.html
declare global {
  const Hilla: {
    SSO: ClientParameters;
  };
}

appStore.fetchUserInfo().finally(() => {
  // Ensure router access checks are not done before we know if we are logged in
  router.setRoutes(routes);
});

window.addEventListener('vaadin-router-location-changed', (e) => {
  appStore.setLocation((e as CustomEvent).detail.location);
  const title = appStore.currentViewTitle;
  if (title) {
    document.title = title + ' | ' + appStore.applicationName;
  } else {
    document.title = appStore.applicationName;
  }
});
