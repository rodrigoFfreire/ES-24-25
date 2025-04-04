import Institution from '@/models/institution/Institution';
import Assessment from '@/models/assessment/Assessment';

export default class InstitutionProfile {
  id: number | null = null;
  shortDescription!: string;
  numMembers: number = 0;
    numActivities: number = 0;
    numAssessments: number = 0;
    numVolunteers: number = 0;
    averageRating: number = 0;
  institution!: Institution;
  selectedAssessments: Assessment[] = [];

  constructor(jsonObj?: InstitutionProfile) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.shortDescription = jsonObj.shortDescription;
      this.numMembers = jsonObj.numMembers;
      this.numActivities = jsonObj.numActivities;
      this.numAssessments = jsonObj.numAssessments;
      this.numVolunteers = jsonObj.numVolunteers;
      this.averageRating = jsonObj.averageRating;
      this.institution = jsonObj.institution;
      this.selectedAssessments = jsonObj.selectedAssessments.map(
        (assessment: Assessment) => {
          return new Assessment(assessment);
        },
      );
    }
  }
}
