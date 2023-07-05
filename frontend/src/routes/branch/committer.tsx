import AsyncDataHandler from "../../components/AsyncDataHandler";
import {useAuthStore} from "../../stores/useAuthStore";
import {useNavigate, useParams} from "react-router-dom";
import useSWR, {mutate} from "swr";
import {committersSchema} from "../../schemas/commiterSchema";
import useDocumentTitle from "../../hooks/useDocumentTitle";
import {MouseEventHandler, useState} from "react";
import {assignmentsSchema} from "../../schemas/assignmentSchema";
import {ArrowLeftIcon, TrashIcon} from "@heroicons/react/24/outline";

export default function Committer() {
    const { token } = useAuthStore();
    const [name, setName] = useState<string>();
    const [assignee, setAssignee] = useState<string>();
    const { repositoryId, branchName} = useParams();
    const navigate = useNavigate();
    useDocumentTitle(`${branchName} committer`);
    const { data, error, isLoading } = useSWR(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/committer?branch=${branchName}`, (url: string) => {
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

    const { data: assignments, isLoading: assignmentsLoading, error: assignmentsError } = useSWR(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/assignment`, (url: string) => {
        if (!token) {
            throw new Error('Token is not set');
        }

        return fetch(url, {
            headers: {
                Authorization: token
            }
        })
            .then(res => res.json())
            .then(maybeAssignments => assignmentsSchema.parse(maybeAssignments));
    });

    async function combine() {
        if (!token) {
            throw new Error('Token is not set');
        }

        await fetch(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/assignment`, {
            method: 'POST',
            headers: {
                Authorization: token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                key: name,
                assignedName: assignee
            })
        });
        await mutate(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/assignment`);
    }

    const handleDeleteAssignment = async (id?: number) => {
        if (!token) {
            throw new Error('Token is not set');
        }

        if (id) {
            await fetch(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/assignment/${id}`, {
                method: 'DELETE',
                headers: {
                    Authorization: token
                }
            });

            await mutate(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/assignment`);
        }
    };

    return (
        <>
            <div className="flex gap-2">
                <button onClick={() => navigate(`/repository/${repositoryId}`)}>
                    <ArrowLeftIcon className="block h-6 w-6" aria-hidden="true" />
                </button>
                <h2 className="text-2xl">Committer</h2>
            </div>
            <AsyncDataHandler isLoading={isLoading} error={error} data={data}>
                <div className="grid grid-cols-2">
                    <div>
                        <h2>Names</h2>
                        {<ul className='p-6'>
                            {data?.filter(committer => !assignments?.reduce((prev, next) => {
                                return next.assignedNames.reduce((prev, next) => next.name === committer.name || prev, false) || prev;
                            }, false))
                                .sort((a, b) => a.name.localeCompare(b.name)).map(committer => <li key={committer.name}>
                                <label className='flex gap-2 items-center'>
                                    <input
                                        id="comments"
                                        aria-describedby="comments-description"
                                        name="comments"
                                        type="radio"
                                        className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-600"
                                        value={committer.name}
                                        onClick={event => setName((event.target as HTMLInputElement).value)}
                                    />
                                    <div>{committer.name}</div>
                                </label>
                            </li>)}
                        </ul>}
                    </div>
                    <div>
                        <h2>Assignees</h2>
                        {<ul className='p-6'>
                            {data?.filter(committer => !assignments?.reduce((prev, next) => {
                                return next.assignedNames.reduce((prev, next) => next.name === committer.name || prev, false) || next.key === committer.name || prev;
                            }, false))
                                .sort((a, b) => a.name.localeCompare(b.name)).map(committer => <li key={committer.name}>
                                <label className='flex gap-2 items-center'>
                                    <input
                                        id="comments"
                                        aria-describedby="comments-description"
                                        name="assignee"
                                        type="radio"
                                        className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-600"
                                        value={committer.name}
                                        onClick={event => setAssignee((event.target as HTMLInputElement).value)}
                                    />
                                    <div>{committer.name}</div>
                                </label>
                            </li>)}
                        </ul>}
                    </div>
                    <div className="flex justify-center items-center col-span-2">
                        <button
                            onClick={combine}
                            type="button"
                            className="rounded-full bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                        >
                            Combine names
                        </button>
                    </div>
                </div>
            </AsyncDataHandler>
            <AsyncDataHandler isLoading={assignmentsLoading} error={assignmentsError} data={assignments}>
                <ul>
                    {assignments?.map(assignment => <li key={assignment.key}>
                        {assignment.key}:
                        <ul className="ml-5">
                        {assignment.assignedNames.map(assignedName =>
                            <li key={assignedName.id} className="flex gap-2">
                                <TrashIcon className="block h-6 w-6 cursor-pointer hover:text-gray-500" aria-hidden="true" onClick={() => handleDeleteAssignment(assignedName.id)} />{assignedName.name}
                            </li>)}
                        </ul>
                    </li>)}
                </ul>
            </AsyncDataHandler>
        </>
    );
}