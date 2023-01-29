import { create } from 'zustand';

type State = {
  token: string | null,
  setToken: (token: string) => void,
  deleteToken: () => void
}

export const useAuthStore = create<State>(set => ({
  token: null,
  setToken: (token: string) => set(() => ({token: `Bearer ${token}`})),
  deleteToken: () => set(() => ({token: null}))
}));