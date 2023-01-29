export default function Wrapper({children}: {children: any}) {
  return <>
    <div className="min-h-full">
      {children}
    </div>
  </>;
}