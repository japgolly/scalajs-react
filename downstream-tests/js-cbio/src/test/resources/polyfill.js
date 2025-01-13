const outerRealmFunctionConstructor = Node.constructor;
window.require = new outerRealmFunctionConstructor("return require")();

window.MessageChannel = require('worker_threads').MessageChannel;
