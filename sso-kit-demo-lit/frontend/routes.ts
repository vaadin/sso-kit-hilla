import { Route } from '@vaadin/router';
import { appStore } from './stores/app-store';
import './views/about/about-view';
import './views/helloworld/hello-world-view';
import './views/main-layout';

export type ViewRoute = Route & {
  title?: string;
  icon?: string;
  children?: ViewRoute[];
  requiresLogin?: boolean;
  rolesAllowed?: string[];
};

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

export const views: ViewRoute[] = [
  // place routes below (more info https://hilla.dev/docs/routing)
  {
    path: '',
    component: 'hello-world-view',
    icon: '',
    title: '',
  },
  {
    path: 'hello',
    component: 'hello-world-view',
    icon: 'la la-globe',
    title: 'Hello World',
  },
  {
    path: 'about',
    icon: 'la la-file',
    title: 'About',
    action: async (_context, _command) => {
      return hasAccess(_context.route) ? _command.component('about-view') : _command.redirect('login');
    },
    requiresLogin: true,
  },
];
export const routes: ViewRoute[] = [
  {
    path: 'login',
    icon: '',
    title: 'Login',
    action: async (_context, _command) => {
      _command.redirect(appStore.loginLink!);
    },
  },
  {
    path: '',
    component: 'main-layout',
    children: views,
  },
];
