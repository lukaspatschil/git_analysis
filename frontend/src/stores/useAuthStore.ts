import { z } from 'zod';
import { create } from 'zustand';

type State = {
  token: string | null,
  user: z.infer<typeof userSchema> | null,
  setToken: (token: string) => Promise<void>,
  deleteToken: () => void
}

const userSchema = z.object({
  id: z.number(),
  username: z.string(),
  pictureUrl: z.string().url(),
  email: z.string().email().optional()
});

export const useAuthStore = create<State>(set => ({
  token: null,
  user: null,
  setToken: async (token: string) => {
    const fullToken = `Bearer ${token}`;
    const user = await fetch(`${import.meta.env.VITE_BASE_API_URL}apiV1/user`, {
      headers: {
        Authorization: fullToken
      }
    })
    .then(res => res.json())
    .then(maybeUser => userSchema.parse(maybeUser));

    set({token: fullToken, user});
  },
  deleteToken: () => set({token: null})
}));