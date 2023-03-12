import {z} from 'zod';

export const commiterSchema = z.object({
    name: z.string()
});

export const commitersSchema = z.array(commiterSchema);