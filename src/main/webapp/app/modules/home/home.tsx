import React from 'react';

import { useAppSelector } from 'app/config/store';
import MoodDashboard from './mood-dashboard';

export const Home = () => {
  return <MoodDashboard />;
};

export default Home;
