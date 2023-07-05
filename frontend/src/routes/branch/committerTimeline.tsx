import {useNavigate, useParams} from "react-router-dom";
import useDocumentTitle from "../../hooks/useDocumentTitle";
import useSWR from "swr";
import {useAuthStore} from "../../stores/useAuthStore";
import AsyncDataHandler from "../../components/AsyncDataHandler";
import {commitsSchema} from "../../schemas/commitSchema";
import { Line } from "react-chartjs-2";
import { ChartData, Point } from "chart.js";
import {dateFormatter} from "../../utils/dateFormatter";
import {red, green} from 'tailwindcss/colors';
import {ArrowLeftIcon} from "@heroicons/react/24/outline";
import {committersSchema} from "../../schemas/commiterSchema";

export default function BranchOverview() {
    const { token } = useAuthStore();
    const {branchName, repositoryId} = useParams();
    const navigate = useNavigate();
    useDocumentTitle(`${branchName} committer timeline`);

    const {data} = useSWR(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/commit?branch=${branchName}&mappedByAssignments=true`, (url: string) => {
        if (!token) {
            throw new Error('Token is not set');
        }

        return fetch(url, {
            headers: {
                Authorization: token
            }
        })
            .then(res => res.json())
            .then(maybeCommits => commitsSchema.parse(maybeCommits));
    });

    const { data: committerData, error: committerError, isLoading: committerLoading } = useSWR(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/committer?branch=${branchName}&mappedByAssignments=true`, (url: string) => {
        if (!token) {
            throw new Error('Token is not set');
        }

        return fetch(url, {
            headers: {
                Authorization: token
            }
        })
            .then(res => res.json())
            .then(maybeCommits => committersSchema.parse(maybeCommits));
    });

    return (
        <>
            <div className="flex gap-2">
                <button onClick={() => navigate(`/repository/${repositoryId}`)}>
                    <ArrowLeftIcon className="block h-6 w-6" aria-hidden="true" />
                </button>
                <h2 className="text-2xl">Committer timeline</h2>
            </div>
            <AsyncDataHandler isLoading={committerLoading} error={committerError} data={committerData}>
                <ul>
                    {committerData?.map(committer => {
                        const committerDisplay: ChartData<"line", (number | Point | null)[], unknown> = {
                            labels: [],
                            datasets: emptyDataSet()};
                        const committerOptions = {
                            responsive: true,
                            plugins: {
                                legend: {
                                    position: 'top' as const,
                                },
                                title: {
                                    display: true,
                                    text: `Additions/Deletions for ${branchName} from ${committer.name}`,
                                },
                            },
                        };
                        data
                            ?.sort((commitA, commitB) => new Date(commitA.timestamp).getTime() - new Date(commitB.timestamp).getTime())
                            .forEach(commit => {
                                if (commit.author === committer.name) {
                                    if (committerDisplay.labels) {
                                        committerDisplay.labels.push(dateFormatter.format(new Date(commit.timestamp)));
                                    }
                                    committerDisplay.datasets[0].data.push(commit.additions);
                                    committerDisplay.datasets[1].data.push(commit.deletions);
                                }
                            });

                        return <>
                            <Line options={committerOptions} data={committerDisplay} />
                            <div className="relative my-4">
                                <div className="absolute inset-0 flex items-center" aria-hidden="true">
                                    <div className="w-full border-t border-gray-300"/>
                                </div>
                            </div>
                        </>;
                    })}
                </ul>
            </AsyncDataHandler>
        </>
    );
}

function emptyDataSet() {
    return [
        {
            label: 'Additions',
            data: [],
            borderColor: green["400"],
            backgroundColor: green["200"]
        },
        {
            label: 'Deletions',
            data: [],
            borderColor: red["400"],
            backgroundColor: red["200"]
        }
    ];
}