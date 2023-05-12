import Body from '../components/Body';
import Navbar from '../components/Navbar';
import Wrapper from '../components/Wrapper';
import useDocumentTitle from "../hooks/useDocumentTitle";

export default function Home() {
  useDocumentTitle(`home`);

  return (
    <>
      <Wrapper>
        <Navbar />
        <Body title='Home'>
          <div className="px-4 py-6 sm:px-0">
            <div className="h-[70vh] rounded-lg border-4 border-dashed border-gray-200 overflow-auto">
              This is the home page!
            </div>
          </div>
        </Body>
      </Wrapper>
    </>
  );
}