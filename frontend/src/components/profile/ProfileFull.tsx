import { Disclosure } from '@headlessui/react';
import { authStore } from '../../stores/authStore';
import { userNavigation } from './userNavigation';

const user = {
  name: 'Tom Cook',
  email: 'tom@example.com',
  imageUrl:
    'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80',
};

export default function ProfileFull() {
  const { deleteToken } = authStore();

  return <div className="border-t border-gray-700 pt-4 pb-3">
    <div className="flex items-center px-5">
      <div className="flex-shrink-0">
        <img className="h-10 w-10 rounded-full" src={user.imageUrl} alt="" />
      </div>
      <div className="ml-3">
        <div className="text-base font-medium leading-none text-white">{user.name}</div>
        <div className="text-sm font-medium leading-none text-gray-400">{user.email}</div>
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
          onClick={deleteToken}
          className="block rounded-md px-3 py-2 text-base font-medium text-gray-400 hover:bg-gray-700 hover:text-white"
        >
          Sign out
        </Disclosure.Button>
    </div>
  </div>;
}