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

    // TODO - Confirm participation list and statistic after creating profile

    // TODO - Log out and visit as unauth'd user the profiles list and confirm its there
  })
})