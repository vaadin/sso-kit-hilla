import { logout as _logout } from '@hilla/frontend';
import { AppLayout } from '@hilla/react-components/AppLayout.js';
import { Button } from '@hilla/react-components/Button.js';
import { ConfirmDialog } from '@hilla/react-components/ConfirmDialog.js';
import { DrawerToggle } from '@hilla/react-components/DrawerToggle.js';
import { Item } from '@hilla/react-components/Item.js';
import { Scroller } from '@hilla/react-components/Scroller.js';
import Placeholder from 'Frontend/components/placeholder/Placeholder.js';
import { MenuProps, routes, useViewMatches, ViewRouteObject } from 'Frontend/routes.js';
import { AuthContext } from 'Frontend/useAuth.js';
import { Suspense, useContext, useEffect } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import css from './MainLayout.module.css';

type MenuRoute = ViewRouteObject &
  Readonly<{
    path: string;
    handle: Required<MenuProps>;
  }>;

export default function MenuOnLeftLayout() {
  const { state, hasAccess, onBackChannelLogout, clearUserInfo } = useContext(AuthContext);
  const matches = useViewMatches();

  useEffect(() => {
    if (state.backChannelLogout) {
      state.backChannelLogout.onNext(() => {
        onBackChannelLogout();
      });
    }
  }, []);

  const currentTitle = matches[matches.length - 1]?.handle?.title ?? 'Unknown';

  const menuRoutes = (routes[0]?.children || []).filter(
    (route) => route.path && route.handle && route.handle.icon && route.handle.title
  ) as readonly MenuRoute[];

  async function logout() {
    await _logout();
    location.href = state.logoutUrl!;
  }

  async function loginAgain() { 
    await _logout();
    location.href = state.loginUrl;
  }

  async function stayOnPage() { 
    await _logout();
    clearUserInfo();
  }

  return (
    <AppLayout className="block h-full" primarySection="drawer">
      <header slot="drawer">
        <h1 className="text-l m-0">My App</h1>
      </header>
      <Scroller slot="drawer" scroll-direction="vertical">
        <nav>
          {menuRoutes.filter(hasAccess).map(({ path, handle: { icon, title } }) => (
            <NavLink
              className={({ isActive }) => `${css.navlink} ${isActive ? css.navlink_active : ''}`}
              key={path}
              to={path}
            >
              {({ isActive }) => (
                <Item key={path} selected={isActive}>
                  <span className={`${icon} ${css.navicon}`} aria-hidden="true"></span>
                  {title}
                </Item>
              )}
            </NavLink>
          ))}
        </nav>
      </Scroller>
      <footer slot="drawer">
        {state.user ? (
          <>
            <div className="flex items-center gap-m">
              {state.user.fullName}
            </div>
            <Button onClick={logout}>Sign out</Button>
          </>
        ) : (
          <a href={state.loginUrl}>Sign in</a>
        )}
      </footer>

      <DrawerToggle slot="navbar" aria-label="Menu toggle"></DrawerToggle>
      <h2 slot="navbar" className="text-l m-0">
        {currentTitle}
      </h2>

      <ConfirmDialog header='Logged out' cancel opened={!!state.backChannelLogoutHappened}
        onConfirm={loginAgain} onCancel={stayOnPage}>
        <p>You have been logged out. Do you want to log in again?</p>
      </ConfirmDialog>

      <Suspense fallback={<Placeholder />}>
        <Outlet />
      </Suspense>
    </AppLayout>
  );
}
