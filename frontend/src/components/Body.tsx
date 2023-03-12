import { ReactNode } from 'react';
import {Link, useLocation, useNavigate} from "react-router-dom";
import { ArrowLeftIcon } from '@heroicons/react/24/outline';

function classNames(...classes: string[]) {
  return classes.filter(Boolean).join(' ');
}

type NavigationNode = {
  name: string;
  href: string;
  current: boolean;
}

export default function Body({title, navigation, children}: {title: string, navigation?: NavigationNode[], children: ReactNode}) {
  const location = useLocation();
  const navigate = useNavigate();

  return <>
    <header className="bg-white shadow">
      <div className="mx-auto max-w-7xl py-6 px-4 sm:px-6 lg:px-8 flex justify-between">
        <div className="flex gap-1">
          <button onClick={() => navigate(-1)}>
            <ArrowLeftIcon className="block h-6 w-6" aria-hidden="true" />
          </button>
          <h1 className="text-3xl font-bold tracking-tight text-gray-900">{title}</h1>
        </div>

        {Boolean(navigation) && <div className="sm:-my-px sm:ml-6 sm:flex sm:space-x-8">
          {navigation?.map((item) => (
              <Link
                  key={item.name}
                  to={item.href}
                  className={classNames(
                      location.pathname === item.href
                          ? 'border-indigo-500 text-gray-900'
                          : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700',
                      'inline-flex items-center border-b-2 px-1 pt-1 text-sm font-medium'
                  )}
                  aria-current={location.pathname === item.href ? 'page' : undefined}
              >
                {item.name}
              </Link>
          ))}
        </div>}
      </div>
    </header>
    <main>
      <div className="mx-auto max-w-7xl py-6 sm:px-6 lg:px-8">
        {children}
      </div>
    </main>
  </>;
}