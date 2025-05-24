import { JSX, useCallback, useEffect } from "react";
import "firebaseui/dist/firebaseui.css";
import GoogleSignInButton from "../provider/googleAuthentication";
import { Box, Button, CircularProgress, Typography } from "@mui/material";
import { EnvProperties, envProperties } from "../values";
import { RootState, store } from "../store";
import {ReactComponent as Logo} from "../assets/Pfns.svg"
import { useAuth } from "../contexts/AuthContext";
import { log } from "console";
import ApiKeysTable from "../component/ApiTokenTable";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { ApiKeyObjects, setKeys } from "../store/apiKey-slice";
import { apiFetch, logWhenDebug } from "../util";


function LoginPage({props, onLoginSuccess}: {props: EnvProperties, onLoginSuccess: (token: string) => void}): JSX.Element { 
  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "center", 
        alignItems: "center",
        height: "100vh",
        width: "100vw",
      }}
    >
      <Box sx={{ backgroundColor: "#0E0E0E",  borderRadius: "10px", padding: "20px", width: "400px" }}>
          <Logo style={{
            marginTop: -25,
            marginLeft: -20,
            marginBottom: -30,
            width: 150,
            height: "auto",
          }} />
          <Typography variant="h3" gutterBottom>Login</Typography>
          <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "300px" }}>
            <GoogleSignInButton
              authUrl={props.google.authUrl}
              googleClientId={props.google.clientId}
              onLoginSuccess={onLoginSuccess}
            />
          </Box>
      </Box>
    </Box>
  );
}

function AuthorizingPage({props, cancel}: {props: EnvProperties, cancel: () => void}): JSX.Element { 
  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "center", 
        alignItems: "center",
        height: "100vh",
        width: "100vw",
      }}
    >
      <Box sx={{ backgroundColor: "#0E0E0E",  borderRadius: "10px", padding: "20px", width: "400px" }}>
          <Logo style={{
            marginTop: -25,
            marginLeft: -20,
            marginBottom: -30,
            width: 150,
            height: "auto",
          }} />
          <Typography variant="h3" gutterBottom>Authenticating</Typography>
          <Box sx={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            flexDirection: "column",
            textAlign: "center",
          }}>
            <CircularProgress size={100} sx={{ marginTop: 10, marginBottom: 10 }} />
            <Button
              variant="contained"
              color="error"
              onClick={cancel}>Cancel</Button>

          </Box>
      </Box>
    </Box>
  );
}

function OverviewComponent({ apiKeys }: { apiKeys: ApiKeyObjects }): JSX.Element {
  const dispatch = useDispatch();
  const props = envProperties;
  const navigate = useNavigate();

  const getTokens = useCallback((onData: (items: ApiKeyObjects) => void) => {
    apiFetch(`${props.backendUrl}/api/web/token/all`, { method: "GET" }, false, () => {
      window.location.href = "/";
    })
      .then(response => response.status === 200 ? response.json() : [])
      .then(onData)
      .catch(() => {});
  }, [props.backendUrl]);

  useEffect(() => {
    getTokens(data => dispatch(setKeys(data)));
  }, [getTokens, dispatch]);

  const deleteToken = (serverId: string) => {
    apiFetch(`${props.backendUrl}/api/web/token`, {
      method: "DELETE",
      body: serverId
    }, true, () => {
      window.location.href = "/";
    })
      .then(response => response.status === 200 ? response.text() : null)
      .then(() => getTokens(data => dispatch(setKeys(data))))
      .catch(() => {});
  };

  return (
    <Box sx={{ backgroundColor: "#0E0E0E", borderRadius: "10px", padding: "20px", width: "80vw" }}>
      <Box sx={{ display: "flex", flexDirection: "row", justifyContent: "center", alignItems: "center" }}>
        <Typography variant="h5">Api Keys</Typography>
        <Button sx={{ alignSelf: "flex-end", marginLeft: "auto" }} variant="contained" color="success" onClick={() => navigate("/createToken")}>
          New token
        </Button>
      </Box>
      <Box sx={{ display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center" }}>
        {apiKeys.items.length === 0 ? (
          <Typography sx={{ marginTop: "100px", marginBottom: "100px", color: "gray" }}>
            None api tokens found
          </Typography>
        ) : (
          <ApiKeysTable apiKeys={apiKeys.items} onDelete={deleteToken} />
        )}
      </Box>
    </Box>
  );
}

export default function Home() {
  const { token, thridPartySignInAccepted, authorized, logout } = useAuth();
  const apiKeys: ApiKeyObjects = useSelector((state: RootState) => state.apiKeys);
  const props = envProperties;
  

  const onLoginSuccess = (accessToken: string) => {
    logWhenDebug("Login success, access token:", accessToken);
    thridPartySignInAccepted(accessToken);
  }

  return (<>
    {token === null && (
      <LoginPage props={props} onLoginSuccess={onLoginSuccess} />
    )}
    {token && !authorized && (
      <AuthorizingPage props={props} cancel={logout} />
    )}
    {token && authorized && (
      <OverviewComponent apiKeys={apiKeys} />
    )}
  </>);
}

