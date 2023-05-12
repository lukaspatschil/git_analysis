import {useAuthStore} from "../../stores/useAuthStore";
import {useNavigate, useParams} from "react-router-dom";
import useDocumentTitle from "../../hooks/useDocumentTitle";
import useSWR from "swr";
import {statsSchema} from "../../schemas/statSchema";
import AsyncDataHandler from "../../components/AsyncDataHandler";
import {Pie} from "react-chartjs-2";
import {useEffect, useState} from "react";
import {ChartData, Point} from "chart.js/dist/types";
import {colors} from "../../utils/colorGenerator";
import {ArrowLeftIcon} from "@heroicons/react/24/outline";

export default function committerStats() {
    const { token } = useAuthStore();
    const {branchName, repositoryId} = useParams();
    const navigate = useNavigate();
    useDocumentTitle(`${branchName} committer stats`);
    const [displayAdditions, setDisplayAdditions] = useState<ChartData<"pie", (number | Point | null)[], unknown>>(generateDisplayData('Additions'));
    const [displayDeletions, setDisplayDeletions] = useState<ChartData<"pie", (number | Point | null)[], unknown>>(generateDisplayData('Deletions'));
    const [displayCommits, setDisplayCommits] = useState<ChartData<"pie", (number | Point | null)[], unknown>>(generateDisplayData('Commits'));


    const { data, error, isLoading } = useSWR(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/stats?branch=${branchName}&mappedByAssignments=true`, (url: string) => {
        if (!token) {
            throw new Error('Token is not set');
        }

        return fetch(url, {
            headers: {
                Authorization: token
            }
        })
            .then(res => res.json())
            .then(maybeStats => statsSchema.parse(maybeStats));
    });

    useEffect(() => {
        if (data) {
            setDisplayAdditions(generateDisplayData('Additions', data));
            setDisplayDeletions(generateDisplayData('Deletions', data));
            setDisplayCommits(generateDisplayData('Commits', data));
        }
    }, [data]);

    return (
        <>
            <AsyncDataHandler isLoading={isLoading} error={error} data={data}>
                <div className="flex gap-2">
                    <button onClick={() => navigate(`/repository/${repositoryId}`)}>
                        <ArrowLeftIcon className="block h-6 w-6" aria-hidden="true" />
                    </button>
                    <h2 className="text-2xl">Commiter Stats</h2>
                </div>
                <h3 className="text-xl">Number of commits</h3>
                <div className="h-96">
                    <Pie data={displayCommits} width={100}
                         height={50}
                         options={{ maintainAspectRatio: false }} />
                </div>
                <h3 className="text-xl">Number of additions</h3>
                <div className="h-96">
                    <Pie data={displayAdditions} width={100}
                         height={50}
                         options={{ maintainAspectRatio: false }} />
                </div>
                <h3 className="text-xl">Number of deletions</h3>
                <div className="h-96">
                    <Pie data={displayDeletions} width={100}
                         height={50}
                         options={{ maintainAspectRatio: false }} />
                </div>
            </AsyncDataHandler>
        </>
        );
}

function generateDisplayData(type: 'Additions' | 'Deletions' | 'Commits', data?: {committer: string, numberOfCommits: number, numberOfAdditions: number, numberOfDeletions: number}[]) {
    const newData: ChartData<"pie", (number | Point | null)[], unknown> = {
        labels: [],
        datasets: [
            {
                label: `# of ${type}`,
                data: [],
                borderColor: [],
                backgroundColor: []
            },
        ]};
    data?.forEach((committer, index) => {
        if (newData.labels && newData.datasets[0].backgroundColor && newData.datasets[0].borderColor) {
            const color = colors[index % colors.length];
            newData.labels.push(committer.committer);
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            newData.datasets[0].backgroundColor?.push(color["200"]);
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            newData.datasets[0].borderColor?.push(color["400"]);
            newData.datasets[0].data.push(committer[`numberOf${type}`]);
        }
    });

    return newData;
}