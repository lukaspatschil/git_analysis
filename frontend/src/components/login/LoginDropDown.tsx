import { Fragment } from 'react';
import { Menu, Transition } from '@headlessui/react';
import { ChevronDownIcon } from '@heroicons/react/24/outline';
import { LOGIN_PROVIDER_KEY } from '../ProtectedRoute';

function classNames(...classes: any[]) {
  return classes.filter(Boolean).join(' ');
}

export default function LoginDropDown() {
  function handleLoginClick(loginProvider: string) {
    localStorage.setItem(LOGIN_PROVIDER_KEY, loginProvider);
    window.location.href = `${import.meta.env.VITE_BASE_API_URL}oauth2/authorization/${loginProvider}`;
  }
  
  return <Menu as="div" className="relative ml-3">
    <div>
      <Menu.Button className="flex max-w-xs items-center rounded-full bg-gray-800 text-sm focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-800">
        <span className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium flex">
          <span>Log in</span>
          <ChevronDownIcon
            className="ml-2 h-5 w-5"
            aria-hidden="true"
          />
        </span>
      </Menu.Button>
    </div>
    <Transition
      as={Fragment}
      enter="transition ease-out duration-100"
      enterFrom="transform opacity-0 scale-95"
      enterTo="transform opacity-100 scale-100"
      leave="transition ease-in duration-75"
      leaveFrom="transform opacity-100 scale-100"
      leaveTo="transform opacity-0 scale-95"
    >
      <Menu.Items className="absolute right-0 z-10 mt-2 w-48 origin-top-right rounded-md bg-white py-1 shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
        <Menu.Item>
          {({ active }) => (
              <a
                onClick={() => handleLoginClick('github')}
                className={classNames(
                  active ? 'bg-gray-100' : '',
                  'block px-4 py-2 text-sm text-gray-700'
                )}
              >
                Use GitHub
              </a>
            )}
        </Menu.Item>
        <Menu.Item>
          {({ active }) => (
              <a
                onClick={() => handleLoginClick('gitlab')}
                className={classNames(
                  active ? 'bg-gray-100' : '',
                  'block px-4 py-2 text-sm text-gray-700'
                )}
              >
                Use GitLab
              </a>
            )}
        </Menu.Item>
      </Menu.Items>
    </Transition>
  </Menu>;
}