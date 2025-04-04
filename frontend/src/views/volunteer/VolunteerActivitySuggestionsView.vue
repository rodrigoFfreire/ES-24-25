<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :items="activitySuggestions"
      :search="search"
      :sort-by="['name']"
      disable-pagination
      :hide-default-footer="true"
      :mobile-breakpoint="0"
      data-cy="volunteerActivitySuggestionsTable"
    >
      <template v-slot:item.institutionName="{ item }">
        <span>{{ getInstitutionName(item.institutionId) }}</span>
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
          <v-btn
            color="primary"
            @click="dialog = true"
            data-cy="newActivitySuggestionButton"
          >
            New Activity Suggestion
          </v-btn>
        </v-card-title>
      </template>
    </v-data-table>

    <activity-suggestion-dialog
      v-model="dialog"
      @save-activity-suggestion="onSuggestionCreated"
      @close-activity-suggestion-dialog="dialog = false"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import ActivitySuggestion from '@/models/activitysuggestion/ActivitySuggestion';
import ActivitySuggestionDialog from '@/views/volunteer/ActivitySuggestionDialog.vue';
import Institution from '@/models/institution/Institution';

@Component({
  components: {
    'activity-suggestion-dialog': ActivitySuggestionDialog,
  },
})
export default class VolunteerActivitySuggestionsView extends Vue {
  activitySuggestions: ActivitySuggestion[] = [];
  institutions: Institution[] = [];
  search: string = '';
  dialog: boolean = false;

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
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.activitySuggestions = await RemoteServices.getVolunteerActivitySuggestions();
      this.institutions = await RemoteServices.getInstitutions();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  getInstitutionName(searchId:number) {
    return this.institutions.find((institution) => institution.id == searchId)?.name;
  }

  onSuggestionCreated(newSuggestion: ActivitySuggestion) {
    this.activitySuggestions.push(newSuggestion);
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
