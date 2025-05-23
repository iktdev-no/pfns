import React, { createContext, useContext, useState, useEffect } from "react";
import { envProperties, localStorageKeys } from "../values";

interface AuthContextUser {
  name: string | null;
  email: string | null;
}

interface AuthContextType {
  token: string | null;
  user: AuthContextUser | null;
  login: (newToken: string) => void;
  logout: () => void;
  loginWithNewAccessToken: (newAccessToken: string) => void;
}


const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(localStorage.getItem(localStorageKeys.accessToken));
  const [user, setUser] = useState<AuthContextUser | null>(null);

  useEffect(() => {
    if (token) {
      localStorage.setItem(localStorageKeys.accessToken, token);
    } else {
      localStorage.removeItem(localStorageKeys.accessToken);
    }
  }, [token]);


  const refreshToken = async () => {
    await fetch(`${envProperties.backendUrl}/api/web/auth/refresh`, { 
        method: "POST", 
        credentials: "include" 
      })
      .then((response) => response.text())
      .then((token) => {
        console.log("Token verification successful");
        if (token) {
          setToken(token);
          localStorage.setItem(localStorageKeys.accessToken, token);
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

  const loginWithNewAccessToken = (newAccessToken: string) => { 
    setToken(newAccessToken);
    login();
  }

  const login = () => {
    fetch(`${envProperties.backendUrl}/api/web/auth/login`, {
        credentials: "include",
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        }
      })
      .then((response) => response.text())
      .then((token) => {
        console.log("Token verification successful");
      })
      .catch((error) => {
        localStorage.removeItem(localStorageKeys.accessToken);
      });
  };



  const logout = () => {
    localStorage.removeItem(localStorageKeys.accessToken);
    fetch(`${envProperties.backendUrl}/api/web/auth/logout`, {
        method: "GET",
        credentials: "include",
      })
      .catch((error) => {});
    setToken(null);
    window.location.href = "/";
  };

  return (
    <AuthContext.Provider value={{ token, login, logout, loginWithNewAccessToken, user }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within an AuthProvider");
  return context;
};
