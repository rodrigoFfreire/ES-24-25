import { ISOtoString } from '@/services/ConvertDateService';

export default class ActivitySuggestion {
  id: number | null = null;
  name!: string;
  description!: string;
  region!: string;
  creationDate!: string;
  applicationDeadline!: string;
  startingDate!: string;
  endingDate!: string;
  participantsNumberLimit!: number;
  state!: string;
  institutionId!: number;
  volunteerId!: number;
  formattedApplicationDeadline!: string;
  formattedStartingDate!: string;
  formattedEndingDate!: string;

  constructor(jsonObj?: ActivitySuggestion) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.name = jsonObj.name;
      this.description = jsonObj.description;
      this.region = jsonObj.region;
      this.creationDate = ISOtoString(jsonObj.creationDate);
      this.applicationDeadline = jsonObj.applicationDeadline;
      this.startingDate = jsonObj.startingDate;
      this.endingDate = jsonObj.endingDate;
      this.participantsNumberLimit = jsonObj.participantsNumberLimit;
      this.state = jsonObj.state;
      this.institutionId = jsonObj.institutionId;
      this.volunteerId = jsonObj.volunteerId;
      if (jsonObj.applicationDeadline)
        this.formattedApplicationDeadline = ISOtoString(jsonObj.applicationDeadline);
      if (jsonObj.startingDate)
        this.formattedStartingDate = ISOtoString(jsonObj.startingDate);
      if (jsonObj.endingDate)
        this.formattedEndingDate = ISOtoString(jsonObj.endingDate);
    }
  }
}
