import { configureStore, ThunkAction, Action } from "@reduxjs/toolkit";
import apiKeySlice from "./store/apiKey-slice";


export const store = configureStore({
  reducer: {
    apiKeys: apiKeySlice,
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