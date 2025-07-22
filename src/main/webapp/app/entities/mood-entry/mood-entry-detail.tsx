import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './mood-entry.reducer';

export const MoodEntryDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const moodEntryEntity = useAppSelector(state => state.moodEntry.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="moodEntryDetailsHeading">Mood Entry</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{moodEntryEntity.id}</dd>
          <dt>
            <span id="date">Date</span>
          </dt>
          <dd>{moodEntryEntity.date ? <TextFormat value={moodEntryEntity.date} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="mood">Mood</span>
          </dt>
          <dd>{moodEntryEntity.mood}</dd>
          <dt>User</dt>
          <dd>{moodEntryEntity.user ? moodEntryEntity.user.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/mood-entry" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/mood-entry/${moodEntryEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default MoodEntryDetail;
