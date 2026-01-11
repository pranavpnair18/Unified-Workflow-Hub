import { RootState } from "./index";
export const selectNotifications = (state: RootState) => state.notifications;
export const selectTotalCount = (state: RootState) => {
  const { github, gmail, calendar, customApi } = state.notifications;
  return github + gmail + calendar + customApi;
};
