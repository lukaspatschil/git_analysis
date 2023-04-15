import {z} from 'zod';

export const committerSchema = z.object({
    name: z.string()
});

export const committersSchema = z.array(committerSchema);