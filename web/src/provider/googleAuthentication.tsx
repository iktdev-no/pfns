import { GoogleLogin, GoogleOAuthProvider } from '@react-oauth/google';


export default function GoogleSignInButton({ authUrl, googleClientId, onLoginSuccess }: { authUrl: string, googleClientId: string, onLoginSuccess: (token: string) => void }) {
  return (
        <GoogleOAuthProvider clientId={googleClientId}>
            <GoogleLogin
                width={350}
                text='continue_with'
                onSuccess={(credentialResponse) => {
                    const token = credentialResponse.credential;
                    if (token) {
                        const body = JSON.stringify({ token });
                        const url = authUrl;
                        fetch(url, {
                            credentials: "include",
                            method: "POST",
                            headers: {
                            "Content-Type": "text/plain",
                            },
                            body: token,
                        })
                        .then(response => response.text())
                        .then(token => {
                            console.log("Verifisering vellykket");
                            onLoginSuccess(token);
                        })
                        .catch(error => console.error("Feil ved sending av token:", error));
                    }
                }}
                onError={() => {
                    console.error("Login Failed");
                }}
                />
        </GoogleOAuthProvider>
    );
};
