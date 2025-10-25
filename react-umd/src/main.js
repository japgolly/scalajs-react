import { TextDecoder, TextEncoder } from 'fast-text-encoding'; // polyfill
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import * as ReactDOMClient from 'react-dom/client';
import * as ReactDOMServer from 'react-dom/server';
import * as ReactTestUtils from 'react-dom/test-utils';

const CombinedReactDOM = {
  ...ReactDOM,
  ...ReactDOMClient,
};

window.TextDecoder = TextDecoder;
window.TextEncoder = TextEncoder;
window.React = React;
window.ReactDOM = CombinedReactDOM;
window.ReactDOMServer = ReactDOMServer;
window.ReactTestUtils = ReactTestUtils;
