import React, { createContext, useContext, useState, useEffect } from "react";
import { envProperties, localStorageKeys } from "../values";
import { logWhenDebug } from "../util";
import { get } from "http";
import { log } from "console";
import { useNavigate } from "react-router-dom";

interface AuthContextUser {
  name: string | null;
  email: string | null;
}

interface AuthContextType {
  token: string | null;
  authorized: boolean;
  user: AuthContextUser | null;
  logout: () => void;
  thridPartySignInAccepted: (newAccessToken: string) => void;
}


const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(localStorage.getItem(localStorageKeys.accessToken));
  const [authorized, setAuthorized] = useState<boolean>(false);
  const [user, setUser] = useState<AuthContextUser | null>(null);

  const updateToken = (newToken: string | null) => {
    if (newToken === null) {
      setToken(null);
      setUser(null);
      localStorage.removeItem(localStorageKeys.accessToken);
      return;
    } else {
      localStorage.setItem(localStorageKeys.accessToken, newToken);
      if (newToken !== token) {
        setToken(newToken);
      }
    }
  }

  const refreshToken = async () => {
    await fetch(`${envProperties.backendUrl}/webapi/auth/refresh`, { 
        method: "POST", 
        credentials: "include" 
      })
      .then((response) => response.text())
      .then((token) => {
        console.log("Token verification successful on Refresh");
        if (token) {
          updateToken(token);
        }
      })
      .catch((error) => {
        console.error("Error refreshing token", error);
        logout();
        localStorage.removeItem(localStorageKeys.accessToken);
    });
  };
  

  useEffect(() => {
    if (!token) return;

    const interval = setInterval(() => {
      refreshToken();
    }, 15 * 60 * 1000); // Forny hvert 15. minutt

    return () => clearInterval(interval);
  }, [token]);

  const thridPartySignInAccepted = (newAccessToken: string) => { 
     setToken(newAccessToken);
  }
  
  const simpleChecksum = (str: string): number =>
    Array.from(str).reduce((sum, char) => (sum + char.charCodeAt(0)) % 100000, 0);


  useEffect(() => {
    if (token) {
      logWhenDebug("Token changed, logging in with new token", {token});
      logWhenDebug("Checksum of token:", simpleChecksum(token));
      login();
    }
  }, [token]);

  const login = () => {
    if (!token) {
      console.error("No login token found");
      return;
    }

    fetch(`${envProperties.backendUrl}/webapi/auth/login`, {
        credentials: "include",
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        }
      })
      .then((response) => {
        if (response.status === 200) {
          return response.text();
        } else {
          setAuthorized(false);
          throw new Error(`Unexpected status code: ${response.status}`);
        }
      })
      .then((token) => {
        logWhenDebug("Token verification successful", token);
        updateToken(token);
        setTimeout(() => setAuthorized(true), 500); // 500 ms delay
      })
      .catch((error) => {
        localStorage.removeItem(localStorageKeys.accessToken);
        setAuthorized(false);
      });
  };



  const logout = () => {
    localStorage.removeItem(localStorageKeys.accessToken);
    fetch(`${envProperties.backendUrl}/webapi/auth/logout`, {
        method: "GET",
        credentials: "include",
      })
      .catch((error) => {});
      updateToken(null);
    window.location.href = "/";
  };

  return (
    <AuthContext.Provider value={{ token, logout, thridPartySignInAccepted, user, authorized }}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * Sjekker brukerens autentiseringstilstand og navigerer hvis nødvendig
 */
export function useAuthGuard() {
    const navigate = useNavigate();

    function CheckAuthentication() {
        const { token } = useContext(AuthContext) || {};

        if (!token) {
            console.log("Ingen token funnet, navigerer til /");
            navigate("/");
            return;
        }
    }

    return { CheckAuthentication };
}


export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within an AuthProvider");
  return context;
};
