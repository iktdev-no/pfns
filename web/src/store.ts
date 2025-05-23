import { configureStore, ThunkAction, Action } from "@reduxjs/toolkit";
import apiKeySlice from "./store/apiKey-slice";
import sessionSlice from "./store/session-slice";


export const store = configureStore({
  reducer: {
    apiKeys: apiKeySlice,
    session: sessionSlice,
  },
});

export type AppDispatch = typeof store.dispatch;
export type RootState = ReturnType<typeof store.getState>;
export type AppThunk<ReturnType = void> = ThunkAction<
  ReturnType,
  RootState,
  unknown,
  Action<string>
>;