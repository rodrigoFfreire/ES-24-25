const credentials = {
  user: Cypress.env('psql_db_username'),
  host: Cypress.env('psql_db_host'),
  database: Cypress.env('psql_db_name'),
  password: Cypress.env('psql_db_password'),
  port: Cypress.env('psql_db_port'),
};

const INSTITUTION_COLUMNS = "institutions (id, active, confirmation_token, creation_date, email, name, nif, token_generation_date)";
const USER_COLUMNS = "users (user_type, id, creation_date, name, role, state, institution_id)";
const AUTH_USERS_COLUMNS = "auth_users (auth_type, id, active, email, username, user_id)";
const ACTIVITY_COLUMNS = "activity (id, application_deadline, creation_date, description, ending_date, name, participants_number_limit, region, starting_date, state, institution_id)";
const ACTIVITY_SUGGESTION_COLUMNS = "activity_suggestion (id, application_deadline, creation_date, description, ending_date, name, participants_number_limit, region, starting_date, state, institution_id, volunteer_id)";
const ENROLLMENT_COLUMNS = "enrollment (id, enrollment_date_time, motivation, activity_id, volunteer_id)"
const PARTICIPATION_COLUMNS = "participation (id, acceptance_date, member_rating, member_review, volunteer_rating, volunteer_review, activity_id, volunteer_id)"
const ASSESSMENT_COLUMNS = "assessment (id, review, review_date, institution_id, volunteer_id)";
const REPORT_COLUMNS = "report (id, justification, activity_id, volunteer_id)";
const INSTITUTION_PROFILE_COLUMNS = "institution_profile (institution_id, short_description)";

const now = new Date();
const tomorrow = new Date(now);
tomorrow.setDate(now.getDate() + 1);
const dayAfterTomorrow = new Date(now);
dayAfterTomorrow.setDate(now.getDate() + 2);
const yesterday = new Date(now);
yesterday.setDate(now.getDate() - 1);
const dayBeforeYesterday = new Date(now);
dayBeforeYesterday.setDate(now.getDate() - 2);
const twoDaysBeforeYesterday = new Date(now);
twoDaysBeforeYesterday.setDate(now.getDate() - 3);

Cypress.Commands.add('deleteAllButArs', () => {
  cy.task('queryDatabase', {
    query: "DELETE FROM ACTIVITY_SUGGESTION",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM ASSESSMENT",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM PARTICIPATION",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM ENROLLMENT",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM REPORT",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM ACTIVITY",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM institution_profile", // ← esta linha resolve o erro
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM VOLUNTEER_PROFILE",
    credentials: credentials
  })
  cy.task('queryDatabase', {
    query: "DELETE FROM AUTH_USERS WHERE NOT (username = 'ars')",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM USERS WHERE NOT (name = 'ars')",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM INSTITUTIONS",
    credentials: credentials,
  });
});

Cypress.Commands.add('createDemoEntities', () => {
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + INSTITUTION_COLUMNS + generateInstitutionTuple(1, "DEMO INSTITUTION", "000000000"),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + USER_COLUMNS + generateUserTuple(2, "MEMBER","DEMO-MEMBER", "MEMBER", 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + AUTH_USERS_COLUMNS + generateAuthUserTuple(2, "DEMO", "demo-member", 2),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + USER_COLUMNS + generateUserTuple(3, "VOLUNTEER","DEMO-VOLUNTEER", "VOLUNTEER", "NULL"),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + AUTH_USERS_COLUMNS + generateAuthUserTuple(3, "DEMO", "demo-volunteer", 3),
    credentials: credentials,
  })
});

Cypress.Commands.add('createDatabaseInfoForEnrollments', () => {
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(1, "A1", "Enrollment is open",  tomorrow.toISOString(), tomorrow.toISOString(),
        tomorrow.toISOString(),1, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(2, "A2", "Enrollment is open and it is already enrolled",  tomorrow.toISOString(), tomorrow.toISOString(),
        tomorrow.toISOString(),1, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(3, "A3", "Enrollment is closed",  yesterday.toISOString(), tomorrow.toISOString(),
        tomorrow.toISOString(),1, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(5, 2, 3),
    credentials: credentials,
  })
});

Cypress.Commands.add('createDatabaseInfoForReports', () => {
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(1, "A1", "Enrollment is open",  tomorrow.toISOString(), tomorrow.toISOString(),
        tomorrow.toISOString(),1, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(2, "A2", "Enrollment is open and it is already enrolled",  tomorrow.toISOString(), tomorrow.toISOString(),
        tomorrow.toISOString(),1, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(3, "A3", "Enrollment is closed",  yesterday.toISOString(), tomorrow.toISOString(),
        tomorrow.toISOString(),1, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(5, 2, 3),
    credentials: credentials,
  })
});

Cypress.Commands.add('createDatabaseInfoForParticipations', () => {
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + USER_COLUMNS + generateUserTuple(4, "VOLUNTEER","DEMO-VOLUNTEER-2", "VOLUNTEER", "NULL"),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + AUTH_USERS_COLUMNS + generateAuthUserTuple(4, "DEMO", "demo-volunteer-2", 4),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + USER_COLUMNS + generateUserTuple(5, "VOLUNTEER","DEMO-VOLUNTEER-3", "VOLUNTEER", "NULL"),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + AUTH_USERS_COLUMNS + generateAuthUserTuple(5, "DEMO", "demo-volunteer-3", 5),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(1, "A1", "Has vacancies",  dayBeforeYesterday.toISOString(), yesterday.toISOString(),
        yesterday.toISOString(),2, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(2, "A2", "Has no vacancies",  dayBeforeYesterday.toISOString(), yesterday.toISOString(),
        yesterday.toISOString(),1, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(1, 1, 3),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(2, 1, 4),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(3, 2, 3),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(4, 2, 5),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + PARTICIPATION_COLUMNS + generateParticipationTuple(5, "", 1, 4),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + PARTICIPATION_COLUMNS + generateParticipationTuple(6, "", 2, 3),
    credentials: credentials,
  })
});

Cypress.Commands.add('createDatabaseInfoForAssessments', () => {
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + INSTITUTION_COLUMNS + generateInstitutionTuple(2, "DEMO INSTITUTION-2", "000000002"),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(1, "A1", "Same institution is enrolled and participates", dayBeforeYesterday.toISOString(), yesterday.toISOString(),
        yesterday.toISOString(),1, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(2, "A2", "Same institution is enrolled and participates",  dayBeforeYesterday.toISOString(), yesterday.toISOString(),
        yesterday.toISOString(),2, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(3, "A3", "Same institution is enrolled and does not participate",  dayBeforeYesterday.toISOString(), yesterday.toISOString(),
        yesterday.toISOString(),3, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(4, "A4", "Same institution is not enrolled",  dayBeforeYesterday.toISOString(), yesterday.toISOString(),
        yesterday.toISOString(),3, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(5, "A5", "Same institution before end date",  dayBeforeYesterday.toISOString(), yesterday.toISOString(),
        tomorrow.toISOString(),3, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(6, "A6", "Other institution is enrolled and participates",  dayBeforeYesterday.toISOString(), yesterday.toISOString(),
        yesterday.toISOString(),3, 2),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(1, 1, 3),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(2, 2, 3),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(3, 3, 3),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(4, 6, 3),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + PARTICIPATION_COLUMNS + generateParticipationTuple(1, "", 1, 3),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + PARTICIPATION_COLUMNS + generateParticipationTuple(2, "", 2, 3),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + PARTICIPATION_COLUMNS + generateParticipationTuple(3, "", 6, 3),
    credentials: credentials,
  })
});

Cypress.Commands.add('createDatabaseInfoForActivitySuggestion', () => {
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_SUGGESTION_COLUMNS + generateActivitySuggestionTuple(2, "Leilão de Latidos", "Leilão solidário para angariar fundos",  tomorrow.toISOString(), tomorrow.toISOString(),
      tomorrow.toISOString(), 5, 1, 3),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ACTIVITY_SUGGESTION_COLUMNS + generateActivitySuggestionTuple(1, "Cãocerto solidário", "Concerto Solidário",  tomorrow.toISOString(), tomorrow.toISOString(),
      tomorrow.toISOString(), 5, 1, 3),
    credentials: credentials,
  })
});

Cypress.Commands.add('createDatabaseInfoForVolunteerAssessments', () => {
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ASSESSMENT_COLUMNS + generateAssessmentTuple(1, "Muito bom!", 2, 3),
    credentials: credentials,
  })
});

Cypress.Commands.add('createDatabaseInfoForInstitutionProfiles', () => {
  // Existing setup code for activities, enrollments, participations, and assessments
  for (let i = 1; i <= 4; i++) {
    cy.task('queryDatabase', {
      query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(i, `A${i}`, `Descrição ${i}`, "2025-04-10 12:00:00", "2025-04-12 12:00:00", "2025-04-15 12:00:00", 10, 1),
      credentials
    });
    cy.task('queryDatabase', {
      query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(i, i, 3),
      credentials
    });
    cy.task('queryDatabase', {
      query: "INSERT INTO " + PARTICIPATION_COLUMNS + generateParticipationTuple(i, i, 3),
      credentials
    });
    cy.task('queryDatabase', {
      query: "INSERT INTO " + ASSESSMENT_COLUMNS + generateAssessmentTuple(i, `Review ${i}`, 1, 3),
      credentials
    });
  }

  // New code to create additional institution, member, and profile
  // Create new institution (id 5)
  cy.task('queryDatabase', {
    query: "INSERT INTO " + INSTITUTION_COLUMNS + generateInstitutionTuple(5, "New Demo Institution", "000000003"),
    credentials: credentials,
  });

  // Create new member (user 6) linked to institution 5
  cy.task('queryDatabase', {
    query: "INSERT INTO " + USER_COLUMNS + generateUserTuple(6, "MEMBER", "NEW-MEMBER", "MEMBER", 5),
    credentials: credentials,
  });

  // Create auth user for the new member
  cy.task('queryDatabase', {
    query: "INSERT INTO " + AUTH_USERS_COLUMNS + generateAuthUserTuple(6, "DEMO", "new-member", 6),
    credentials: credentials,
  });

  cy.task('queryDatabase', {
    query: "INSERT INTO " + INSTITUTION_PROFILE_COLUMNS + generateInstitutionProfileTuple(5, "This is just a short description"),
    credentials: credentials,
  });
});

Cypress.Commands.add('createDatabaseInfoForVolunteerProfile', () => {
  // Activties (5)
  cy.task('queryDatabase', {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(1, "A1", "Activity 1 Description",
        dayBeforeYesterday.toISOString(), yesterday.toISOString(), yesterday.toISOString(), 1, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(2, "A2", "Activity 2 Description",
        dayBeforeYesterday.toISOString(), yesterday.toISOString(), yesterday.toISOString(), 2, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(3, "A3", "Activity 3 Description",
        dayBeforeYesterday.toISOString(), yesterday.toISOString(), yesterday.toISOString(), 3, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(4, "A4", "Activity 4 Description",
        dayBeforeYesterday.toISOString(), yesterday.toISOString(), yesterday.toISOString(), 2, 1),
    credentials: credentials,
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(5, "A5", "Activity 5 Description",
        dayBeforeYesterday.toISOString(), yesterday.toISOString(), tomorrow.toISOString(), 1, 1),
    credentials: credentials,
  })

  // Enrollments (5)
  cy.task('queryDatabase', {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(1, 1, 3),
    credentials: credentials
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(2, 2, 3),
    credentials: credentials
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(3, 3, 3),
    credentials: credentials
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(4, 4, 3),
    credentials: credentials
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(5, 5, 3),
    credentials: credentials
  })

  // Participations (4)
  cy.task('queryDatabase', {
    query: "INSERT INTO " + PARTICIPATION_COLUMNS + generateParticipationTuple(1, "Very Good job!", 1, 3),
    credentials: credentials
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + PARTICIPATION_COLUMNS + generateParticipationTuple(2, "Very Good job!", 2, 3),
    credentials: credentials
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + PARTICIPATION_COLUMNS + generateParticipationTuple(3, "Very Good job!", 3, 3),
    credentials: credentials
  })
  cy.task('queryDatabase', {
    query: "INSERT INTO " + PARTICIPATION_COLUMNS + generateParticipationTuple(4, "", 5, 3),
    credentials: credentials
  })

  // Assessments (1)
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + ASSESSMENT_COLUMNS + generateAssessmentTuple(1, "Foi Muito bom!", 1, 3),
    credentials: credentials,
  })
})

function generateAuthUserTuple(id, authType, username, userId) {
  return "VALUES ('"
      + authType + "', '"
      + id + "', 't', 'demo_member@mail.com','"
      + username + "', '"
      + userId + "')"
}

function generateUserTuple(id, userType, name, role, institutionId) {
  return "VALUES ('"
      + userType + "', '"
      + id + "', '2022-02-06 17:58:21.419878', '"
      + name + "', '"
      + role + "', 'ACTIVE', "
      + institutionId + ")";
}

function generateInstitutionTuple(id, name, nif) {
  return "VALUES ('"
      + id + "', 't', 'abca428c09862e89', '2022-08-06 17:58:21.402146','demo_institution@mail.com', '" +
      name + "', '" +
      nif + "', '2024-02-06 17:58:21.402134')";
}

function generateActivityTuple(id, name, description, deadline, start, end, participants, institutionId) {
  return "VALUES ('"
      + id + "', '"
      + deadline +
      "', '2022-08-06 17:58:21.402146', '" +
      description + "', '"
      + end + "', '"
      + name + "', '" +
      participants +
      "', 'Lisbon',  '"
      + start + "', 'APPROVED', " +
      institutionId + ")";
}

function generateActivitySuggestionTuple(id, name, description, deadline, start, end, participants, institutionId, volunteerId) {
  return "VALUES ('"
    + id + "', '"
    + deadline +
    "', '2022-08-06 17:58:21.402146', '" +
    description + "', '"
    + end + "', '"
    + name + "', '" +
    participants +
    "', 'Lisbon',  '"
    + start + "', 'IN_REVIEW', '" +
    institutionId + "', '" +
    volunteerId + "')";
}

function generateEnrollmentTuple(id, activityId, volunteerId) {
  return "VALUES ("
      + id + ", '2022-08-06 17:58:21.402146', 'sql-inserted-motivation', "
      + activityId + ", "
      + volunteerId + ")";
}

function generateParticipationTuple(id, memberReview, activityId, volunteerId) {
  return "VALUES ("
      + id + ", '2024-02-06 18:51:37.595713', 5, '" +
      memberReview + "', 5, 'Foi uma otima experiencia', " +
      activityId + ", " +
      volunteerId + ")";
}

function generateAssessmentTuple(id, review, institutionId, volunteerId) {
  return "VALUES (" + id + ", '" + review + "', '2024-02-07 18:51:37.595713', '" + institutionId + "', " + volunteerId + ")";
}

function generateInstitutionProfileTuple(institutionId, shortDescription) {
    return "VALUES (" + institutionId + ", '" + shortDescription + "')";
}

