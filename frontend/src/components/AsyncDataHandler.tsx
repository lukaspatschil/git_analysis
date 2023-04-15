import Errortext from "./Errortext";
import Loading from "./Loading";
import {ReactNode} from "react";

export default function AsyncDataHandler({isLoading, error, data, children}: {isLoading: boolean, error: any, data: any, children: ReactNode}) {
    return (
        <>
            {Boolean(error) && <Errortext>{error?.message ?? error.toString()}</Errortext>}
            {isLoading && <Loading />}
            {Boolean(data) && children}
        </>
    );
}