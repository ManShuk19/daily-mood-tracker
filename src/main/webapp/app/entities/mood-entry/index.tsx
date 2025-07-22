import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import MoodEntry from './mood-entry';
import MoodEntryDetail from './mood-entry-detail';
import MoodEntryUpdate from './mood-entry-update';
import MoodEntryDeleteDialog from './mood-entry-delete-dialog';

const MoodEntryRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<MoodEntry />} />
    <Route path="new" element={<MoodEntryUpdate />} />
    <Route path=":id">
      <Route index element={<MoodEntryDetail />} />
      <Route path="edit" element={<MoodEntryUpdate />} />
      <Route path="delete" element={<MoodEntryDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default MoodEntryRoutes;
