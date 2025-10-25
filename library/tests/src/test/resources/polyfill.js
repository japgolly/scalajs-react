const outerRealmFunctionConstructor = Node.constructor;
window.require = new outerRealmFunctionConstructor("return require")();

window.MessageChannel = require('worker_threads').MessageChannel;

window.scrollTo = function () { }



// --- NEW JSDOM POLYFILLS for React 19 ---
// React 19 (and 18.3+) relies on some DOM APIs that jsdom doesn't
// fully implement, especially around focus management and <dialog>.
if (!window.HTMLDialogElement) {
  // @ts-ignore
  window.HTMLDialogElement = class HTMLDialogElement extends HTMLElement {};
  window.HTMLDialogElement.prototype.showModal = function () {
    /* no-op */
  };
  window.HTMLDialogElement.prototype.close = function () {
    /* no-op */
  };
}

// A simple polyfill for requestAnimationFrame, which jsdom might not provide.
if (!window.requestAnimationFrame) {
  window.requestAnimationFrame = function (callback) {
    return setTimeout(callback, 0);
  };
  // @ts-ignore
  window.cancelAnimationFrame = function (id) {
    clearTimeout(id);
  };
}

  // Add PointerEvent polyfill (React 19 uses this)
  if (!window.PointerEvent) {
    // @ts-ignore
    window.PointerEvent = class PointerEvent extends MouseEvent {};
  }

  // Add IntersectionObserver polyfill (common in modern test envs)
  if (!window.IntersectionObserver) {
    // @ts-ignore
    window.IntersectionObserver = class IntersectionObserver {
      observe() {
        /* no-op */
      }
      unobserve() {
        /* no-op */
      }
      disconnect() {
        /* no-op */
      }
    };
  }
  // --- END NEW JSDOM POLYFILLS ---
