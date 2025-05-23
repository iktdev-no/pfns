import { JSX, useCallback, useEffect } from "react";
import "firebaseui/dist/firebaseui.css";
import GoogleSignInButton from "../provider/googleAuthentication";
import { Box, Button, Typography } from "@mui/material";
import { EnvProperties, envProperties } from "../values";
import { RootState, store } from "../store";
import { setAccessToken } from "../store/session-slice";
import {ReactComponent as Logo} from "../assets/Pfns.svg"
import { useAuth } from "../contexts/AuthContext";
import { log } from "console";
import ApiKeysTable from "../component/ApiTokenTable";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { useAuthGuard } from "../authGuard";
import { ApiKeyObjects, setKeys } from "../store/apiKey-slice";
import { apiFetch } from "../util";


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

function OverviewComponent({apiKeys}: {apiKeys: ApiKeyObjects}): JSX.Element {
    const dispatch = useDispatch();
    const props = envProperties;
    const naviage = useNavigate();

    const getTokens = useCallback((onData: (items: ApiKeyObjects) => void) => {
        const token = localStorage.getItem("accessToken");
        apiFetch(`${props.backendUrl}/api/web/token/all`, {
            method: "GET",
        }, false, () => {
            window.location.href = "/";
        })
            .then(response => response.json())
            .then(data => {
                console.log("Data received:", data);
                onData(data);
            })
            .catch(error => { });
    }, [props.backendUrl]);

    useEffect(() => {

        getTokens((data: ApiKeyObjects) => {
            console.log("Data received:", data);
            dispatch(setKeys(data));
        });
    }, [getTokens, dispatch])


    const deleteToken = (serverId: string) => {
        apiFetch(`${props.backendUrl}/api/web/token`, {
            method: "DELETE",
            body: serverId
        }, true, () => {
            window.location.href = "/";
        })
            .then((response) => {
                if (response.status !== 200) {
                    console.error("Error deleting token:", response.statusText);
                    return null;
                }
                return response.text();
            })
            .then(() => {
                console.log("Token deleted!");
                getTokens((data: ApiKeyObjects) => {
                    dispatch(setKeys(data));
                });
            })
            .catch((error) => {
                console.error("Error deleting token:", error);
            });
    };

    const onNewTokenClick = () => {
        naviage("/createToken");
    };

  return (
    <Box sx={{ backgroundColor: "#0E0E0E", borderRadius: "10px", padding: "20px", width: "80vw" }}>
        <Box sx={{ display: "flex", flexDirection: "row", justifyContent: "center", alignItems: "center", alignContent: "center" }}>
            <Typography variant="h5">Api Keys</Typography>
            <Button sx={{ alignSelf: "flex-end", marginLeft: "auto" }} variant="contained" color="success" onClick={onNewTokenClick}>New token</Button>
        </Box>
        <Box sx={{ display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center", alignContent: "center" }}>
            {apiKeys.items.length === 0 ? (<Typography sx={{
                marginTop: "100px",
                marginBottom: "100px",
                color: "gray",
            }}>None api tokens found</Typography>)
                :
                <ApiKeysTable apiKeys={apiKeys.items} onDelete={deleteToken} />
            }
        </Box>
    </Box>
);
}

export default function Home() {
  const { token, login, logout, loginWithNewAccessToken } = useAuth();
  const apiKeys: ApiKeyObjects = useSelector((state: RootState) => state.apiKeys);
  const props = envProperties;
  

  const onLoginSuccess = (accessToken: string) => {
    console.log("Login success, access token:", accessToken);
    loginWithNewAccessToken(accessToken);
  }

  return (<>
    {token === null && (
      <LoginPage props={props} onLoginSuccess={onLoginSuccess} />
    )}
    {token && (
      <OverviewComponent apiKeys={apiKeys} />
    )}
  </>);
}

