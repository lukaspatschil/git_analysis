import AsyncDataHandler from "../../components/AsyncDataHandler";
import {useAuthStore} from "../../stores/useAuthStore";
import {useParams} from "react-router-dom";
import useSWR from "swr";
import {committersSchema} from "../../schemas/commiterSchema";
import useDocumentTitle from "../../hooks/useDocumentTitle";
import {FormEventHandler, useState} from "react";

export default function Committer() {
    const { token } = useAuthStore();
    const [name, setName] = useState('');
    const { repositoryId, branchName} = useParams();
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

    const { data: test } = useSWR(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/assignment`, (url: string) => {
        if (!token) {
            throw new Error('Token is not set');
        }

        return fetch(url, {
            headers: {
                Authorization: token
            }
        })
            .then(res => res.json())
            .then(maybeCommits => console.log(maybeCommits));
    });

    const handleSetAssignment: FormEventHandler<HTMLFormElement> = async event => {
        event.preventDefault();
        const selectedNames = [...(event.target as unknown as HTMLInputElement[])].slice(0, data?.length).map(target => target.checked);
        if (!token) {
            throw new Error('Token is not set');
        }

        const updateNameQueries = data?.filter((x, i) => selectedNames[i]).map((comitter, i) => fetch(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/assignment`, {
            method: 'POST',
            headers: {
                Authorization: token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                key: name,
                assignedName: comitter.name
            })
        })) ?? [];

        await Promise.all(updateNameQueries);
    };

    return (
        <AsyncDataHandler isLoading={isLoading} error={error} data={data}>
            <form onSubmit={handleSetAssignment}>
                {<ul className='p-6'>
                    {data?.map(committer => <li key={committer.name}>
                        <label className='flex gap-2 items-center'>
                            <input
                                id="comments"
                                aria-describedby="comments-description"
                                name="comments"
                                type="checkbox"
                                className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-600"
                            />
                            <div>{committer.name}</div>
                        </label>
                    </li>)}
                </ul>}
                <div className='m-4'>
                    <label htmlFor="name" className="block text-sm font-medium leading-6 text-gray-900">
                        New name
                    </label>
                    <div className="mt-2">
                        <input
                            type="text"
                            name="name"
                            id="name"
                            className="block w-full rounded-md border-0 p-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                            value={name}
                            onChange={event => setName(event.target.value)}
                        />
                    </div>
                </div>
                <button
                    type="submit"
                    className="rounded-md m-4 bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                >
                    Combine with name
                </button>
            </form>
        </AsyncDataHandler>
    );
}