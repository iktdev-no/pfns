import { useNavigate } from "react-router-dom";


/**
 * Sjekker brukerens autentiseringstilstand og navigerer hvis nødvendig
 */
export function useAuthGuard() {
    const navigate = useNavigate();

    function checkAuthentication() {
        const accessToken = localStorage.getItem("accessToken");

        if (!accessToken) {
            console.log("Ingen token funnet, navigerer til /");
            navigate("/");
            return;
        }
    }

    return { checkAuthentication };
}
