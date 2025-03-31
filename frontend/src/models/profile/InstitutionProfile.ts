import Institution from '@/models/institution/Institution';
import Assessment from '@/models/assessment/Assessment';

export default class InstitutionProfile {
  id: number | null = null;
  shortDescription!: string;
  numMembers!: number;
  numActivities!: number;
  numAssessments!: number;
  numVolunteers!: number;
  averageRating!: number;
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
