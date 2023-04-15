import {z} from "zod";

export const statSchema = z.object({
    committer: z.string(),
    numberOfCommits: z.number(),
    numberOfAdditions: z.number(),
    numberOfDeletions: z.number()
});

export const statsSchema = z.array(statSchema);