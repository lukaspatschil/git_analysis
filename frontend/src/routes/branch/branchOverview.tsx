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
import {red, green, blue} from 'tailwindcss/colors';
import {ArrowLeftIcon} from "@heroicons/react/24/outline";
import {committersSchema} from "../../schemas/commiterSchema";

export default function BranchOverview() {
    const { token } = useAuthStore();
    const {branchName, repositoryId} = useParams();
    const navigate = useNavigate();
    const [display, setDisplay] = useState<ChartData<"line", (number | Point | null)[], unknown>>({
        labels: [],
        datasets: emptyDataSet()});
    const [displayLinesOfCode, setDisplayLinesOfCode] = useState<ChartData<"line", (number | Point | null)[], unknown>>({
        labels: [],
        datasets: emptySet()});
    const [displayLinesOfCodeAgg, setDisplayLinesOfCodeAgg] = useState<ChartData<"line", (number | Point | null)[], unknown>>({
        labels: [],
        datasets: emptySet()});
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

    useEffect(() => {
        if (data) {
            const newData: ChartData<"line", (number | Point | null)[], unknown> = {
                labels: [],
                datasets: emptyDataSet()};
            const newData2: ChartData<"line", (number | Point | null)[], unknown> = {
                labels: [],
                datasets: emptySet()};
            const newDataAgg: ChartData<"line", (number | null)[], unknown> = {
                labels: [],
                datasets: emptySet()};
            data
                .sort((commitA, commitB) => new Date(commitA.timestamp).getTime() - new Date(commitB.timestamp).getTime())
                .forEach(commit => {
                    if (newData.labels) {
                        newData.labels.push(dateFormatter.format(new Date(commit.timestamp)));
                    }
                    newData.datasets[0].data.push(commit.additions);
                    newData.datasets[1].data.push(commit.deletions);

                    if (newData2.labels) {
                        newData2.labels.push(dateFormatter.format(new Date(commit.timestamp)));
                    }
                    newData2.datasets[0].data.push(commit.linesOfCodeOverall);

                    if (newDataAgg.labels) {
                        newDataAgg.labels.push(dateFormatter.format(new Date(commit.timestamp)));
                    }
                    const previousValue = newDataAgg.datasets[0].data.at(-1) ?? 0;
                    newDataAgg.datasets[0].data.push(previousValue + commit.linesOfCodeOverall);
                });

            setDisplay(newData);
            setDisplayLinesOfCode(newData2);
            setDisplayLinesOfCodeAgg(newDataAgg);
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

    const optionsLinesOfCode = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top' as const,
            },
            title: {
                display: true,
                text: `Total lines of code for ${branchName}`,
            },
        },
    };

    const optionsLinesOfCodeAgg = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top' as const,
            },
            title: {
                display: true,
                text: `Total lines of code for ${branchName} aggregated`,
            },
        },
    };

    return (
        <>
            <div className="flex gap-2">
                <button onClick={() => navigate(`/repository/${repositoryId}`)}>
                    <ArrowLeftIcon className="block h-6 w-6" aria-hidden="true" />
                </button>
                <h2 className="text-2xl">Overview</h2>
            </div>
            <AsyncDataHandler isLoading={isLoading} error={error} data={data}>
                <h3 className="text-xl">Overall commits</h3>
                <Line options={options} data={display} />
                <Line options={optionsLinesOfCode} data={displayLinesOfCode} />
                <Line options={optionsLinesOfCodeAgg} data={displayLinesOfCodeAgg} />
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

function emptySet() {
    return [
        {
            label: 'Total lines of code',
            data: [],
            borderColor: blue["400"],
            backgroundColor: blue["200"]
        }
    ];
}