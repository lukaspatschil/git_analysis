import { Disclosure } from '@headlessui/react';
import { useAuthStore } from '../../stores/useAuthStore';
import { LOGIN_PROVIDER_KEY } from '../ProtectedRoute';
import { userNavigation } from './userNavigation';

export default function ProfileFull() {
  const { user, deleteToken } = useAuthStore();

  function handleSignOut() {
    localStorage.removeItem(LOGIN_PROVIDER_KEY);
    deleteToken();
  }

  return <div className="border-t border-gray-700 pt-4 pb-3">
    <div className="flex items-center px-5">
      <div className="flex-shrink-0">
        <img className="h-10 w-10 rounded-full" src={user?.pictureUrl} alt="" />
      </div>
      <div className="ml-3">
        <div className="text-base font-medium leading-none text-white">{user?.username}</div>
        <div className="text-sm font-medium leading-none text-gray-400">{user?.email}</div>
      </div>
    </div>
    <div className="mt-3 space-y-1 px-2">
      {userNavigation.map((item) => (
        <Disclosure.Button
          key={item.name}
          as="a"
          href={item.href}
          className="block rounded-md px-3 py-2 text-base font-medium text-gray-400 hover:bg-gray-700 hover:text-white"
        >
          {item.name}
        </Disclosure.Button>
      ))}
        <Disclosure.Button
          as="a"
          onClick={handleSignOut}
          className="block rounded-md px-3 py-2 text-base font-medium text-gray-400 hover:bg-gray-700 hover:text-white"
        >
          Sign out
        </Disclosure.Button>
    </div>
  </div>;
}