/*******************************************************************************
 *
 *    Copyright 2020 Adobe. All rights reserved.
 *    This file is licensed to you under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License. You may obtain a copy
 *    of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software distributed under
 *    the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 *    OF ANY KIND, either express or implied. See the License for the specific language
 *    governing permissions and limitations under the License.
 *
 ******************************************************************************/

import React, { Fragment, useEffect } from 'react';
import { shape, string } from 'prop-types';
import { FormattedMessage, useIntl } from 'react-intl';
import { AlertCircle as AlertCircleIcon } from 'react-feather';
import { Link } from 'react-router-dom';

import { useWindowSize, useToasts } from '@magento/peregrine';
import { CHECKOUT_STEP, useCheckoutPage } from '@magento/peregrine/lib/talons/CheckoutPage/useCheckoutPage';

import { useStyle } from '@magento/venia-ui/lib/classify';
import Button from '@magento/venia-ui/lib/components/Button';
import Icon from '@magento/venia-ui/lib/components/Icon';
import { fullPageLoadingIndicator } from '@magento/venia-ui/lib/components/LoadingIndicator';
import StockStatusMessage from '@magento/venia-ui/lib/components/StockStatusMessage';
import FormError from '@magento/venia-ui/lib/components/FormError';
import AddressBook from '@magento/venia-ui/lib/components/CheckoutPage/AddressBook';
import GuestSignIn from '@magento/venia-ui/lib/components/CheckoutPage/GuestSignIn';
import PriceAdjustments from '@magento/venia-ui/lib/components/CheckoutPage/PriceAdjustments';
import ShippingMethod from '@magento/venia-ui/lib/components/CheckoutPage/ShippingMethod';
import ShippingInformation from '@magento/venia-ui/lib/components/CheckoutPage/ShippingInformation';
import ItemsReview from '@magento/venia-ui/lib/components/CheckoutPage/ItemsReview';
import ScrollAnchor from '@magento/venia-ui/lib/components/ScrollAnchor/scrollAnchor';

import defaultClasses from '@magento/venia-ui/lib/components/CheckoutPage/checkoutPage.css';

// Overrides
import OrderSummary from './OrderSummary';
import PaymentInformation from './PaymentInformation';
import payments from './PaymentInformation/paymentMethodCollection';
import OrderConfirmationPage from './OrderConfirmationPage';

const errorIcon = <Icon src={AlertCircleIcon} size={20} />;

const CheckoutPage = props => {
    const { classes: propClasses } = props;
    const { formatMessage } = useIntl();
    const talonProps = useCheckoutPage();

    const {
        /**
         * Enum, one of:
         * SHIPPING_ADDRESS, SHIPPING_METHOD, PAYMENT, REVIEW
         */
        activeContent,
        availablePaymentMethods,
        cartItems,
        checkoutStep,
        customer,
        error,
        handlePlaceOrder,
        hasError,
        isCartEmpty,
        isGuestCheckout,
        isLoading,
        isUpdating,
        orderDetailsData,
        orderDetailsLoading,
        orderNumber,
        placeOrderLoading,
        setCheckoutStep,
        setIsUpdating,
        setShippingInformationDone,
        scrollShippingInformationIntoView,
        setShippingMethodDone,
        scrollShippingMethodIntoView,
        setPaymentInformationDone,
        shippingInformationRef,
        shippingMethodRef,
        resetReviewOrderButtonClicked,
        handleReviewOrder,
        reviewOrderButtonClicked,
        toggleAddressBookContent,
        toggleSignInContent
    } = talonProps;

    const [, { addToast }] = useToasts();

    useEffect(() => {
        if (hasError) {
            const message =
                error && error.message
                    ? error.message
                    : formatMessage({
                          id: 'checkoutPage.errorSubmit',
                          defaultMessage: 'Oops! An error occurred while submitting. Please try again.'
                      });
            addToast({
                type: 'error',
                icon: errorIcon,
                message,
                dismissable: true,
                timeout: 7000
            });

            if (process.env.NODE_ENV !== 'production') {
                console.error(error);
            }
        }
    }, [addToast, error, formatMessage, hasError]);

    const classes = useStyle(defaultClasses, propClasses);

    const windowSize = useWindowSize();
    const isMobile = windowSize.innerWidth <= 960;

    let checkoutContent;

    const heading = isGuestCheckout
        ? formatMessage({
              id: 'checkoutPage.guestCheckout',
              defaultMessage: 'Guest Checkout'
          })
        : formatMessage({
              id: 'checkoutPage.checkout',
              defaultMessage: 'Checkout'
          });

    if (orderNumber && orderDetailsData) {
        return <OrderConfirmationPage data={orderDetailsData} orderNumber={orderNumber} />;
    } else if (isLoading) {
        return fullPageLoadingIndicator;
    } else if (isCartEmpty) {
        checkoutContent = (
            <div className={classes.empty_cart_container}>
                <div className={classes.heading_container}>
                    <h1 className={classes.heading}>{heading}</h1>
                </div>
                <h3>
                    <FormattedMessage
                        id={'checkoutPage.emptyMessage'}
                        defaultMessage={'There are no items in your cart.'}
                    />
                </h3>
            </div>
        );
    } else {
        const signInContainerElement = isGuestCheckout ? (
            <div className={classes.signInContainer}>
                <span className={classes.signInLabel}>
                    <FormattedMessage id={'checkoutPage.signInLabel'} defaultMessage={'Sign in for Express Checkout'} />
                </span>
            </div>
        ) : null;

        const shippingMethodSection =
            checkoutStep >= CHECKOUT_STEP.SHIPPING_METHOD ? (
                <ShippingMethod
                    pageIsUpdating={isUpdating}
                    onSave={setShippingMethodDone}
                    onSuccess={scrollShippingMethodIntoView}
                    setPageIsUpdating={setIsUpdating}
                />
            ) : (
                <h3 className={classes.shipping_method_heading}>
                    <FormattedMessage id={'checkoutPage.shippingMethodStep'} defaultMessage={'2. Shipping Method'} />
                </h3>
            );

        const formErrors = [];

        const paymentMethods = Object.keys(payments);

        // If we have an implementation, or if this is a "zero" checkout,
        // we can allow checkout to proceed.
        const isPaymentAvailable = !!availablePaymentMethods.find(
            ({ code }) => code === 'free' || paymentMethods.includes(code)
        );

        if (!isPaymentAvailable) {
            formErrors.push(
                new Error(
                    formatMessage({
                        id: 'checkoutPage.noPaymentAvailable',
                        defaultMessage: 'Payment is currently unavailable.'
                    })
                )
            );
        }

        const paymentInformationSection =
            checkoutStep >= CHECKOUT_STEP.PAYMENT ? (
                <PaymentInformation
                    onSave={setPaymentInformationDone}
                    checkoutError={error}
                    resetShouldSubmit={resetReviewOrderButtonClicked}
                    setCheckoutStep={setCheckoutStep}
                    shouldSubmit={reviewOrderButtonClicked}
                />
            ) : (
                <h3 className={classes.payment_information_heading}>
                    <FormattedMessage
                        id={'checkoutPage.paymentInformationStep'}
                        defaultMessage={'3. Payment Information'}
                    />
                </h3>
            );

        const priceAdjustmentsSection =
            checkoutStep === CHECKOUT_STEP.PAYMENT ? (
                <div className={classes.price_adjustments_container}>
                    <PriceAdjustments setPageIsUpdating={setIsUpdating} />
                </div>
            ) : null;

        const reviewOrderButton =
            checkoutStep === CHECKOUT_STEP.PAYMENT ? (
                <Button
                    onClick={handleReviewOrder}
                    priority="high"
                    className={classes.review_order_button}
                    disabled={reviewOrderButtonClicked || isUpdating || !isPaymentAvailable}
                >
                    <FormattedMessage id={'checkoutPage.reviewOrder'} defaultMessage={'Review Order'} />
                </Button>
            ) : null;

        const itemsReview =
            checkoutStep === CHECKOUT_STEP.REVIEW ? (
                <div className={classes.items_review_container}>
                    <ItemsReview />
                </div>
            ) : null;

        const placeOrderButton =
            checkoutStep === CHECKOUT_STEP.REVIEW ? (
                <Button
                    onClick={handlePlaceOrder}
                    priority="high"
                    className={classes.place_order_button}
                    disabled={isUpdating || placeOrderLoading || orderDetailsLoading}
                >
                    <FormattedMessage id={'checkoutPage.placeOrder'} defaultMessage={'Place Order'} />
                </Button>
            ) : null;

        // If we're on mobile we should only render price summary in/after review.
        const shouldRenderPriceSummary = !(isMobile && checkoutStep < CHECKOUT_STEP.REVIEW);

        const orderSummary = shouldRenderPriceSummary ? (
            <div className={classes.summaryContainer}>
                <OrderSummary isUpdating={isUpdating} />
            </div>
        ) : null;

        let headerText;

        if (isGuestCheckout) {
            headerText = formatMessage({
                id: 'checkoutPage.guestCheckout',
                defaultMessage: 'Guest Checkout'
            });
        } else if (customer.default_shipping) {
            headerText = formatMessage({
                id: 'checkoutPage.reviewAndPlaceOrder',
                defaultMessage: 'Review and Place Order'
            });
        } else {
            headerText = formatMessage(
                { id: 'checkoutPage.greeting', defaultMessage: 'Welcome' },
                { firstname: customer.firstname }
            );
        }

        const checkoutContentClass =
            activeContent === 'checkout' ? classes.checkoutContent : classes.checkoutContent_hidden;

        const stockStatusMessageElement = (
            <Fragment>
                <FormattedMessage
                    id={'checkoutPage.stockStatusMessage'}
                    defaultMessage={
                        'An item in your cart is currently out-of-stock and must be removed in order to Checkout. Please return to your cart to remove the item.'
                    }
                />
                <Link className={classes.cartLink} to={'/cart'}>
                    <FormattedMessage id={'checkoutPage.returnToCart'} defaultMessage={'Return to Cart'} />
                </Link>
            </Fragment>
        );
        checkoutContent = (
            <div className={checkoutContentClass}>
                <div className={classes.heading_container}>
                    <FormError
                        classes={{
                            root: classes.formErrors
                        }}
                        errors={formErrors}
                    />
                    <StockStatusMessage cartItems={cartItems} message={stockStatusMessageElement} />
                    <h1 className={classes.heading}>{headerText}</h1>
                </div>
                {signInContainerElement}
                <div className={classes.shipping_information_container}>
                    <ScrollAnchor ref={shippingInformationRef}>
                        <ShippingInformation
                            onSave={setShippingInformationDone}
                            onSuccess={scrollShippingInformationIntoView}
                            toggleActiveContent={toggleAddressBookContent}
                        />
                    </ScrollAnchor>
                </div>
                <div className={classes.shipping_method_container}>
                    <ScrollAnchor ref={shippingMethodRef}>{shippingMethodSection}</ScrollAnchor>
                </div>
                <div className={classes.payment_information_container}>{paymentInformationSection}</div>
                {priceAdjustmentsSection}
                {reviewOrderButton}
                {itemsReview}
                {orderSummary}
                {placeOrderButton}
            </div>
        );
    }

    const addressBookElement = !isGuestCheckout ? (
        <AddressBook
            activeContent={activeContent}
            toggleActiveContent={toggleAddressBookContent}
            onSuccess={scrollShippingInformationIntoView}
        />
    ) : null;

    const signInElement = isGuestCheckout ? (
        <GuestSignIn isActive={activeContent === 'signIn'} toggleActiveContent={toggleSignInContent} />
    ) : null;

    return (
        <div className={classes.root}>
            {checkoutContent}
            {addressBookElement}
            {signInElement}
        </div>
    );
};

export default CheckoutPage;

CheckoutPage.propTypes = {
    classes: shape({
        root: string,
        checkoutContent: string,
        checkoutContent_hidden: string,
        heading_container: string,
        heading: string,
        cartLink: string,
        stepper_heading: string,
        shipping_method_heading: string,
        payment_information_heading: string,
        signInContainer: string,
        signInLabel: string,
        signInButton: string,
        empty_cart_container: string,
        shipping_information_container: string,
        shipping_method_container: string,
        payment_information_container: string,
        price_adjustments_container: string,
        items_review_container: string,
        summaryContainer: string,
        formErrors: string,
        review_order_button: string,
        place_order_button: string
    })
};
