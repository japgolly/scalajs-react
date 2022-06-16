import { useState, useEffect } from 'react';

function useFriendStatus(id) {
  const [isOnline, setIsOnline] = useState(null);

  useEffect(() => {
    setIsOnline(true);
    return () => {};
  });

  return isOnline;
}

function FriendStatus(props) {
  const isOnline = useFriendStatus(props.friend.id);

  if (isOnline === null) {
    return 'Loading...';
  }
  return isOnline ? 'Online' : 'Offline';
}

export default FriendStatus
