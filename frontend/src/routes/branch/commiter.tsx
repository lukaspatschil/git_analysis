import AsyncDataHandler from "../../components/AsyncDataHandler";
import {useAuthStore} from "../../stores/useAuthStore";
import {useParams} from "react-router-dom";
import useSWR from "swr";
import {commitersSchema} from "../../schemas/commiterSchema";
import useDocumentTitle from "../../hooks/useDocumentTitle";

export default function Commiter() {
    const { token } = useAuthStore();
    const { repositoryId, branchName} = useParams();
    useDocumentTitle(`${branchName} commiter`);
    const { data, error, isLoading } = useSWR(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/commiter?branch=${branchName}`, (url: string) => {
        if (!token) {
            throw new Error('Token is not set');
        }

        return fetch(url, {
            headers: {
                Authorization: token
            }
        })
            .then(res => res.json())
            .then(maybeCommits => commitersSchema.parse(maybeCommits));
    });

    return (
        <AsyncDataHandler isLoading={isLoading} error={error} data={data}>
            {<ul className='p-6'>
                {data?.map(commiter => <li key={commiter.name}>
                    <div>{commiter.name}</div>
                </li>)}
            </ul>}
        </AsyncDataHandler>
    );
}