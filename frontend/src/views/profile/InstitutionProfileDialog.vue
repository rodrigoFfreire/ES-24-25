<template>
    <v-dialog v-model="dialog" persistent width="1200">
      <v-card>
        <v-card-title>
          <span class="headline">New Institution Profile</span>
        </v-card-title>
  
        <v-card-text>
          <v-form ref="descriptionForm" lazy-validation>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  label="*Short description"
                  :rules="[
                    (v) => !!v || 'Short description is required',
                    (v) => v.length >= 10 || 'Must be at least 10 characters',
                  ]"
                  required
                  v-model="newProfile.shortDescription"
                ></v-text-field>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
  
        <h2>Selected Assessments</h2>
        <v-card class="table">
          <v-data-table
            :headers="headers"
            :items="assessments"
            :search="search"
            v-model="newProfile.selectedAssessments"
            disable-pagination
            show-select
            :hide-default-footer="true"
            :mobile-breakpoint="0"
          >
            <template v-slot:item.reviewDate="{ item }">
              {{ ISOtoString(item.reviewDate) }}
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
          <v-btn @click="$emit('institution-profile:close')"> Close </v-btn>
          <v-btn
            v-if="!!newProfile.shortDescription"
            @click="saveProfile"
          >
            Save
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </template>
  
  <script lang="ts">
  import { Vue, Component, Prop, Model, Ref } from 'vue-property-decorator';
  import RemoteServices from '@/services/RemoteServices';
  import InstitutionProfile from '@/models/profile/InstitutionProfile';
  import Assessment from '@/models/assessment/Assessment';
  import { ISOtoString } from '@/services/ConvertDateService';
  
  @Component({ methods: { ISOtoString } })
  export default class InstitutionProfileDialog extends Vue {
    @Prop({ required: true }) readonly institutionId!: number;
    @Model('dialog', Boolean) dialog!: boolean;
    @Ref('descriptionForm') readonly descriptionForm!: any;
  
    newProfile: InstitutionProfile = { shortDescription: '' } as InstitutionProfile;
    assessments: Assessment[] = [];
    search: string = '';
  
    headers: object = [
      { text: 'Volunteer Name', value: 'volunteerName', align: 'left', width: '30%' },
      { text: 'Review', value: 'review', align: 'left', width: '40%' },
      { text: 'Review Date', value: 'reviewDate', align: 'left', width: '30%' },
    ];
  
    async created() {
      await this.$store.dispatch('loading');
      try {
        this.assessments = await RemoteServices.getInstitutionAssessments(this.institutionId);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
      await this.$store.dispatch('clearLoading');
    }
  
    async saveProfile() {
      const isValid = await this.descriptionForm.validate();
      if (isValid) {
        try {
            const savedProfile = await RemoteServices.createInstitutionProfile(this.newProfile);
          this.$emit('institution-profile:create', savedProfile);
        } catch (error) {
          await this.$store.dispatch('error', error);
        }
      }
    }
  }
  </script>
  
  <style scoped lang="scss"></style>
  