import {z} from 'zod';

export const commitSchema = z.object({
    id: z.string(),
    message: z.string(),
    author: z.string(),
    timestamp: z.string(),
    parentIds: z.array(z.string()),
    mergeCommit: z.boolean(),
    additions: z.number(),
    deletions: z.number(),
    linesOfCodeOverall: z.number()
});

export const commitsSchema = z.array(commitSchema);