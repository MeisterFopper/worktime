<script setup>
defineProps({
  title: { type: String, required: true },
  lines: { type: Array, default: () => [] }, // [{ label, value }]
  closeText: { type: String, default: 'Close' },
});

const emit = defineEmits(['close', 'confirm']);

function close() {
  emit('close');
}

function onKeydown(e) {
  if (e.key === 'Escape') emit('close');
}
</script>

<template>
  <div
    class="modal d-block"
    tabindex="-1"
    role="dialog"
    style="background: rgba(0,0,0,.5)"
    @keydown="onKeydown"
  >
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ title }}</h5>
          <button type="button" class="btn-close" @click="close" />
        </div>

        <div class="modal-body">
          <table class="table table-sm mb-0 info-modal__table">
            <tbody>
              <tr v-for="(line, idx) in lines" :key="idx">
                <th class="text-muted fw-normal align-top" style="width: 30%;">
                  {{ line?.label ?? '' }}
                </th>
                <td class="text-break">
                  {{ line?.value ?? '-' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" type="button" @click="close">
            {{ closeText }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

