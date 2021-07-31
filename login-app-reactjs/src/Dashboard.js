import React, {useState, useEffect} from 'react';
import { getUser, getToken, removeUserSession } from './Utils/Common';
import axios from 'axios';

function Dashboard(props) {
  const user = getUser();
  const [profile, setProfile] = useState(null);

  useEffect(() => {
	axios({
	  method: 'get',
	  url: 'http://localhost:8080/profile',
	  headers: {'Authorization': 'Bearer ' + getToken()}
	}).then( function(response) {
		setProfile(response.data);
	});
  }, []);
  
  // handle click event of logout button
  const handleLogout = () => {
    removeUserSession();
    props.history.push('/logout');
  }
  
  function showProfile() {
	  if ( profile != null ) {
		  return (
				  <div>
				  <p>Username: {profile.username}</p>
				  <p>First: {profile.first_name}</p>
				  <p>Last: {profile.last_name}</p>
				  <p>Enabled: {profile.enabled.toString()}</p>
				  </div>
			  );
	  }
  }
  
  return (
    <div>
      Welcome {user}!<br /><br />
      <input type="button" onClick={handleLogout} value="Logout" />
	  {showProfile()}
	  </div>
  );
}

export default Dashboard;
