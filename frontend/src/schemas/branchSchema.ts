import {z} from 'zod';

export const branchSchema = z.object({
    name: z.string()
});

export const branchesSchema = z.array(branchSchema);