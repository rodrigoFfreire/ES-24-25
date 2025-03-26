<template>
  <v-dialog v-model="dialog" persistent width="1200">
    <v-card>
      <v-card-title>
        <span class="headline">New Volunteer Profile</span>
      </v-card-title>

      <v-card-text>
        <v-form ref="shortBioForm" lazy-validation>
          <v-row>
            <v-col cols="12">
              <v-text-field
                label="*Short bio"
                :rules="[
                  (v) => !!v || 'Short bio is required',
                  (v) => v.length >= 10 || 'Must be at least 10 characters',
                ]"
                required
                v-model="newProfile.shortBio"
                data-cy="shortBioInput"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <h2>Selected Participations</h2>
      <v-card class="table">
        <v-data-table
          :headers="headers"
          :items="participations"
          :search="search"
          v-model="newProfile.selectedParticipations"
          disable-pagination
          show-select
          :hide-default-footer="true"
          :mobile-breakpoint="0"
          data-cy="pickParticipationsTable"
        >
          <template v-slot:item.activityName="{ item }">
            {{ activityName(item) }}
          </template>
          <template v-slot:item.institutionName="{ item }">
            {{ institutionName(item) }}
          </template>
          <template v-slot:item.memberRating="{ item }">
            {{ getMemberRating(item) }}
          </template>
          <template v-slot:top>
            <v-card-title>
              <v-text-field
                v-model="search"
                append-icon="search"
                label="Search"
                class="mx-2"
              />
              <v-spacer />
            </v-card-title>
          </template>
        </v-data-table>
      </v-card>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="$emit('volunteer-profile:close')"> Close </v-btn>
        <v-btn
          data-cy="saveVolunteerProfileBtn"
          v-if="!!newProfile.shortBio"
          @click="saveProfile"
          >Save
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Vue, Component, Prop, Model, Ref } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import VolunteerProfile from '@/models/profile/VolunteerProfile';
import Activity from '@/models/activity/Activity';
import Participation from '@/models/participation/Participation';

@Component
export default class VolunteerProfileDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;

  @Prop({ required: true }) readonly activities!: Activity[];
  @Prop(Function) activityName!: (participation: Participation) => string;
  @Prop(Function) institutionName!: (participation: Participation) => string;
  @Prop(Function) getMemberRating!: (participation: Participation) => string;

  @Ref('shortBioForm') readonly shortBioForm!: any;

  newProfile: VolunteerProfile = { shortBio: '' } as VolunteerProfile;
  participations: Participation[] = [];

  search: string = '';
  headers: object = [
    {
      text: 'Activity Name',
      value: 'activityName',
      align: 'left',
      width: '20%',
    },
    {
      text: 'Institution',
      value: 'institutionName',
      align: 'left',
      width: '20%',
    },
    {
      text: 'Rating',
      value: 'memberRating',
      align: 'left',
      width: '20%',
    },
    {
      text: 'Review',
      value: 'memberReview',
      align: 'left',
      width: '40%',
    },
    {
      text: 'Acceptance Date',
      value: 'acceptanceDate',
      align: 'left',
      width: '20%',
    },
  ];

  async created() {
    await this.$store.dispatch('loading');

    try {
      this.participations = await RemoteServices.getVolunteerParticipations();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async saveProfile() {
    const isValid = await this.shortBioForm.validate();
    if (isValid) {
      try {
        const savedProfile = await RemoteServices.createVolunteerProfile(
          this.newProfile,
        );
        this.$emit('volunteer-profile:create', savedProfile);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }
}
</script>

<style scoped lang="scss"></style>
