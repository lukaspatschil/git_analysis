import Body from '../components/Body';
import Navbar from '../components/Navbar';
import Wrapper from '../components/Wrapper';
import {Link} from 'react-router-dom';
import { useAuthStore } from '../stores/useAuthStore';
import useSWR from 'swr';
import Loading from '../components/Loading';
import Errortext from '../components/Errortext';
import { z } from 'zod';

const repositorySchema = z.object({
  id: z.number(),
  name: z.string(),
  url: z.string().url()
});

const repositoryArraySchema = z.array(repositorySchema);

export default function Dashboard() {
  const { token } = useAuthStore();
  const { data, error, isLoading } = useSWR(`${import.meta.env.VITE_BASE_API_URL}apiV1/repository`, (url) => {
    if (!token) {
      throw new Error('Token is not set');
    }
  
    return fetch(url, {
      headers: {
        Authorization: token
      }
    })
      .then(res => res.json())
      .then(maybeRepos => repositoryArraySchema.parse(maybeRepos));
  });

  return (
    <>
      <Wrapper>
        <Navbar />
        <Body title='Dashboard'>
          <div className="px-4 py-6 sm:px-0">
            <div className="h-96 rounded-lg border-4 border-dashed border-gray-200 overflow-auto">
              {Boolean(error) && <Errortext>{error.toString()}</Errortext>}
              {isLoading && <Loading />}
              {Boolean(data) &&<ul className='p-6'>
                {data?.map((repo: any) => <li className='grid grid-cols-2' key={repo.id}>
                    <div>{repo.name}</div>
                    <Link className='hover:underline' to={repo.url}>{repo.url}</Link>
                  </li>)}
              </ul>}
            </div>
          </div>
        </Body>
      </Wrapper>
    </>
  );
}