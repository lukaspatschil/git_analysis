import { Disclosure } from '@headlessui/react';

export default function LoginFull() {
  return <div className="mt-3 space-y-1 px-2">
      <div className="px-3 py-2 text-base font-medium leading-none text-white">Log in</div>
      <Disclosure.Button
        as="a"
        href='http://localhost:8080/oauth2/authorization/github'
        className="block rounded-md px-3 py-2 text-base font-medium text-gray-400 hover:bg-gray-700 hover:text-white"
      >
        Use GitHub
      </Disclosure.Button>
      <Disclosure.Button
        as="a"
        href='http://localhost:8080/oauth2/authorization/gitlab'
        className="block rounded-md px-3 py-2 text-base font-medium text-gray-400 hover:bg-gray-700 hover:text-white"
      >
        Use GitLab
      </Disclosure.Button>
  </div>;
}