import { useEffect } from 'react';
import { ApolloClient, InMemoryCache, ApolloProvider, useQuery, gql } from '@apollo/client';

const epsilonCart = () => {
    useEffect(() => {
        const client = new ApolloClient({
            uri: 'https://48p1r2roz4.sse.codesandbox.io',
            cache: new InMemoryCache()
        });
        client
            .query({
                query: gql`
                    query GetRates {
                        rates(currency: "USD") {
                            currency
                        }
                    }
                `
            })
            .then(result => console.log(result));
    }, []);

    return <div></div>;
};

export default epsilonCart;
