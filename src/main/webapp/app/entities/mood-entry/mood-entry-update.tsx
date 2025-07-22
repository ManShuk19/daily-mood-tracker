import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, FormText, Row } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { MoodType } from 'app/shared/model/enumerations/mood-type.model';
import { createEntity, getEntity, reset, updateEntity } from './mood-entry.reducer';

export const MoodEntryUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const users = useAppSelector(state => state.userManagement.users);
  const moodEntryEntity = useAppSelector(state => state.moodEntry.entity);
  const loading = useAppSelector(state => state.moodEntry.loading);
  const updating = useAppSelector(state => state.moodEntry.updating);
  const updateSuccess = useAppSelector(state => state.moodEntry.updateSuccess);
  const moodTypeValues = Object.keys(MoodType);

  const handleClose = () => {
    navigate(`/mood-entry${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getUsers({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }

    const entity = {
      ...moodEntryEntity,
      ...values,
      user: users.find(it => it.id.toString() === values.user?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          mood: 'HAPPY',
          ...moodEntryEntity,
          user: moodEntryEntity?.user?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="dailyMoodTrackerApp.moodEntry.home.createOrEditLabel" data-cy="MoodEntryCreateUpdateHeading">
            Create or edit a Mood Entry
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? <ValidatedField name="id" required readOnly id="mood-entry-id" label="ID" validate={{ required: true }} /> : null}
              <ValidatedField
                label="Date"
                id="mood-entry-date"
                name="date"
                data-cy="date"
                type="date"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField label="Mood" id="mood-entry-mood" name="mood" data-cy="mood" type="select">
                {moodTypeValues.map(moodType => (
                  <option value={moodType} key={moodType}>
                    {moodType}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField id="mood-entry-user" name="user" data-cy="user" label="User" type="select" required>
                <option value="" key="0" />
                {users
                  ? users.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.login}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>This field is required.</FormText>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/mood-entry" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default MoodEntryUpdate;
