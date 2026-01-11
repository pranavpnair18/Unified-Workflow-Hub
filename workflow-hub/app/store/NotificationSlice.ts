import { createSlice, PayloadAction } from "@reduxjs/toolkit";

type NotificationsState = {
  github: number;
  gmail: number;
  calendar: number;
  customApi: number;
};

const initialState: NotificationsState = {
  github: 0,
  gmail: 0,
  calendar: 0,
  customApi: 0,
};

const notificationsSlice = createSlice({
  name: "notifications",
  initialState,
  reducers: {
    setNotifications: (state, action: PayloadAction<NotificationsState>) => {
      return action.payload;
    },
  },
});

export const { setNotifications } = notificationsSlice.actions;
export default notificationsSlice.reducer;
