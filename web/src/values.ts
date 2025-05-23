import exp from "constants";

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
    backendUrl: getEnvVariable("REACT_APP_BACKEND_API_URL"),
    google: {
        clientId: getEnvVariable("REACT_APP_GOOGLE_CLIENT_ID"),
        authUrl: getEnvVariable("REACT_APP_BACKEND_API_URL") + "/api/web/auth/login/google",
    }
}