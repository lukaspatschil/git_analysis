import Body from '../components/Body';
import Navbar from '../components/Navbar';
import Wrapper from '../components/Wrapper';
import useDocumentTitle from "../hooks/useDocumentTitle";
import {LOGIN_PROVIDER_KEY} from "../components/ProtectedRoute";
import {useAuthStore} from "../stores/useAuthStore";
import { Link } from 'react-router-dom';

export default function Home() {
  const { token } = useAuthStore();
  useDocumentTitle(`home`);

  function handleLoginClick(loginProvider: 'github' | 'gitlab') {
    localStorage.setItem(LOGIN_PROVIDER_KEY, loginProvider);
    window.location.href = `${import.meta.env.VITE_BASE_API_URL}oauth2/authorization/${loginProvider}`;
  }

  return (
    <>
      <Wrapper>
        <Navbar />
        <Body title='Home'>
          <div className="px-4 py-6 sm:px-0">
            <div className="h-[70vh] rounded-lg border-4 border-dashed border-gray-200 overflow-auto p-4">
              <h2 className="text-2xl font-bold tracking-tight text-gray-900">Welcome to git analyser</h2>
              <p className="mb-5">
                This is <span className="italic">git analyser</span> a interactive web application to track the commit data of users inside your Github and GitLab repositories.
              </p>

              <h3 className="text-xl font-bold tracking-tight text-gray-900">Getting started</h3>
              <p>
                In order to get started log into your Account using GitHub or GitLab and select one of your repositories.
              </p>
              <p>
                Then you can browse the different branches and see the committer, commits and the amount of lines they provided.
                If you want you can also combine committer if a person has worked from multiple different accounts by accident.
              </p>
              <img className="mx-auto mb-10" alt="Example of the git analysis overview graph." src="/example.png" />

              <div className="flex gap-2 justify-center items-center">
                {token ?
                    <Link to="repository" className="rounded-full bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600">
                      To dashboard
                    </Link>
                    :
                    <>
                      <button
                          onClick={() => handleLoginClick('github')}
                          type="button"
                          className="rounded-full bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                      >
                        Log in with GitHub
                      </button>
                      <button
                          onClick={() => handleLoginClick('gitlab')}
                          type="button"
                          className="rounded-full bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                      >
                        Log in with GitLab
                      </button>
                    </>
                }
              </div>
            </div>
          </div>
        </Body>
      </Wrapper>
    </>
  );
}