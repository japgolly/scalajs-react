const outerRealmFunctionConstructor = Node.constructor;
// const nodeGlobal = new outerRealmFunctionConstructor("return global")();
// nodeGlobal.document = document;
// nodeGlobal.navigator = navigator;
// nodeGlobal.window = window;
// nodeGlobal.Event = window.Event;
// nodeGlobal.CustomEvent = window.CustomEvent;
// nodeGlobal.IS_REACT_ACT_ENVIRONMENT = true;
window.require = new outerRealmFunctionConstructor("return require")();

window.MessageChannel = require('worker_threads').MessageChannel;

window.scrollTo = function () { }
