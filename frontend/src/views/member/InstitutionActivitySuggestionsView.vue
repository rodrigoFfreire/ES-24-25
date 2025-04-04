<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :items="suggestions"
      :search="search"
      disable-pagination
      :sort-by="['name']" 
      :hide-default-footer="true"
      :mobile-breakpoint="0"
      data-cy="institutionActivitySuggestionsTable"
    >
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
      <template v-slot:[`item.actions`]="{ item }">
        <v-tooltip
          bottom
          v-if="item.state == 'REJECTED' || item.state == 'IN_REVIEW'"
        >
          <template v-slot:activator="{ on }">
            <v-icon
              class="mr-2 action-button"
              color="green"
              v-on="on"
              data-cy="approveButton"
              @click="approveActivitySuggestion(item)"
              >mdi-check-bold</v-icon
            >
          </template>
          <span>Approve Activity Suggestion</span>
        </v-tooltip>
        <v-tooltip
          bottom
          v-if="item.state == 'IN_REVIEW' || item.state == 'APPROVED'"
        >
          <template v-slot:activator="{ on }">
            <v-icon
              class="mr-2 action-button"
              color="red"
              v-on="on"
              data-cy="rejectButton"
              @click="rejectActivitySuggestion(item)"
              >mdi-pause-octagon</v-icon
            >
          </template>
          <span>Reject Activity Suggestion</span>
        </v-tooltip>
      </template>
    </v-data-table>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import Institution from '@/models/institution/Institution';
import ActivitySuggestion from '@/models/activitysuggestion/ActivitySuggestion';
import RemoteServices from '@/services/RemoteServices';

@Component({
  components: {},
})
export default class InstitutionActivitySuggestionsView extends Vue {
  suggestions: ActivitySuggestion[] = [];
  institution: Institution = new Institution();
  search: string = '';
  headers: object = [
    {
      text: 'Name',
      value: 'name',
      align: 'left',
      width: '10%',
    },
    {
      text: 'Institution',
      value: 'institutionName',
      align: 'left',
      width: '10%',
    },
    {
      text: 'Description',
      value: 'description',
      align: 'left',
      width: '30%',
    },
    {
      text: 'Region',
      value: 'region',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Participants Limit',
      value: 'participantsNumberLimit',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Start Date',
      value: 'formattedStartingDate',
      align: 'left',
      width: '5%',
    },
    {
      text: 'End Date',
      value: 'formattedEndingDate',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Application Deadline',
      value: 'formattedApplicationDeadline',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Creation Date',
      value: 'creationDate',
      align: 'left',
      width: '5%',
    },
    {
      text: 'State',
      value: 'state',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Actions',
      value: 'actions',
      align: 'left',
      sortable: false,
      width: '5%',
    },
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      let userId = this.$store.getters.getUser.id;
      this.institution = await RemoteServices.getInstitution(userId!);
      this.suggestions =  await RemoteServices.getActivitySuggestionsByInstitution(this.institution.id!);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async approveActivitySuggestion(suggestion: ActivitySuggestion) {
    if (suggestion.id !== null && this.institution.id != null) {
      try {
        this.suggestions = await RemoteServices.approveActivitySuggestion(
          this.institution.id,
          suggestion.id,
        );
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  async rejectActivitySuggestion(suggestion: ActivitySuggestion) {
    if (suggestion.id !== null && this.institution.id != null) {
      try {
        this.suggestions = await RemoteServices.rejectActivitySuggestion(
          this.institution.id,
          suggestion.id,
        );
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  institutionName() {
    return this.institution.name;
  }
}
</script>

<style lang="scss" scoped>
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