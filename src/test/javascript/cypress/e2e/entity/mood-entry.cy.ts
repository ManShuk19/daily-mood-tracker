import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('MoodEntry e2e test', () => {
  const moodEntryPageUrl = '/mood-entry';
  const moodEntryPageUrlPattern = new RegExp('/mood-entry(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  // const moodEntrySample = {"date":"2025-07-21","mood":"HAPPY"};

  let moodEntry;
  // let user;

  beforeEach(() => {
    cy.login(username, password);
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/users',
      body: {"login":"Z|?ami@RWXQ\\?S\\KH\\@f\\;GVer","firstName":"Emmanuel","lastName":"Boyer","email":"Gretchen41@hotmail.com","imageUrl":"capitalize busily place"},
    }).then(({ body }) => {
      user = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/mood-entries+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/mood-entries').as('postEntityRequest');
    cy.intercept('DELETE', '/api/mood-entries/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/users', {
      statusCode: 200,
      body: [user],
    });

  });
   */

  afterEach(() => {
    if (moodEntry) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/mood-entries/${moodEntry.id}`,
      }).then(() => {
        moodEntry = undefined;
      });
    }
  });

  /* Disabled due to incompatibility
  afterEach(() => {
    if (user) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/users/${user.id}`,
      }).then(() => {
        user = undefined;
      });
    }
  });
   */

  it('MoodEntries menu should load MoodEntries page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('mood-entry');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('MoodEntry').should('exist');
    cy.url().should('match', moodEntryPageUrlPattern);
  });

  describe('MoodEntry page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(moodEntryPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create MoodEntry page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/mood-entry/new$'));
        cy.getEntityCreateUpdateHeading('MoodEntry');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', moodEntryPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/mood-entries',
          body: {
            ...moodEntrySample,
            user: user,
          },
        }).then(({ body }) => {
          moodEntry = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/mood-entries+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/mood-entries?page=0&size=20>; rel="last",<http://localhost/api/mood-entries?page=0&size=20>; rel="first"',
              },
              body: [moodEntry],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(moodEntryPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(moodEntryPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details MoodEntry page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('moodEntry');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', moodEntryPageUrlPattern);
      });

      it('edit button click should load edit MoodEntry page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('MoodEntry');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', moodEntryPageUrlPattern);
      });

      it('edit button click should load edit MoodEntry page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('MoodEntry');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', moodEntryPageUrlPattern);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of MoodEntry', () => {
        cy.intercept('GET', '/api/mood-entries/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('moodEntry').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', moodEntryPageUrlPattern);

        moodEntry = undefined;
      });
    });
  });

  describe('new MoodEntry page', () => {
    beforeEach(() => {
      cy.visit(`${moodEntryPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('MoodEntry');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of MoodEntry', () => {
      cy.get(`[data-cy="date"]`).type('2025-07-22');
      cy.get(`[data-cy="date"]`).blur();
      cy.get(`[data-cy="date"]`).should('have.value', '2025-07-22');

      cy.get(`[data-cy="mood"]`).select('SAD');

      cy.get(`[data-cy="user"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        moodEntry = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', moodEntryPageUrlPattern);
    });
  });
});
