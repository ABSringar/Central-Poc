// import {
//     ApolloClient,
//     ApolloLink,
//     HttpLink,
//     split,
//     InMemoryCache,
// } from "apollo-boost";
// import { WebSocketLink } from "apollo-link-ws";
// import { getMainDefinition } from "apollo-utilities";
// import { getAccessToken } from "../auth";

// // const httpUrl = "http://localhost:9000/graphql";
// // const wsURL = "ws://localhost:9000/graphql";

// const httpLink = ApolloLink.from([
//     new ApolloLink((operation, forward) => {
//         const token = getAccessToken();
//         if (token) {
//             operation.setContext({
//                 headers: { authorization: `Bearer ${token}` },
//             });
//         }
//         return forward(operation);
//     }),
//     new HttpLink({ uri: httpUrl }),
// ]);

// export default httpLink;

// is the new apolloclient link

import { ApolloClient, HttpLink, ApolloLink, InMemoryCache, concat } from '@apollo/client';

const httpLink = new HttpLink({ uri: '/graphql' });

const authMiddleware = new ApolloLink((operation, forward) => {
    // add the authorization to the headers
    operation.setContext(({ headers = {} }) => ({
        headers: {
            ...headers,
            authorization: localStorage.getItem('token') || null
        }
    }));

    return forward(operation);
});

const client = new ApolloClient({
    cache: new InMemoryCache(),
    link: httpLink
});

export default client;
