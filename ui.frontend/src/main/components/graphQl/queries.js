import { gql } from '@apollo/client';

export const categoryList = gql`
    {
        categoryList {
            children {
                id
                name
                level
                children {
                    id
                    level
                    name
                    children {
                        id
                        level
                        name
                    }
                }
            }
        }
    }
`;

export const addMessageMutation = gql`
    mutation AddMessageMutation($input: MessageInput!) {
        message: addMessage(input: $input) {
            id
            from
            text
        }
    }
`;
