import { Fragment } from 'react';
import { Menu, Transition } from '@headlessui/react';
import { Link } from 'react-router-dom';
import { useAuthStore } from '../../stores/useAuthStore';
import { userNavigation } from './userNavigation';
import { LOGIN_PROVIDER_KEY } from '../ProtectedRoute';

function classNames(...classes: any[]) {
  return classes.filter(Boolean).join(' ');
}

export default function ProfileDropDown() {
  const { user, deleteToken } = useAuthStore();

  function handleSignOut() {
    localStorage.removeItem(LOGIN_PROVIDER_KEY);
    deleteToken();
  }

  return <Menu as="div" className="relative ml-3">
    <div>
      <Menu.Button className="flex max-w-xs items-center rounded-full bg-gray-800 text-sm focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-800">
        <span className="sr-only">Open user menu</span>
        <img className="h-8 w-8 rounded-full" src={user?.pictureUrl} alt="" />
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
        {userNavigation.map((item) => (
          <Menu.Item key={item.name}>
            {({ active }) => (
              <Link
                to={item.href}
                className={classNames(
                  active ? 'bg-gray-100' : '',
                  'block px-4 py-2 text-sm text-gray-700'
                )}
              >
                {item.name}
              </Link>
            )}
          </Menu.Item>
        ))}
        <Menu.Item>
          {({ active }) => (
            <button
              onClick={handleSignOut}
              className={classNames(
                active ? 'bg-gray-100' : '',
                'block px-4 py-2 text-sm text-gray-700 w-full text-start'
              )}
            >
              Sign out
            </button>
          )}
        </Menu.Item>
      </Menu.Items>
    </Transition>
  </Menu>;
}