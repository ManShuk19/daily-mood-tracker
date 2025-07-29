import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table, Card, CardBody, Badge, Row, Col } from 'reactstrap';
import './mood-entry.scss';
import { JhiItemCount, JhiPagination, TextFormat, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faSort,
  faSortDown,
  faSortUp,
  faSmile,
  faFrown,
  faAngry,
  faMeh,
  faFlushed,
  faPlus,
  faChartLine,
} from '@fortawesome/free-solid-svg-icons';
import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities } from './mood-entry.reducer';

export const MoodEntry = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const moodEntryList = useAppSelector(state => state.moodEntry.entities);
  const loading = useAppSelector(state => state.moodEntry.loading);
  const totalItems = useAppSelector(state => state.moodEntry.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = paginationState.sort;
    const order = paginationState.order;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  const getMoodIcon = mood => {
    switch (mood) {
      case 'HAPPY':
        return faSmile;
      case 'SAD':
        return faFrown;
      case 'ANGRY':
        return faAngry;
      case 'NEUTRAL':
        return faMeh;
      case 'ANXIOUS':
        return faFlushed;
      default:
        return faMeh;
    }
  };

  const getMoodColor = mood => {
    switch (mood) {
      case 'HAPPY':
        return 'success';
      case 'SAD':
        return 'danger';
      case 'ANGRY':
        return 'warning';
      case 'NEUTRAL':
        return 'secondary';
      case 'ANXIOUS':
        return 'info';
      default:
        return 'secondary';
    }
  };

  const getMoodLabel = mood => {
    switch (mood) {
      case 'HAPPY':
        return 'Happy';
      case 'SAD':
        return 'Sad';
      case 'ANGRY':
        return 'Angry';
      case 'NEUTRAL':
        return 'Neutral';
      case 'ANXIOUS':
        return 'Anxious';
      default:
        return mood;
    }
  };

  return (
    <div>
      <Row className="mb-4">
        <Col md="8">
          <h2 id="mood-entry-heading" data-cy="MoodEntryHeading" className="mb-0">
            <FontAwesomeIcon icon={faChartLine} className="me-2" />
            My Mood History
          </h2>
          <p className="text-muted">Track your emotional journey over time</p>
        </Col>
        <Col md="4" className="text-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh
          </Button>
          <Link to="/mood-entry/new" className="btn btn-primary" data-cy="entityCreateButton">
            <FontAwesomeIcon icon={faPlus} />
            &nbsp; Add Mood Entry
          </Link>
        </Col>
      </Row>

      <Card>
        <CardBody>
          <div className="table-responsive">
            {moodEntryList && moodEntryList.length > 0 ? (
              <Table responsive className="mood-table">
                <thead>
                  <tr>
                    <th className="hand" onClick={sort('date')}>
                      Date <FontAwesomeIcon icon={getSortIconByFieldName('date')} />
                    </th>
                    <th className="hand" onClick={sort('mood')}>
                      Mood <FontAwesomeIcon icon={getSortIconByFieldName('mood')} />
                    </th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {moodEntryList.map((moodEntry, i) => (
                    <tr key={`entity-${i}`} data-cy="entityTable" className="mood-entry-row">
                      <td>
                        <div className="d-flex align-items-center">
                          <div className="date-badge me-3">
                            <div className="day">{new Date(moodEntry.date).getDate()}</div>
                            <div className="month">{new Date(moodEntry.date).toLocaleDateString('en-US', { month: 'short' })}</div>
                          </div>
                          <div>
                            <div className="fw-bold">{new Date(moodEntry.date).toLocaleDateString('en-US', { weekday: 'long' })}</div>
                            <div className="text-muted small">{new Date(moodEntry.date).getFullYear()}</div>
                          </div>
                        </div>
                      </td>
                      <td>
                        <div className="d-flex align-items-center">
                          <FontAwesomeIcon
                            icon={getMoodIcon(moodEntry.mood)}
                            className={`me-2 text-${getMoodColor(moodEntry.mood)}`}
                            size="lg"
                          />
                          <Badge color={getMoodColor(moodEntry.mood)} className="mood-badge">
                            {getMoodLabel(moodEntry.mood)}
                          </Badge>
                        </div>
                      </td>
                      <td>
                        <div className="btn-group" role="group">
                          <Button
                            tag={Link}
                            to={`/mood-entry/${moodEntry.id}`}
                            color="info"
                            size="sm"
                            data-cy="entityDetailsButton"
                            className="me-1"
                          >
                            <FontAwesomeIcon icon="eye" />
                          </Button>
                          <Button
                            tag={Link}
                            to={`/mood-entry/${moodEntry.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                            color="primary"
                            size="sm"
                            data-cy="entityEditButton"
                            className="me-1"
                          >
                            <FontAwesomeIcon icon="pencil-alt" />
                          </Button>
                          <Button
                            onClick={() =>
                              (window.location.href = `/mood-entry/${moodEntry.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                            }
                            color="danger"
                            size="sm"
                            data-cy="entityDeleteButton"
                          >
                            <FontAwesomeIcon icon="trash" />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            ) : (
              !loading && (
                <div className="text-center py-5">
                  <FontAwesomeIcon icon={faChartLine} size="4x" className="text-muted mb-3" />
                  <h4 className="text-muted">No Mood Entries Found</h4>
                  <p className="text-muted">Start tracking your moods to see your emotional journey here.</p>
                  <Link to="/mood-entry/new" className="btn btn-primary">
                    <FontAwesomeIcon icon={faPlus} /> Add Your First Mood Entry
                  </Link>
                </div>
              )
            )}
          </div>
        </CardBody>
      </Card>

      {totalItems ? (
        <div className={moodEntryList && moodEntryList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default MoodEntry;
