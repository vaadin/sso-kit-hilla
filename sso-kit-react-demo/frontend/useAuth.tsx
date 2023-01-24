import { Subscription } from "@hilla/frontend";
import { createContext, Dispatch, SetStateAction, useState } from "react";
import User from "./generated/dev/hilla/sso/endpoint/User";
import Message from "./generated/dev/hilla/sso/starter/BackChannelLogoutSubscription/Message";
import type SingleSignOnData from "./generated/dev/hilla/sso/starter/SingleSignOnData";
import { BackChannelLogoutEndpoint, SingleSignOnEndpoint, UserEndpoint } from "./generated/endpoints";

export type AuthState = Readonly<SingleSignOnData & {
    user?: User,
    backChannelLogout?: Subscription<Message>,
    backChannelLogoutHappened: boolean,
}>;

type AccessCheck = ({ handle }: { handle: AccessProps }) => boolean;

export type Authentication = Readonly<{
    state: AuthState;
    clearUserInfo: () => void;
    onBackChannelLogout: () => void;
    hasAccess: AccessCheck;
}>;

export const initialState = await (async () => {
    const authInfo = await SingleSignOnEndpoint.getData();
    const backChannelLogout = authInfo.backChannelLogoutEnabled
        ? BackChannelLogoutEndpoint.subscribe() : undefined;

    return {
        user: await UserEndpoint.getAuthenticatedUser(),
        backChannelLogout,
        backChannelLogoutHappened: false,
        ...authInfo,
    };
})();

export type AccessProps = Readonly<{
    requiresLogin?: boolean;
    rolesAllowed?: readonly string[];
}>;

const hasAccess = (state: AuthState): AccessCheck => {
    return ({ handle }: { handle: AccessProps }) => {
        const requiresAuth = handle.requiresLogin || handle.rolesAllowed;
        if (!requiresAuth) {
            return true;
        }

        if (!state.authenticated) {
            return false;
        }

        if (handle.rolesAllowed) {
            return handle.rolesAllowed.some((allowedRole) => state.roles.includes(allowedRole));
        }

        return true;
    }
}

export const AuthContext = createContext<Authentication>({
    state: initialState,
    clearUserInfo: () => { },
    onBackChannelLogout: () => { },
    hasAccess: hasAccess(initialState),
});

export const useAuth = (state: AuthState, setState: Dispatch<SetStateAction<AuthState>>): Authentication => {
    return {
        state,
        clearUserInfo: () => {
            setState({
                ...state,
                user: undefined,
                logoutUrl: undefined,
                authenticated: false,
                roles: [],
                backChannelLogout: undefined,
                backChannelLogoutHappened: false,
                backChannelLogoutEnabled: false,
            });
        },
        onBackChannelLogout: () => {
            setState({
                ...state,
                backChannelLogoutHappened: true,
            });
        },
        hasAccess: hasAccess(state),
    };
}
