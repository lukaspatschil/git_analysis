import Body from '../components/Body';
import Navbar from '../components/Navbar';
import Wrapper from '../components/Wrapper';

export default function Home() {
  return (
    <>
      <Wrapper>
        <Navbar />
        <Body title='Dashboard' />
      </Wrapper>
    </>
  );
}