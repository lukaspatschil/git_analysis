import { z } from "zod";

export const assignmentSchema = z.object({
   assignedNames: z.array(z.object({
       id: z.number(),
       name: z.string()
   })),
   key: z.string()
});

export const assignmentsSchema = z.array(assignmentSchema);