import {z} from 'zod';
import {commitsSchema} from "../schemas/commitSchema";

export default function ParticipationChart(commits: z.infer<typeof commitsSchema>) {
    return (
        <>
            Not implemented!
        </>
    );
}