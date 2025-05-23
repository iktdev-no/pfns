import { Table, TableHead, TableRow, TableCell, TableBody, Button } from "@mui/material";
import { ApiKeyObject } from "../store/apiKey-slice";
import exp from "constants";


const ApiKeysTable = ({ apiKeys, onDelete }: { apiKeys: ApiKeyObject[], onDelete: (serverId: string) => void }) => {
    return (
        <Table sx={{ color: "white" }}>
            <TableHead>
                <TableRow>
                    <TableCell>Server ID</TableCell>
                    <TableCell>Email</TableCell>
                    <TableCell>Opprettet</TableCell>
                    <TableCell>IP</TableCell>
                    <TableCell>Revoked</TableCell>
                    <TableCell>Usage</TableCell>
                    <TableCell>Actions</TableCell>
                </TableRow>
            </TableHead>
            <TableBody style={{ color: "white" }}>
                {apiKeys.map((key, index) => (
                    <TableRow key={index}>
                        <TableCell>{key.serverId}</TableCell>
                        <TableCell>{key.email}</TableCell>
                        <TableCell>
                            {
                                new Date(key.createdAt).toLocaleString("nb-NO",
                                    {
                                        year: "numeric",
                                        month: "2-digit",
                                        day: "2-digit",
                                        hour: "2-digit",
                                        minute: "2-digit",
                                        hour12: false,  
                                    })
                            }
                        </TableCell>
                        <TableCell>{key.ip}</TableCell>
                        <TableCell>{key.revoked ? "Yes" : "No"}</TableCell>
                        <TableCell>{key.usage}</TableCell>
                        <TableCell>
                            <Button variant="contained" color="error" onClick={() => onDelete(key.serverId)}>Delete</Button>
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
};

export default ApiKeysTable;