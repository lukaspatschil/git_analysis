import {Outlet, useParams} from "react-router-dom";
import Wrapper from "../../components/Wrapper";
import Navbar from "../../components/Navbar";
import Body from "../../components/Body";

export default function Index() {
    const { repositoryId, branchName} = useParams();

    const navigation = [
        {name: 'Overview', href: `/repository/${repositoryId}/${branchName}`, current: true},
        {name: 'Committer Stats', href: `/repository/${repositoryId}/${branchName}/committerStats`, current: false},
        {name: 'Commits', href: `/repository/${repositoryId}/${branchName}/commits`, current: false},
        {name: 'Committer', href: `/repository/${repositoryId}/${branchName}/committer`, current: false},
    ];

    return (
        <>
            <Wrapper>
                <Navbar />
                <Body title='Index details' navigation={navigation}>
                    <div className="px-4 py-6 sm:px-0">
                        <div className="h-[80vh] rounded-lg border-4 border-dashed border-gray-200 overflow-auto p-1">
                            <Outlet />
                        </div>
                    </div>
                </Body>
            </Wrapper>
        </>
    );
}