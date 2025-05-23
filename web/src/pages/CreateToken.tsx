import { useDispatch } from "react-redux";
import { useAuthGuard } from "../authGuard";
import { envProperties } from "../values";
import { Box, Typography, Button, Input } from "@mui/material";
import { use, useState } from "react";
import { apiFetch } from "../util";


export default function CreateToken() {
    const dispatch = useDispatch();
    const { checkAuthentication } = useAuthGuard();
    const props = envProperties;
    const [serverId, setServerId] = useState<string | null>(null);
    const [apiToken, setApiToken] = useState<string | null>(null);
    const [apiTokenError, setApiTokenError] = useState<string | null>(null);

    const createNewToken = (serverId: string | null) => {
        if (serverId === null) {
            console.error("Server ID is null");
            return;
        }
        apiFetch(`${props.backendUrl}/api/web/token/create`, {
            method: "POST",
            body: serverId
        }, true, () => {
            window.location.href = "/";
        })
            .then((response) => {
                if (response.status !== 200) {
                    console.error("Error creating token:", response.statusText);
                    if (response.status === 409) {
                        setApiTokenError("Token already exists for this serverId");
                    }
                    return null;
                }
                return response.text();
            })
            .then((token) => {
                console.log("Token created!");
                setApiToken(token);
            })
            .catch((error) => {
                console.error("Error creating token:", error);
            });
    };

    return (
        <Box sx={{ display: "flex", flexDirection: "row", justifyContent: "center", alignItems: "center", maxWidth: "1200px" }}>
            <Box sx={{ width: "40vw", marginRight: "4vw" }}>
                <Typography variant="h5">Api token</Typography>
                <br />
                <Typography variant="body1" sx={{ marginBottom: 2, textAlign: "start" }}>
                    Pfns Api Tokens are used to authenticate and authorize requests to the Pfns Api, and perform operations that communicates over the FCM (Firebase Cloud Messaging).
                    <br />
                    <br />
                    This setup is used to allow individual instances of the Streamit backend to communicate with the applications and web, both for notification and for remote setup.
                    <br />
                    <br />
                    Creation of API tokens is limited to the serverId, which is used to identify the server instance that is being used.
                    <br />
                    <br />
                    <strong>There can only be one API token per serverId.</strong>
                </Typography>
            </Box>

            <Box sx={{  width: "40vw", }}>
                <Box sx={{ backgroundColor: "#0E0E0E",  borderRadius: "10px", padding: "20px" }}>
                    <Box sx={{ display: "flex", flexDirection: "row", marginBottom: 5 }}>
                        <Typography variant="h5">Create Api token</Typography>
                    </Box>
                    <Box sx={{ display: "flex", flexDirection: "column", }}>
                        <Input placeholder="Server ID" sx={{ marginRight: "10px", backgroundColor: "#000", borderRadius: "5px", padding: "5px", marginBottom: 2 }}
                        onChange={(event) => setServerId(event.target.value)}
                        />
                        {apiTokenError && (
                            <Typography variant="body1" sx={{ color: "red", marginBottom: 2 }}>
                                {apiTokenError}
                            </Typography>
                        )}
                        <Button variant="contained" color="success" onClick={() => createNewToken(serverId)}>Create new token</Button>
                    </Box>
                </Box>
                {apiToken !== null && (
                    <Box sx={{ backgroundColor: "#0E0E0E", borderRadius: "10px", padding: "20px", marginTop: 2 }}>
                        <Typography variant="h5">Token created!</Typography>
                        <Typography variant="body1" sx={{ marginTop: 2 }}>
                            Your new API token is:
                        </Typography>
                        <hr />
                        <Typography variant="body1" sx={{lineBreak: "anywhere"}}>{apiToken}</Typography>
                        <hr />
                        <Typography variant="body1" sx={{ marginTop: 2 }}>
                            Make sure to save it somewhere safe, as it will <strong>never</strong> be shown again.
                        </Typography>
                    </Box>
                )}
            </Box>
        </Box>
    )
}