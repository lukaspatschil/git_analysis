import Body from '../components/Body';
import Navbar from '../components/Navbar';
import Wrapper from '../components/Wrapper';

export default function Stats() {
  return <div>
    <Wrapper>
      <Navbar />
      <Body title='Stats' />
    </Wrapper>
  </div>;
}