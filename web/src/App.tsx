import React from 'react';
import logo from './logo.svg';
import './App.css';
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
//import Dashboard from "./pages/Dashboard";
import { AuthProvider } from "./contexts/AuthContext";
import { Box, createTheme, ThemeProvider } from '@mui/material';
import Header from './component/Header';
import CreateToken from './pages/CreateToken';
import Home from './pages/Home';
//import PrivateRoute from "./components/PrivateRoute";
//          <Route path="/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />

const darkTheme = createTheme({
  palette: {
    mode: "dark", // Aktiverer dark mode
    primary: {
      main: "#90caf9",
    },
    secondary: {
      main: "#f48fb1",
    },
    background: {
      default: "#121212",
      paper: "#1e1e1e",
    },
    text: {
      primary: "#ffffff",
      secondary: "#b0bec5",
    },
  },
});

const App = () => {
  return (
    <ThemeProvider theme={darkTheme}>
      <AuthProvider>
        <Router>
          <Box sx={{ display: "flex", flexDirection: "column", height: "100vh" }}>
            <Header />
            <Box sx={{ display: "flex", flexGrow: 1, justifyContent: "center", alignItems: "center" }}>
              <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/createToken" element={<CreateToken />} />
              </Routes>
            </Box>
          </Box>
        </Router>

      </AuthProvider>
    </ThemeProvider>
  );
};

export default App;
