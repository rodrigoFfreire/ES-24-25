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
    <v-card class="table">
      <v-card-title>
        <h2>Institution Profiles</h2>
      </v-card-title>
      <v-data-table
        :headers="headersInstitutionProfile"
        :items="institutionProfiles"
        :search="search"
        disable-pagination
        :hide-default-footer="true"
        :mobile-breakpoint="0"
      >
        <template v-slot:item.institution.creationDate="{ item }">
          {{ ISOtoString(item.institution.creationDate) }}
        </template>
        <template v-slot:item.institution.active="{ item }">
          </v-chip>
        </template>
        <template v-slot:item.action="{ item }">
          <v-btn
            icon
            @click="viewInstitutionDetails(item)"
          >
          <v-icon class="pr-2">visibility</v-icon>
          </v-btn>
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
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import { ISOtoString } from "../../services/ConvertDateService";
import RemoteServices from '../../services/RemoteServices';
import InstitutionProfile from '@/models/institution/InstitutionProfile';
import VolunteerProfile from '@/models/volunteer/VolunteerProfile';

@Component({
  methods: { ISOtoString }
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

  async created() {
    await this.$store.dispatch('loading');
    try {
      // Fetch institution profiles
      this.institutionProfiles = await RemoteServices.getAllInstitutionProfiles();
      // TODO: Fetch volunteer profiles when needed
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
  
  viewInstitutionDetails(institution: InstitutionProfile) {
    // Navigate to the institution details page
    this.$router.push({ 
      name: 'institution-profile', 
      params: { id: institution.id.toString() } 
    });
  }
}
</script>

<style lang="scss" scoped>
.table {
  margin-bottom: 20px;
}
</style>
