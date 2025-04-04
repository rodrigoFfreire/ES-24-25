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
      const INSTITUTION_NAME = 'DEMO INSTITUTION';
      
  
      cy.intercept('GET', '/activitySuggestions/volunteer').as('getSuggestions');
      cy.intercept('GET', '/institutions').as('institutions');
      cy.intercept('POST', '/activitySuggestions/institution/*').as('suggest');
  
      // volunteer login and check that there are 2 activitysuggestions
      cy.demoVolunteerLogin()
      cy.get('[data-cy="volunteerActivitySuggestions"]').click();
      cy.wait('@getSuggestions');
      cy.wait('@institutions');
      cy.get('[data-cy="volunteerActivitySuggestionsTable"] tbody tr')
        .should('have.length', 2)
        .eq(0)
  
      // volunteer creates new activity suggestion
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

      // volunteer check that there are 3 activity suggestions
      cy.get('[data-cy="volunteerActivitySuggestionsTable"] tbody tr')
        .should('have.length', 3)
        .eq(0)

      // volunteer check correct data in new activitysuggestion
      
      cy.get('[data-cy="volunteerActivitySuggestionsTable"] tbody tr')
        .eq(0).children().eq(0).should('contain', NAME);
      cy.get('[data-cy="volunteerActivitySuggestionsTable"] tbody tr')
        .eq(0).children().eq(1).should('have.text', INSTITUTION_NAME)
      cy.get('[data-cy="volunteerActivitySuggestionsTable"] tbody tr')
        .eq(0).children().eq(2).should('contain', DESCRIPTION);
      cy.logout();
    });

    it('approve and reject activity suggestions as member', () => {
      cy.intercept('GET', '/activitySuggestions/institution/*').as('getSuggestions');
      cy.intercept('GET', '/activitySuggestions/volunteer').as('VolunteerSuggestions');
      cy.intercept('PUT', '/activitySuggestions/institution/*/*/approve').as('approve');
      cy.intercept('PUT', '/activitySuggestions/institution/*/*/reject').as('reject');
  
      // login as member and go to activity suggestions view
      cy.demoMemberLogin();
      cy.get('[data-cy="institution"]').click();
      cy.get('[data-cy="activitysuggestions"]').click();
      cy.wait('@getSuggestions');
      
      // check that the first suggestion is in IN_REVIEW state
      cy.get('tbody tr').first().children().eq(9).should('contain', 'IN_REVIEW');
      
      // approve the suggestion
      cy.get('[data-cy="approveButton"]').first().click();
      cy.wait('@approve');
      cy.get('tbody tr').first().children().eq(9).should('contain', 'APPROVED');
      cy.logout();
      
      // login as volunteer and check that the state is now APPROVED
      cy.demoVolunteerLogin();
      cy.get('[data-cy="volunteerActivitySuggestions"]').click();
      cy.wait('@VolunteerSuggestions');
      cy.get('[data-cy="volunteerActivitySuggestionsTable"] tbody tr')
      .eq(0).children().eq(9).should('contain', 'APPROVED');
      cy.logout();
      
      // login again as member to reject the next suggestion
      cy.demoMemberLogin();
      cy.get('[data-cy="institution"]').click();
      cy.get('[data-cy="activitysuggestions"]').click();
      cy.wait('@getSuggestions');
  
      // reject the suggestion
      cy.get('[data-cy="rejectButton"]').first().click();
      cy.wait('@reject');
      cy.get('tbody tr').first().children().eq(9).should('contain', 'REJECTED');
      cy.logout();
  
      // login as volunteer and check that the state is now REJECTED
      cy.demoVolunteerLogin();
      cy.get('[data-cy="volunteerActivitySuggestions"]').click();
      cy.wait('@VolunteerSuggestions');
      cy.get('[data-cy="volunteerActivitySuggestionsTable"] tbody tr')
        .eq(0).children().eq(9).should('contain', 'REJECTED');
    });
  });
  