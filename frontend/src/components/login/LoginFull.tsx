import { Disclosure } from '@headlessui/react';
import { LOGIN_PROVIDER_KEY } from '../ProtectedRoute';

export default function LoginFull() {
  function handleLoginClick(loginProvider: string) {
    localStorage.setItem(LOGIN_PROVIDER_KEY, loginProvider);
    window.location.href = `${import.meta.env.VITE_BASE_API_URL}oauth2/authorization/${loginProvider}`;
  }

  return <div className="mt-3 space-y-1 px-2">
      <div className="px-3 py-2 text-base font-medium leading-none text-white">Log in</div>
      <Disclosure.Button
        as="a"
        onClick={() => handleLoginClick('github')}
        className="block rounded-md px-3 py-2 text-base font-medium text-gray-400 hover:bg-gray-700 hover:text-white"
      >
        Use GitHub
      </Disclosure.Button>
      <Disclosure.Button
        as="a"
        onClick={() => handleLoginClick('gitlab')}
        className="block rounded-md px-3 py-2 text-base font-medium text-gray-400 hover:bg-gray-700 hover:text-white"
      >
        Use GitLab
      </Disclosure.Button>
  </div>;
}