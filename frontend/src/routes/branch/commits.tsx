import AsyncDataHandler from "../../components/AsyncDataHandler";
import useSWR from "swr";
import {commitsSchema} from "../../schemas/commitSchema";
import {useAuthStore} from "../../stores/useAuthStore";
import {useParams} from "react-router-dom";
import useDocumentTitle from "../../hooks/useDocumentTitle";

export default function Commits() {
    const { token } = useAuthStore();
    const { repositoryId, branchName} = useParams();
    useDocumentTitle(`${branchName} commits`);
    const { data, error, isLoading } = useSWR(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/commit?branch=${branchName}`, (url: string) => {
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

    return (
        <AsyncDataHandler isLoading={isLoading} error={error} data={data}>
            {<ul className='p-6'>
                {data?.map(commit => <li className='grid grid-cols-5' key={commit.id}>
                    <div>{commit.message}</div>
                    <div>{commit.author}</div>
                    <div>{commit.additions}</div>
                    <div>{commit.deletions}</div>
                    <div>{new Date(commit.timestamp).toString()}</div>
                </li>)}
            </ul>}
        </AsyncDataHandler>
    );
}