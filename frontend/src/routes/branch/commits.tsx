import AsyncDataHandler from "../../components/AsyncDataHandler";
import useSWR from "swr";
import {commitsSchema} from "../../schemas/commitSchema";
import {useAuthStore} from "../../stores/useAuthStore";
import {useNavigate, useParams} from "react-router-dom";
import useDocumentTitle from "../../hooks/useDocumentTitle";
import {dateFormatter} from "../../utils/dateFormatter";
import {ArrowLeftIcon} from "@heroicons/react/24/outline";

export default function Commits() {
    const { token } = useAuthStore();
    const { repositoryId, branchName} = useParams();
    const navigate = useNavigate();
    useDocumentTitle(`${branchName} commits`);
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

    return (
        <>
            <div className="flex gap-2">
                <button onClick={() => navigate(`/repository/${repositoryId}`)}>
                    <ArrowLeftIcon className="block h-6 w-6" aria-hidden="true" />
                </button>
                <h2 className="text-2xl">Commits</h2>
            </div>
            <AsyncDataHandler isLoading={isLoading} error={error} data={data}>
                {<ul className='p-6'>
                    {data?.map(commit => <li className='grid gap-1' key={commit.id} style={{gridTemplateColumns: '5fr 1fr repeat(2, 0.5fr) 1fr'}}>
                        <div>{commit.message}</div>
                        <div>{commit.author}</div>
                        <div className="text-green-600">+ {commit.additions}</div>
                        <div className="text-red-600">- {commit.deletions}</div>
                        <div>{dateFormatter.format(new Date(commit.timestamp))}</div>
                    </li>)}
                </ul>}
            </AsyncDataHandler>
        </>
    );
}