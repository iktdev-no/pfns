
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