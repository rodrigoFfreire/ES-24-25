<template>
  <div class="container">
    <!-- in case theres no porifle, show button -->
    <div v-if="!hasProfile">
      <h1 class="mb-4">Institution Profile</h1>
      <p class="mb-8">
        No institution profile found. Click the button below to create a new one!
      </p>
      <v-btn color="blue" @click="openDialog">Create Institution Profile</v-btn>
    </div>

    <!-- in case theres a profile, show info (TODO)-->
    <div v-else>
      <h1>Institution: SHOW INSTITUTION NAME HERE</h1>
      <div class="text-description">
        <p><strong>Short Description: </strong> SHOW SHORT DESCRIPTION HERE</p>
      </div>

      <div class="stats-container">
        <!-- Total Members -->
        <div class="items">
          <div class="icon-wrapper">
            <span>{{ profile.totalMembers }}</span>
          </div>
          <div class="project-name">
            <p>Total Members</p>
          </div>
        </div>

        <!-- Total Activities -->
        <div class="items">
          <div class="icon-wrapper">
            <span>{{ profile.totalActivities }}</span>
          </div>
          <div class="project-name">
            <p>Total Activities</p>
          </div>
        </div>

        <!-- Total Volunteers -->
        <div class="items">
          <div class="icon-wrapper">
            <span>{{ profile.totalVolunteers }}</span>
          </div>
          <div class="project-name">
            <p>Total Volunteers</p>
          </div>
        </div>

        <!-- Total Assessments -->
        <div class="items">
          <div class="icon-wrapper">
            <span>{{ profile.totalAssessments }}</span>
          </div>
          <div class="project-name">
            <p>Total Assessments</p>
          </div>
        </div>

        <!-- Average Rating -->
        <div class="items">
          <div class="icon-wrapper">
            <span>{{ profile.averageRating.toFixed(2) }}</span>
          </div>
          <div class="project-name">
            <p>Average Rating</p>
          </div>
        </div>
      </div>

      <div>
        <h2>Selected Assessments</h2>
        <div>
          <v-card class="table">
            <v-data-table
              :headers="headers"
              :search="search"
              disable-pagination
              :hide-default-footer="true"
              :mobile-breakpoint="0"
              data-cy="institutionAssessmentsTable"
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
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { ISOtoString } from "../../services/ConvertDateService";
import InstitutionProfile from '@/models/profile/InstitutionProfile';

@Component({
  methods: { ISOtoString },
  components: {
  }
})
export default class InstitutionProfileView extends Vue {
  institutionId: number = 0;
  profile: InstitutionProfile | null = null;
  search: string = '';
  showDialog: boolean = false;

  headers: object = [
    {
      text: 'Volunteer Name',
      value: 'volunteerName',
      align: 'left',
      width: '30%',
    },
    {
      text: 'Review',
      value: 'review',
      align: 'left',
      width: '30%',
    },
    {
      text: 'Review Date',
      value: 'reviewDate',
      align: 'left',
      width: '40%',
    }
  ];

  async created() {
    await this.$store.dispatch('loading');

    try {
      this.institutionId = Number(this.$route.params.id);
      this.profile = await RemoteServices.getInstitutionProfile(this.institutionId);
      console.log('Profile returned:', this.profile);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  get hasProfile() {
    const desc = this.profile?.shortDescription;
    return desc !== undefined && desc !== null && desc.trim() !== '';
  }

  openDialog() {
    this.showDialog = true;
  }

  closeDialog() {
    this.showDialog = false;
  }
}
</script>

<style lang="scss" scoped>
.stats-container {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: center;
  align-items: stretch;
  align-content: center;
  height: 100%;

  .items {
    background-color: rgba(255, 255, 255, 0.75);
    color: #696969;
    border-radius: 5px;
    flex-basis: 25%;
    margin: 20px;
    cursor: pointer;
    transition: all 0.6s;
  }
}

.icon-wrapper,
.project-name {
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrapper {
  font-size: 100px;
  transform: translateY(0px);
  transition: all 0.6s;
}

.icon-wrapper {
  align-self: end;
}

.project-name {
  align-self: start;
}

.project-name p {
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 2px;
  transform: translateY(0px);
  transition: all 0.5s;
}

.items:hover {
  border: 3px solid black;

  & .project-name p {
    transform: translateY(-10px);
  }

  & .icon-wrapper i {
    transform: translateY(5px);
  }
}

.text-description {
  display: block;
  padding: 1em;
}
</style>