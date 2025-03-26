describe('VolunteerProfile', () => {
  beforeEach(() => {
    cy.deleteAllButArs();
    cy.createDemoEntities();
    cy.createDatabaseInfoForVolunteerProfile();
  });

  afterEach(() => {
    cy.deleteAllButArs();
  });

  it('Create volunteer profile', () => {
    const SHORT_BIO = "This is a short bio!";
    const TOTAL_PARTICIPATIONS = 4;
    const ACTIVITY1 = "A1";
    const ACTIVITY3 = "A3";

    cy.demoVolunteerLogin()

    // Intercept requests that originate from VolunteerProfileView
    cy.intercept('GET', '/activities').as('volunteerActivities')
    cy.intercept('GET', '/participations/volunteer').as('volunteerParticipations')
    cy.intercept('GET', '/profile/volunteer/*').as('volunteerProfile')

    // Click PROFILES -> VOLUNTEER PROFILE and wait requests
    cy.get('[data-cy="profiles"]').click()
    cy.get('[data-cy="volunteer-profile"]').click()
    cy.wait('@volunteerActivities')
    cy.wait('@volunteerParticipations')
    cy.wait('@volunteerProfile')

    // Click CREATE MY PROFILE and wait for dialog to open
    cy.get('[data-cy="createVolunteerProfileBtn"]').click()
    cy.wait(500)

    // Fill form
    cy.get('[data-cy="shortBioInput"]').type(SHORT_BIO);

    cy.get('[data-cy="pickParticipationsTable"] tbody tr')
      .eq(0) // First row (A1)
      .find('.v-data-table__checkbox .v-input--selection-controls__input')
      .click({ force: true })
    cy.get('[data-cy="pickParticipationsTable"] tbody tr')
      .eq(2) // Third row (A3)
      .find('.v-data-table__checkbox .v-input--selection-controls__input')
      .click({ force: true })

    // Click SAVE
    cy.intercept('POST', '/profile/volunteer').as('saveProfile')

    cy.get('[data-cy="saveVolunteerProfileBtn"]').click()
    cy.wait('@saveProfile')
    cy.wait(500)

    // Confirm Total Participations stat
    cy.get('[data-cy="totalParticipationsStat"] span')
      .should('have.text', TOTAL_PARTICIPATIONS.toString())

    // Confirm selected participations are in the table
    cy.get('[data-cy="selectedParticipationsTable"] tbody tr')
      .should('have.length', 2)
      .eq(0)
      .find('td').first()
      .should('contain', ACTIVITY1);

    cy.get('[data-cy="selectedParticipationsTable"] tbody tr')
      .eq(1)
      .find('td').first()
      .should('contain', ACTIVITY3);

    // TODO - Log out and visit as unauth'd user the profiles list and confirm its there
  })
})