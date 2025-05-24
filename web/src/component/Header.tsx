import { Box, Button, IconButton, Typography } from "@mui/material";
import { useSelector } from "react-redux";
import { RootState } from "../store";
import { envProperties } from "../values";
import {ReactComponent as Logo} from "../assets/Pfns.svg"
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";


export default function Header() {
  const { token, logout, authorized } = useAuth();
    const naviage = useNavigate();
    
    if (token === null || !authorized) {
        return (<></>)
    }

    return (
        <Box sx={{
            display: "flex",
            flexDirection: "row",
            alignItems: "center",
            height: "64px",
            backgroundColor: "#030303",
        }} >

            <Box sx={{ fontSize: "24px", fontWeight: "bold" }}>
                <Button sx={{ height: "64px", paddingLeft: 2.5 }} onClick={() => naviage("/")}>
                    <Logo style={{ width: 100, height: 100, marginLeft: -20, marginBottom: -20 }} />
                </Button>
            </Box>
            <Box sx={{ height: "64px", alignSelf: "flex-end", marginLeft: "auto", }}>
                <Button sx={{ height: "64px", paddingRight: "20px", paddingLeft: "20px" }}
                    onClick={logout}
                >
                    <Typography sx={{ color: "#FFFFFF" }}>Log out</Typography>
                </Button>
            </Box>
        </Box>
    );

}