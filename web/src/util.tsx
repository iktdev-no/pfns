
export async function apiFetch(input: RequestInfo, init?: RequestInit, useCredentials: boolean = false, onUnauthorized?: () => void): Promise<Response> {
    const accessToken = localStorage.getItem("accessToken");
    const response = await fetch(input, {
        ...init,
        credentials: useCredentials ? "include" : "same-origin",
        headers: {
            ...init?.headers,
            ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
        },
    });

    if (response.status === 401 && useCredentials) {
        console.error("Unauthorized request, redirecting to login");
        onUnauthorized?.();
        return Promise.reject(new Error("Unauthorized"));
    }

    return response;
}

export const getBackendUrl = (): string => {
    const { protocol, hostname } = window.location;
    const port = (hostname === "localhost" || hostname === "127.0.0.1") ? ":8080" : "";
    return `${protocol}//${hostname}${port}`;
}

export function logWhenDebug(message: string, ...optionalParams: any[]) {
    const { hostname } = window.location;
    if (hostname === "localhost" || hostname === "127.0.0.1") {
        // Print stack trace after log
        console.log(message, ...optionalParams);
        console.trace();
    }
}