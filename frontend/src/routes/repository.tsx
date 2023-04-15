import useSWR from "swr";
import {useAuthStore} from "../stores/useAuthStore";
import {Link, useParams} from "react-router-dom";
import Wrapper from "../components/Wrapper";
import Navbar from "../components/Navbar";
import Body from "../components/Body";
import AsyncDataHandler from "../components/AsyncDataHandler";
import {branchesSchema} from "../schemas/branchSchema";
import {repoSchema} from "../schemas/repoSchema";
import useDocumentTitle from "../hooks/useDocumentTitle";

export default function Repository() {
    const { token } = useAuthStore();
    const { repositoryId } = useParams();
    useDocumentTitle(`repository ${repositoryId}`);
    const { data, error, isLoading } = useSWR(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}`, (url: string) => {
        if (!token) {
            throw new Error('Token is not set');
        }

        return fetch(url, {
            headers: {
                Authorization: token
            }
        })
            .then(res => res.json())
            .then(maybeRepo => repoSchema.parse(maybeRepo));
    });

    const branches = useSWR(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository/${repositoryId}/branch`, (url: string) => {
        if (!token) {
            throw new Error('Token is not set');
        }

        return fetch(url, {
            headers: {
                Authorization: token
            }
        })
            .then(res => res.json())
            .then(maybeBranches => branchesSchema.parse(maybeBranches));
    });

    return (
        <>
            <Wrapper>
                <Navbar />
                <Body title='Repository details'>
                    <div className="px-4 py-6 sm:px-0">
                        <div className="h-[70vh] rounded-lg border-4 border-dashed border-gray-200 overflow-auto p-2">
                            <AsyncDataHandler isLoading={isLoading} error={error} data={data}>
                                <h2 className="text-2xl">{data?.name}</h2>
                            </AsyncDataHandler>
                            <AsyncDataHandler isLoading={branches.isLoading} error={branches.error} data={branches.data}>
                                {<ul className='p-6'>
                                    {branches.data?.map(branch => <li className='grid grid-cols-2' key={branch.name}>
                                        <Link className='hover:underline' to={`/repository/${repositoryId}/${branch.name}`}>{branch.name}</Link>
                                    </li>)}
                                </ul>}
                            </AsyncDataHandler>
                        </div>
                    </div>
                </Body>
            </Wrapper>
        </>
    );
}