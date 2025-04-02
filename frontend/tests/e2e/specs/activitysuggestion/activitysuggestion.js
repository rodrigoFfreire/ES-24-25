describe('ActivitySuggestion', () => {
    beforeEach(() => {
      cy.deleteAllButArs()
      cy.createDemoEntities();
      cy.createDatabaseInfoForActivitySuggestion()
    });
  
    afterEach(() => {
      cy.deleteAllButArs()
    });
  
    it('create activitysuggestion', () => {
      const DESCRIPTION = 'NEW activity suggestion';
      const NAME = 'AS1';
      const REGION = 'Lisboa';
      const INSTITUTION_NAME = 'DEMO_INSTITUTION';
      
  
      cy.intercept('GET', '/activitySuggestions/volunteer').as('getSuggestions');
      cy.intercept('GET', '/institutions').as('institutions');
      cy.intercept('POST', '/activitySuggestions/institution/*').as('suggest');
  
      // volunteer login and check that there are 2 activitysuggestions
      cy.demoVolunteerLogin()
      cy.get('[data-cy="volunteerActivitySuggestions"]').click();
      cy.wait('@getSuggestions');
      cy.get('[data-cy="volunteerActivitySuggestionsTable"] tbody tr')
        .should('have.length', 2)
        .eq(0)
      cy.logout()
  
      // volunteer creates new activity suggestion
      cy.demoVolunteerLogin()
      cy.get('[data-cy="volunteerActivitySuggestions"]').click();
      cy.wait('@getSuggestions');
      cy.get('[data-cy="volunteerActivitySuggestionsTable"] tbody tr')
        .eq(0)
      cy.get('[data-cy="newActivitySuggestionButton"]').click();
      cy.wait('@institutions');
      cy.get('[data-cy="nameInput"]').type(NAME);
      cy.get('[data-cy="institutionSelect"]').click()
      cy.get('.v-list-item').contains('DEMO INSTITUTION').click();
      cy.get('[data-cy="descriptionInput"]').type(DESCRIPTION);
      cy.get('[data-cy="regionInput"]').type(REGION);
      cy.get('[data-cy="participantsInput"]').type(10);
      cy.get('#applicationDeadlineInput-input').click().get('button').filter(':visible').contains('13').click();
      cy.get('#startingDateInput-input').click().get('button').filter(':visible').contains('23').click();
      cy.get('#endingDateInput-input').click().get('button').filter(':visible').contains('28').click();
      cy.get('[data-cy="saveActivitySuggestion"]').click()
      cy.wait('@suggest')
      cy.logout()

      // volunteer check that there are 3 activity suggestions
      cy.demoVolunteerLogin()
      cy.get('[data-cy="volunteerActivitySuggestions"]').click();
      cy.wait('@getSuggestions');
      cy.get('[data-cy="volunteerActivitySuggestionsTable"] tbody tr')
        .should('have.length', 3)
        .eq(0)
      cy.logout()

      // volunteer check correct data in new activitysuggestion
      cy.demoVolunteerLogin()
      cy.get('[data-cy="volunteerActivitySuggestions"]').click();
      cy.wait('@getSuggestions');
      cy.get('[data-cy="volunteerActivitySuggestionsTable"] tbody tr')
        .eq(2).children().eq(0).should('contain', NAME);
      cy.get('[data-cy="volunteerActivitySuggestionsTable"] tbody tr')
        .eq(2).children().eq(1).should('contain', INSTITUTION_NAME)
      cy.get('[data-cy="volunteerActivitySuggestionsTable"] tbody tr')
        .eq(2).children().eq(2).should('contain', DESCRIPTION);
      cy.logout();
    });
});  