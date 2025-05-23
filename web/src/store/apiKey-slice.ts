import { createSlice } from "@reduxjs/toolkit";

export interface ApiKeyObject {
    serverId: string
    email: string
    createdAt: number
    ip: string
    revoked: boolean
    usage: number
}

export interface ApiKeyObjects {
    items: Array<ApiKeyObject>
}

const initialState: ApiKeyObjects = {
    items : [],
}


const apiKeySlice = createSlice({
    name: "apiKey",
    initialState,
    reducers: {
        setKeys: (state, action) => {
            state.items = action.payload;
        },
        clearKeys: (state) => {
            state.items = [];
        },
    },
});

export const { setKeys, clearKeys } = apiKeySlice.actions;
export default apiKeySlice.reducer;