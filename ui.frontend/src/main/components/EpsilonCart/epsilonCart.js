import { useEffect } from 'react';
import { ApolloClient, InMemoryCache, ApolloProvider, useQuery, gql } from '@apollo/client';
import { categoryList } from '../graphQl/queries';

const epsilonCart = () => {
    const data = useQuery(categoryList);
    console.log(data);

    return <div>Epsilon Cart Authoring </div>;
};

export default epsilonCart;
