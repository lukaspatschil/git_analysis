import {z} from 'zod';

export const repoSchema = z.object({
    id: z.number(),
    name: z.string(),
    url: z.string()
});

export const reposSchema = z.array(repoSchema);