<template>
  <v-dialog v-model="dialog" persistent width="1300">
    <v-card>
      <v-card-title>
        <span class="headline">New Activity Suggestion</span>
      </v-card-title>
      <v-card-text>
        <v-form ref="form" lazy-validation>
          <v-row>
            <!-- Name + Institution (cada um com 1/3 da largura) -->
            <v-col cols="12" sm="4">
              <v-text-field
                label="*Name"
                v-model="editSuggestion.name"
                :rules="[(v) => !!v || 'Name is required']"
                required
                data-cy="nameInput"
              />
            </v-col>

            <v-col cols="12" sm="4">
              <v-select
                label="*Institution"
                v-model="editSuggestion.institutionId"
                :items="institutions"
                item-text="name"
                item-value="id"
                :rules="[(v) => !!v || 'Institution is required']"
                required
                data-cy="institutionSelect"
              />
            </v-col>

            <!-- Espaço vazio ou para ajustes futuros -->
            <v-col cols="12" sm="4">
              <!-- Opcional: v-spacer ou simplesmente deixado vazio -->
            </v-col>

            <!-- Description: ocupa 100%, mas continua como text-field -->
            <v-col cols="12">
              <v-text-field
                label="*Description"
                v-model="editSuggestion.description"
                :rules="[(v) => !!v || 'Description is required']"
                required
                data-cy="descriptionInput"
              />
            </v-col>

            <!-- Region -->
            <v-col cols="12">
              <v-text-field
                label="*Region"
                v-model="editSuggestion.region"
                :rules="[(v) => !!v || 'Region is required']"
                required
                data-cy="regionInput"
              />
            </v-col>

            <!-- Number of Participants -->
            <v-col cols="12">
              <v-text-field
                label="*Number of Participants"
                v-model.number="editSuggestion.participantsNumberLimit"
                :rules="[(v) => v > 0 || 'Must be greater than 0']"
                required
                type="number"
                data-cy="participantsInput"
              />
            </v-col>

            <!-- Data fields: 3 colunas -->
            <v-col cols="12" sm="4">
              <VueCtkDateTimePicker
                v-model="editSuggestion.applicationDeadline"
                label="*Application Deadline"
                format="YYYY-MM-DDTHH:mm:ssZ"
                id="applicationDeadlineInput"
              />
            </v-col>
            <v-col cols="12" sm="4">
              <VueCtkDateTimePicker
                v-model="editSuggestion.startingDate"
                label="*Starting Date"
                format="YYYY-MM-DDTHH:mm:ssZ"
                id="startingDateInput"
              />
            </v-col>
            <v-col cols="12" sm="4">
              <VueCtkDateTimePicker
                v-model="editSuggestion.endingDate"
                label="*Ending Date"
                format="YYYY-MM-DDTHH:mm:ssZ"
                id="endingDateInput"
              />
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          color="blue-darken-1"
          variant="text"
          @click="() => { resetForm(); $emit('close-activity-suggestion-dialog') }"
        >
          Close
        </v-btn>
        <v-btn
          v-if="canSave"
          color="blue-darken-1"
          variant="text"
          @click="saveSuggestion"
          data-cy="saveActivitySuggestion"
        >
          Save
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Vue, Component, Prop, Model } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import ActivitySuggestion from '@/models/activitysuggestion/ActivitySuggestion';
import Institution from '@/models/institution/Institution';
import VueCtkDateTimePicker from 'vue-ctk-date-time-picker';
import 'vue-ctk-date-time-picker/dist/vue-ctk-date-time-picker.css';

Vue.component('VueCtkDateTimePicker', VueCtkDateTimePicker);

@Component
export default class ActivitySuggestionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;

  editSuggestion: ActivitySuggestion = new ActivitySuggestion();
  institutions: Institution[] = [];

  resetForm() {
    this.editSuggestion = new ActivitySuggestion();
    if (this.$refs.form) {
      (this.$refs.form as Vue & { reset: () => void }).reset();
    }
  }

  async created() {
    this.editSuggestion = new ActivitySuggestion();
    try {
      this.institutions = await RemoteServices.getInstitutions();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  get canSave(): boolean {
    const s = this.editSuggestion;
    return (
      !!s.name &&
      !!s.institutionId &&
      !!s.description &&
      !!s.region &&
      s.participantsNumberLimit > 0 &&
      !!s.applicationDeadline &&
      !!s.startingDate &&
      !!s.endingDate
    );
  }

  async saveSuggestion() {
    const valid = (this.$refs.form as Vue & { validate: () => boolean }).validate();
    if (!valid) return;

    try {
      const result = await RemoteServices.createActivitySuggestion(
        this.editSuggestion.institutionId,
        this.editSuggestion,
      );
      this.$emit('save-activity-suggestion', result);
      this.resetForm();
      this.$emit('close-activity-suggestion-dialog');
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>
