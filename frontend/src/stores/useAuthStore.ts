import { z } from 'zod';
import { create } from 'zustand';
import {parseJwt} from "../utils/parseJwt";

type State = {
  token: string | null,
  refreshToken: string | null,
  user: User | null,
  setToken: (accessToken: string, refreshToken: string) => Promise<void>,
  deleteToken: () => void
}

type User = z.infer<typeof userSchema>;

const userSchema = z.object({
  id: z.number(),
  username: z.string(),
  pictureUrl: z.string().url(),
  email: z.string().email().optional()
});

const tokenSchema = z.object({
  accessToken: z.string(),
  refreshToken: z.string()
});

export const useAuthStore = create<State>(set => ({
  token: null,
  refreshToken: null,
  user: null,
  setToken: async (accessToken: string, refreshToken: string) => {
    const fullToken = `Bearer ${accessToken}`;
    const user = await fetch(`${import.meta.env.VITE_BASE_API_URL}apiV1/user`, {
      headers: {
        Authorization: fullToken
      }
    })
    .then(res => res.json())
    .then(maybeUser => userSchema.parse(maybeUser));

    set({token: fullToken, user, refreshToken});

    const decodedToken = parseJwt(accessToken);
    const now = new Date().getTime();
    const timeToRefresh = decodedToken?.exp - now - 30000;

    setTimeout(async () => {
      const tokenPair = await fetch(`${import.meta.env.VITE_BASE_API_URL}apiV1/refresh`, {
        body: JSON.stringify({
          refreshToken
        })
      })
        .then(res => res.json())
        .then(maybeToken => tokenSchema.parse(maybeToken));

      set({token: tokenPair.accessToken, refreshToken: tokenPair.refreshToken});
    }, timeToRefresh);
  },
  deleteToken: () => set({token: null})
}));