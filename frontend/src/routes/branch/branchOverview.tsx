import {useParams} from "react-router-dom";
import useDocumentTitle from "../../hooks/useDocumentTitle";

export default function BranchOverview() {
    const { branchName} = useParams();
    useDocumentTitle(`${branchName} overview`);

    return (
        <>
            This is the overview for the branch {branchName}.
            Please select a tab on the top right.
        </>
    );
}