import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Body from '../components/Body';
import Navbar from '../components/Navbar';
import Wrapper from '../components/Wrapper';
import { useAuthStore } from '../stores/useAuthStore';
import useDocumentTitle from "../hooks/useDocumentTitle";

export default function Login() {
  const { setToken } = useAuthStore();
    useDocumentTitle(`login`);
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const token = location.hash.substring(1);

    if (import.meta.env.MODE === 'development') {
        console.log(token);
    }

    setToken(token).then(() => navigate('/repository'));
  }, []);

  return (
    <>
      <Wrapper>
        <Navbar />
        <Body title='Login'>
            <div className="px-4 py-6 sm:px-0">
              <div className="h-96 rounded-lg border-4 border-dashed border-gray-200 flex justify-center items-center">
                <div className="text-xl font-medium text-gray-500">You should be redirected...</div>
              </div>
            </div>
        </Body>
      </Wrapper>
    </>
  );
}