import React, { useEffect, useState } from 'react';

const ProductTest = () => {
    const [data, setData] = useState('');

    useEffect(() => {
        const url = '/content/centralthai/us/en/jcr:content/root/container/container/producttest.model.json';

        const fetchData = async () => {
            try {
                const response = await fetch(url);

                const json = await response.json();

                console.log(json);
                setData(json);
            } catch (error) {
                console.log('error', error);
            }
        };

        fetchData();
    }, []);

    return (
        <>
            <h1>Title: {data?.name}</h1>
            {data && (
                <div>
                    Price: {data?.dataLayer[data.id]['xdm:listPrice']} {data?.dataLayer[data.id]['xdm:currencyCode']}
                </div>
            )}
        </>
    );
};

export default ProductTest;
