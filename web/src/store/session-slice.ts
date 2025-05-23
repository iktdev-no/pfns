import { createSlice } from "@reduxjs/toolkit";
import { act } from "react";


export interface SessionState {
    name: string | null,
    email: string | null,
    accessToken: string | null,
}

export const initialState: SessionState = {
    name: null,
    email: null,
    accessToken: localStorage.getItem("accessToken") || null,
}

const sessionSlice = createSlice({
    name: "session",
    initialState,
    reducers: {
        setUserInfo(state, action) { 
            state.name = action.payload.name;
            state.email = action.payload.email;
        },
        setAccessToken: (state, action) => {
            state.accessToken = action.payload;
            localStorage.setItem("accessToken", action.payload);
        },
        clearSession: (state) => {
            state.name = null;
            state.email = null;
            state.accessToken = null;
        },
    },
});

export const { setUserInfo, setAccessToken, clearSession } = sessionSlice.actions;
export default sessionSlice.reducer;