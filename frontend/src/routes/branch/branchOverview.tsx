import {useNavigate, useParams} from "react-router-dom";
import useDocumentTitle from "../../hooks/useDocumentTitle";
import useSWR from "swr";
import {useAuthStore} from "../../stores/useAuthStore";
import AsyncDataHandler from "../../components/AsyncDataHandler";
import {commitsSchema} from "../../schemas/commitSchema";
import { Line } from "react-chartjs-2";
import { ChartData, Point } from "chart.js";
import {dateFormatter} from "../../utils/dateFormatter";
import {useEffect, useState} from "react";
import {red, green} from 'tailwindcss/colors';
import {ArrowLeftIcon} from "@heroicons/react/24/outline";

export default function BranchOverview() {
    const { token } = useAuthStore();
    const {branchName, repositoryId} = useParams();
    const navigate = useNavigate();
    const [display, setDisplay] = useState<ChartData<"line", (number | Point | null)[], unknown>>({
        labels: [],
        datasets: [
            {
                label: 'Additions',
                data: [],
                borderColor: 'rgb(100,189,44)',
                backgroundColor: 'rgba(53,235,68,0.5)'
            },
            {
                label: 'Deletions',
                data: [],
                borderColor: 'rgb(255, 99, 132)',
                backgroundColor: 'rgba(255, 99, 132, 0.5)'
            }
        ]});
    useDocumentTitle(`${branchName} overview`);

    const { data, error, isLoading } = useSWR(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/commit?branch=${branchName}&mappedByAssignments=true`, (url: string) => {
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

    useEffect(() => {
        if (data) {
            const newData: ChartData<"line", (number | Point | null)[], unknown> = {
                labels: [],
                datasets: [
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
                ]};
            data
                .sort((commitA, commitB) => new Date(commitB.timestamp).getTime() - new Date(commitA.timestamp).getTime())
                .forEach(commit => {
                    if (newData.labels) {
                        newData.labels.push(dateFormatter.format(new Date(commit.timestamp)));
                    }
                    newData.datasets[0].data.push(commit.additions);
                    newData.datasets[1].data.push(commit.deletions);
                });

            setDisplay(newData);
        }
    }, [data]);

    const options = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top' as const,
            },
            title: {
                display: true,
                text: `Additions/Deletions for ${branchName}`,
            },
        },
    };

    return (
        <>
            <AsyncDataHandler isLoading={isLoading} error={error} data={data}>
                <div className="flex gap-2">
                    <button onClick={() => navigate(`/repository/${repositoryId}`)}>
                        <ArrowLeftIcon className="block h-6 w-6" aria-hidden="true" />
                    </button>
                    <h2 className="text-2xl">Overview</h2>
                </div>
                <Line options={options} data={display} />
            </AsyncDataHandler>
        </>
    );
}