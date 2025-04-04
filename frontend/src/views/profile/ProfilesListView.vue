<template>
  <div>
    <v-card class="table">
      <v-card-title>
        <h2>Volunteer Profiles</h2>
      </v-card-title>
      <v-data-table
        :headers="headersVolunteerProfile"
        :items="volunteerProfiles"
        :search="search"
        disable-pagination
        :hide-default-footer="true"
        :mobile-breakpoint="0"
      >
        <template v-slot:item.volunteer.creationDate="{ item }">
          {{ ISOtoString(item.volunteer.creationDate) }}
        </template>
        <template v-slot:item.volunteer.lastAccess="{ item }">
          {{ ISOtoString(item.volunteer.lastAccess) }}
        </template>
        <template v-slot:item.action="{ item }">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                v-on="on"
                data-cy="goToProfileBtn"
                @click="goToProfile(item.volunteer.id)"
                >mdi-eye
              </v-icon>
            </template>
            <span>View volunteer profile</span>
          </v-tooltip>
        </template>
        <template v-slot:top>
          <v-card-title>
            <v-text-field
              v-model="search"
              append-icon="search"
              label="Search"
              class="mx-2"
            />
          </v-card-title>
        </template>
      </v-data-table>
    </v-card>
    <!-- Institution Profiles -->
    <v-card class="table" data-cy="institution-profiles-card">
      <v-card-title>
        <h2 data-cy="institution-profiles-title">Institution Profiles</h2>
      </v-card-title>
      <v-data-table
        :headers="headersInstitutionProfile"
        :items="institutionProfiles"
        :search="search"
        disable-pagination
        :hide-default-footer="true"
        :mobile-breakpoint="0"
        data-cy="institution-profiles-table"
      >
        <template v-slot:item.institution.creationDate="{ item }">
          <span data-cy="institution-creation-date">{{ ISOtoString(item.institution.creationDate) }}</span>
        </template>
        <template v-slot:item.institution.active="{ item }">
          <span data-cy="institution-active-status"></span>
        </template>
        <template v-slot:item.action="{ item }">
          <v-btn
            icon
            data-cy="view-institution-profile"
            @click="viewInstitutionDetails(item)"
          >
            <v-icon class="pr-2" data-cy="view-institution-icon">visibility</v-icon>
          </v-btn>
        </template>
        <template v-slot:top>
          <v-card-title>
            <v-text-field
              v-model="search"
              append-icon="search"
              label="Search"
              class="mx-2"
              data-cy="institution-search-field"
            />
          </v-card-title>
        </template>
        <!-- You might want to add these for individual rows or columns -->
        <template v-slot:item.institution.name="{ item }">
          <span data-cy="institution-name">{{ item.institution.name }}</span>
        </template>
        <template v-slot:item.institution.id="{ item }">
          <span data-cy="institution-id">{{ item.institution.id }}</span>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import { ISOtoString } from "../../services/ConvertDateService";
import RemoteServices from '../../services/RemoteServices';
import InstitutionProfile from '@/models/profile/InstitutionProfile';
import VolunteerProfile from '@/models/profile/VolunteerProfile';

@Component({
  methods: { ISOtoString },
})
export default class ProfilesListView extends Vue {
  volunteerProfiles: VolunteerProfile[] = []; // Will store volunteer profiles
  institutionProfiles: InstitutionProfile[] = []; // Will store institution profiles

  search: string = '';

  headersVolunteerProfile: object = [
    { text: 'Name', value: 'volunteer.name', align: 'left', width: '10%' },
    {
      text: 'Short Bio',
      value: 'shortBio',
      align: 'left',
      width: '40%',
    },
    {
      text: 'Registration Date',
      value: 'volunteer.creationDate',
      align: 'left',
      width: '10%',
    },
    {
      text: 'Last Access',
      value: 'volunteer.lastAccess',
      align: 'left',
      width: '10%',
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      sortable: false,
      width: '5%',
    },
  ];

  headersInstitutionProfile: object = [
    { text: 'Name', value: 'institution.name', align: 'left', width: '10%' },
    {
      text: 'Short Description',
      value: 'shortDescription',
      align: 'left',
      width: '40%',
    },
    {
      text: 'Registration Date',
      value: 'institution.creationDate',
      align: 'left',
      width: '10%',
    },
    {
      text: 'Active',
      value: 'institution.active',
      align: 'left',
      width: '10%',
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      sortable: false,
      width: '5%',
    },
  ];

  goToProfile(volunteerId: number) {
    this.$router.push({
      name: 'volunteer-profile',
      params: { id: String(volunteerId) },
    });
  }

  async fetchVolunteerProfiles() {
    try {
      this.volunteerProfiles = await RemoteServices.getAllVolunteerProfiles(); // Fetch from backend
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.institutionProfiles = await RemoteServices.getAllInstitutionProfiles();
      await this.fetchVolunteerProfiles();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
  
  viewInstitutionDetails(institutionProfile: InstitutionProfile) {
      if (institutionProfile?.institution?.id) {
        this.$store.commit('setCurrentInstitutionProfile', institutionProfile);
        this.$router.push({
          name: 'institution-profile',
          params: { id: institutionProfile.institution.id.toString() }
        });
      } else {
        console.error('Invalid institutionProfile:', institutionProfile);
      }
  }
}
</script>

<style lang="scss" scoped>
.table {
  margin-bottom: 20px;
}
.date-fields-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.date-fields-row {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}
</style>
