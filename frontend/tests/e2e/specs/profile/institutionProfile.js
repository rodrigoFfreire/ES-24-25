describe('Seed Test for Institution Profiles', () => {
    it('create institution profile', () => {
      cy.deleteAllButArs();
      cy.createDemoEntities();
      cy.createDatabaseInfoForInstitutionProfiles();
  
      const DESCRIPTION = 'DEMO Institution Description';
  
      // login as demo-member and go to institution profiles page
      cy.demoMemberLogin();
      cy.get('[data-cy="institution"]').click();
      cy.get('[data-cy="profiles"]').click();
      cy.get('[data-cy="member-profile"]').click();
  
      // create new institution profile
      cy.get('[data-cy="createInstitutionProfile"]').click();
      cy.get('[data-cy="profileDescription"]').type(DESCRIPTION);
      cy.get('[data-cy="assessmentCheckbox-0"]').click();
      cy.get('[data-cy="assessmentCheckbox-1"]').click();
      cy.contains('button', 'Save').click();

      // check number of activities shown is correct
      cy.contains('p', 'Total Assessments')
        .parent()
        .prev()
        .find('span')
        .should('have.text', '4');

      // check if one of the selected assessments is shown
      cy.get('[data-cy="institutionAssessmentsTable"]')
        .contains('td', 'Review 1')
        .should('exist');

      cy.get('[data-cy="institutionAssessmentsTable"]')
        .contains('td', '2024-02-07 18:51')
        .should('exist');
    });
  });
  