var _s = $RefreshSig$(),
    _s2 = $RefreshSig$();

import { useState, useEffect } from 'react';

function useFriendStatus(id) {
  _s();

  const [isOnline, setIsOnline] = useState(null);
  useEffect(() => {
    setIsOnline(true);
    return () => {};
  });
  return isOnline;
}

_s(useFriendStatus, "HLSnnfJYDaeB5hXQTNbVZdmJwXQ=");

function FriendStatus(props) {
  _s2();

  const isOnline = useFriendStatus(props.friend.id);

  if (isOnline === null) {
    return 'Loading...';
  }

  return isOnline ? 'Online' : 'Offline';
}

_s2(FriendStatus, "a3B569k9rI2h5KE8Nx8Iioa8o4I=", false, function () {
  return [useFriendStatus];
});

_c = FriendStatus;
export default FriendStatus;

var _c;

$RefreshReg$(_c, "FriendStatus");
