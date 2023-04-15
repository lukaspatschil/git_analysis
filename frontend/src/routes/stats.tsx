import Body from '../components/Body';
import Navbar from '../components/Navbar';
import Wrapper from '../components/Wrapper';
import useDocumentTitle from "../hooks/useDocumentTitle";

export default function Stats() {
  useDocumentTitle(`stats`);

  return (
    <>
      <Wrapper>
        <Navbar />
        <Body title='Stats'>
          {/* Replace with your content */}
          <div className="px-4 py-6 sm:px-0">
            <div className="h-96 rounded-lg border-4 border-dashed border-gray-200 p-1 flex justify-center items-center">
              There is nothing here!
            </div>
          </div>
          {/* /End replace */}
        </Body>
      </Wrapper>
    </>
  );
}