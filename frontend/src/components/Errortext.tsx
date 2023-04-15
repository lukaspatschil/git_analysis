import {ReactNode} from 'react';

export default function Errortext({children}: {children: ReactNode}) {
  return <div className='w-full h-full flex justify-center items-center' role="status">
      <div className='text-sm font-medium text-red-600'>{children}</div>
    </div>;
}