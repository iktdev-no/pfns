import { getBackendUrl } from "./util";


export interface LocalStorageKeys {
    accessToken: string;
}

export const localStorageKeys: LocalStorageKeys = {
    accessToken: "accessToken"
}

export interface EnvProperties {
    backendUrl: string;
    google: {
        clientId: string;
        authUrl: string;
    }
}

const getEnvVariable = (key: string): string => {
    const value = process.env[key];
    if (!value) {
        throw new Error(`${key} is not defined in the environment variables.`);
    }
    return value;
};

export const envProperties: EnvProperties = {
    backendUrl: getBackendUrl(),
    google: {
        clientId: getEnvVariable("REACT_APP_GOOGLE_CLIENT_ID"),
        authUrl: getBackendUrl() + "/webapi/auth/login/google",
    }
}
